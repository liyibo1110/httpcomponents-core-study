package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.concurrent.BasicFuture;
import com.github.liyibo1110.hc.core5.concurrent.FutureCallback;
import com.github.liyibo1110.hc.core5.function.Callback;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.io.ModalCloseable;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Asserts;
import com.github.liyibo1110.hc.core5.util.Deadline;
import com.github.liyibo1110.hc.core5.util.DeadlineTimeoutException;
import com.github.liyibo1110.hc.core5.util.TimeValue;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 连接池的最终实现（strict模式）。
 * strict主要体现在对maxTotal/maxPerRoute这样的上限会尽量给出严格保证。
 * @author liyibo
 * @date 2026-04-13 10:24
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class StrictConnPool<T, C extends ModalCloseable> implements ManagedConnPool<T, C> {

    private final TimeValue timeToLive;
    private final PoolReusePolicy policy;
    private final DisposalCallback<C> disposalCallback;
    private final ConnPoolListener<T> connPoolListener;

    /** 每个route还对应一个自己的小连接池 */
    private final Map<T, PerRoutePool<T, C>> routeToPool;

    /** 当前还没拿到连接，还在排队等的lease请求 */
    private final LinkedList<LeaseRequest<T, C>> pendingRequests;

    /** 已经借出的连接 */
    private final Set<PoolEntry<T, C>> leased;

    /** 当前空闲、可供lease的entry，支持LIFO和FIFO两种策略 */
    private final LinkedList<PoolEntry<T, C>> available;

    /** 已经拿到结果或异常的leaseRequest，暂时先放到这个并发队列里，稍后统一调用fireCallback */
    private final ConcurrentLinkedQueue<LeaseRequest<T, C>> completedRequests;

    /**
     * 以下3个字段都是上限相关的配置：
     * 1、maxPerRoute：每个route可以有单独上限。
     * 2、defaultMaxPerRoute没有单独配的route，用这个默认上限。
     * 3、maxTotal、全局还有总上限。
     */
    private final Map<T, Integer> maxPerRoute;
    private volatile int defaultMaxPerRoute;
    private volatile int maxTotal;

    /** 连接池是否已关闭 */
    private final AtomicBoolean isShutDown;

    /** 整个连接池就用这一个锁 */
    private final Lock lock;

    public StrictConnPool(final int defaultMaxPerRoute,
                          final int maxTotal,
                          final TimeValue timeToLive,
                          final PoolReusePolicy policy,
                          final DisposalCallback<C> disposalCallback,
                          final ConnPoolListener<T> connPoolListener) {
        super();
        Args.positive(defaultMaxPerRoute, "Max per route value");
        Args.positive(maxTotal, "Max total value");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.policy = policy != null ? policy : PoolReusePolicy.LIFO;   // 默认是LIFO
        this.disposalCallback = disposalCallback;
        this.connPoolListener = connPoolListener;
        this.routeToPool = new HashMap<>();
        this.pendingRequests = new LinkedList<>();
        this.leased = new HashSet<>();
        this.available = new LinkedList<>();
        this.completedRequests = new ConcurrentLinkedQueue<>();
        this.maxPerRoute = new HashMap<>();
        this.lock = new ReentrantLock();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.maxTotal = maxTotal;
    }

    public StrictConnPool(final int defaultMaxPerRoute,
                          final int maxTotal,
                          final TimeValue timeToLive,
                          final PoolReusePolicy policy,
                          final ConnPoolListener<T> connPoolListener) {
        this(defaultMaxPerRoute, maxTotal, timeToLive, policy, null, connPoolListener);
    }

    public StrictConnPool(final int defaultMaxPerRoute, final int maxTotal) {
        this(defaultMaxPerRoute, maxTotal, TimeValue.NEG_ONE_MILLISECOND, PoolReusePolicy.LIFO, null);
    }

    public boolean isShutdown() {
        return this.isShutDown.get();
    }

    @Override
    public void close(final CloseMode closeMode) {
        if (this.isShutDown.compareAndSet(false, true)) {

        }
    }

    @Override
    public void close() {
        close(CloseMode.GRACEFUL);
    }

    private PerRoutePool<T, C> getPool(final T route) {
        PerRoutePool<T, C> pool = this.routeToPool.get(route);
        // 不存在就新增
        if (pool == null) {
            pool = new PerRoutePool<>(route, disposalCallback);
            routeToPool.put(route, pool);
        }
        return pool;
    }

    @Override
    public Future<PoolEntry<T, C>> lease(final T route, final Object state,
                                         final Timeout requestTimeout,
                                         final FutureCallback<PoolEntry<T, C>> callback) {
        Args.notNull(route, "Route");
        Args.notNull(requestTimeout, "Request timeout");
        Asserts.check(!this.isShutDown.get(), "Connection pool shut down");
        final Deadline deadline = Deadline.calculate(requestTimeout);

        final var future = new BasicFuture<>(callback) {
            @Override
            public synchronized PoolEntry<T, C> get(final long timeout, final TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                try {
                    return super.get(timeout, unit);
                } catch (final TimeoutException e) {
                    // 如果future层面超时了，会先cancel，相当于影响了future里面的状态字段
                    cancel();
                    throw e;
                }
            }
        };

        // 先抢锁，抢到了才能尝试借出连接
        final boolean acquiredLock;
        try {
            if(Timeout.isPositive(requestTimeout)) {
                acquiredLock = lock.tryLock(requestTimeout.getDuration(), requestTimeout.getTimeUnit());
            } else {
                this.lock.lockInterruptibly();
                acquiredLock = true;
            }
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            future.cancel();
            return future;
        }

        if (acquiredLock) {
            try {
                final LeaseRequest<T, C> request = new LeaseRequest<>(route, state, requestTimeout, future);
                final boolean completed = processPendingRequest(request);
                if(!request.isDone() && !completed) // 没有成功lease到连接
                    pendingRequests.add(request);
                if(request.isDone())
                    completedRequests.add(request);
            } finally {
                this.lock.unlock();
            }
            fireCallbacks();
        } else {
            future.failed(DeadlineTimeoutException.from(deadline));
        }
        return future;
    }

    public Future<PoolEntry<T, C>> lease(final T route, final Object state) {
        return lease(route, state, Timeout.DISABLED, null);
    }

    @Override
    public void release(final PoolEntry<T, C> entry, final boolean reusable) {
        if (entry == null)
            return;
        if (this.isShutDown.get())
            return;
        if (!reusable)  // 不允许复用，则要丢弃底层连接
            entry.discardConnection(CloseMode.GRACEFUL);
        this.lock.lock();
        try {
            if (this.leased.remove(entry)) {
                if (this.connPoolListener != null)
                    this.connPoolListener.onRelease(entry.getRoute(), this);
                // 获取对应route级别的池子
                final PerRoutePool<T, C> pool = getPool(entry.getRoute());
                final boolean keepAlive = entry.hasConnection() && reusable;
                // 先处理好route池子内部的归还操作
                pool.free(entry, keepAlive);
                if (keepAlive) {
                    switch (policy) {
                        case LIFO:
                            available.addFirst(entry);
                            break;
                        case FIFO:
                            available.addLast(entry);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected ConnPoolPolicy value: " + policy);
                    }
                } else {
                    entry.discardConnection(CloseMode.GRACEFUL);
                }
                // 重要：立即尝试处理下一个pending请求，因为已经release了，可以让下一个排队的请求来lease了
                processNextPendingRequest();
            } else {
                throw new IllegalStateException("Pool entry is not present in the set of leased entries");
            }
        } finally {
            this.lock.unlock();
        }
        fireCallbacks();
    }

    /**
     * 遍历全部pendingRequests：
     * 1、如果future已经cancel，则移除，否则尝试processPendingRequest(request)。
     * 2、如果request已完成或这轮已经处理掉，则从pending中移除。
     * 3、如果request真正完成，移到completedRequests等待callback。
     */
    private void processPendingRequests() {
        final ListIterator<LeaseRequest<T, C>> it = this.pendingRequests.listIterator();
        while (it.hasNext()) {
            final LeaseRequest<T, C> request = it.next();
            final var future = request.getFuture();
            if(future.isCancelled()) {
                it.remove();
                continue;
            }
            final boolean completed = processPendingRequest(request);
            if(request.isDone() || completed)
                it.remove();
            if(request.isDone())
                completedRequests.add(request);
        }
    }

    /**
     * 让pending队列中，前面的一个请求开始干活。
     */
    private void processNextPendingRequest() {
        final ListIterator<LeaseRequest<T, C>> it = this.pendingRequests.listIterator();
        while (it.hasNext()) {
            final LeaseRequest<T, C> request = it.next();
            final var future = request.getFuture();
            if(future.isCancelled()) {
                it.remove();
                continue;
            }
            // 触发干活
            final boolean completed = processPendingRequest(request);
            if(request.isDone() || completed)
                it.remove();
            if(request.isDone())
                completedRequests.add(request);
            if(completed)
                return;
        }
    }

    /**
     * 触发LeaseRequest开始干活的调度决策树方法。
     */
    private boolean processPendingRequest(final LeaseRequest<T, C> request) {
        final T route = request.getRoute();
        final Object state = request.getState();
        final Deadline deadline = request.getDeadline();

        // 先检查request是否已经过期了
        if (deadline.isExpired()) {
            request.failed(DeadlineTimeoutException.from(deadline));
            return false;
        }

        final PerRoutePool<T, C> pool = getPool(route);
        PoolEntry<T, C> entry;
        while (true) {
            // 从route池子里的available里尝试借一个entry
            entry = pool.getFree(state);
            if(entry == null)   // 没借到就跳出while循环
                break;
            // 如果借出来的entry已经过期了，则丢弃处理，然后继续再借一个，相当于做了附带清理的工作
            if(entry.getExpiryDeadline().isExpired()) {
                entry.discardConnection(CloseMode.GRACEFUL);
                available.remove(entry);
                pool.free(entry, false);
            } else {    // 借出来的entry如果没过期，也继续走后面的逻辑
                break;
            }
        }
        if(entry != null) {
            // 说明在上面的route池子，找到了能复用的entry，登记后直接返回
            this.available.remove(entry);
            this.leased.add(entry);
            request.completed(entry);
            if (this.connPoolListener != null)
                this.connPoolListener.onLease(entry.getRoute(), this);
            return true;
        }

        // 到这里说明没有借到entry，只能尝试新增entry
        final int maxPerRoute = getMax(route);
        // 计算新增了1个entry后，会不会超过route池子的容量上限
        final int excess = Math.max(0, pool.getAllocatedCount() + 1 - maxPerRoute);
        if(excess > 0) {
            // 进入说明超过了，会把route池子里，最后使用的available entry给移除（注意并没有清除leased）
            for (int i = 0; i < excess; i++) {
                final PoolEntry<T, C> lastUsed = pool.getLastUsed();
                if (lastUsed == null)
                    break;
                lastUsed.discardConnection(CloseMode.GRACEFUL);
                this.available.remove(lastUsed);
                pool.remove(lastUsed);
            }
        }

        if (pool.getAllocatedCount() < maxPerRoute) {
            // 还要检查全局池子的剩余容量
            final int freeCapacity = Math.max(this.maxTotal - this.leased.size(), 0);
            if (freeCapacity == 0)
                return false;
            final int totalAvailable = this.available.size();
            if (totalAvailable > freeCapacity - 1) {
                // 需要理解的一个环节，全局leased正常，但全局available太多了（available + leased达到了maxTotal - 1了），需要清理available
                final PoolEntry<T, C> lastUsed = this.available.removeLast();
                lastUsed.discardConnection(CloseMode.GRACEFUL);
                final PerRoutePool<T, C> otherpool = getPool(lastUsed.getRoute());
                otherpool.remove(lastUsed);
            }

            // 创建新的entry
            entry = pool.createEntry(this.timeToLive);
            this.leased.add(entry);
            request.completed(entry);
            if (this.connPoolListener != null)
                this.connPoolListener.onLease(entry.getRoute(), this);
            return true;
        }
        return false;
    }

    /**
     * 会从completedRequests队列中不断poll出请求，然后根据结果调用：
     * 1、future.failed(ex)
     * 2、future.completed(result)
     * 3、future.cancel()
     */
    private void fireCallbacks() {
        LeaseRequest<T, C> request;
        while ((request = this.completedRequests.poll()) != null) {
            final var future = request.getFuture();
            final Exception ex = request.getException();
            final PoolEntry<T, C> result = request.getResult();
            boolean successfullyCompleted = false;
            if (ex != null) {
                future.failed(ex);
            } else if(result != null) {
                if(future.completed(result))
                    successfullyCompleted = true;
            } else {
                future.cancel();
            }

            /**
             * 重要：如果future操作未成功，则必须显式把entry还回去
             */
            if(!successfullyCompleted)
                release(result, true);
        }
    }

    /**
     * 主要做超时/取消后的清理工作。
     */
    public void validatePendingRequests() {
        this.lock.lock();
        try {
            final long now = System.currentTimeMillis();
            final ListIterator<LeaseRequest<T, C>> it = this.pendingRequests.listIterator();
            while (it.hasNext()) {
                final LeaseRequest<T, C> request = it.next();
                final var future = request.getFuture();
                if (future.isCancelled() && !request.isDone()) {
                    it.remove();
                } else {
                    final Deadline deadline = request.getDeadline();
                    // 已经超时了
                    if (deadline.isBefore(now))
                        request.failed(DeadlineTimeoutException.from(deadline));
                    // 已经完成了
                    if (request.isDone()) {
                        it.remove();
                        this.completedRequests.add(request);
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
        fireCallbacks();
    }

    private int getMax(final T route) {
        final Integer v = maxPerRoute.get(route);
        if (v != null)
            return v;
        return defaultMaxPerRoute;
    }

    @Override
    public void setMaxTotal(final int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.maxTotal = max;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMaxTotal() {
        this.lock.lock();
        try {
            return this.maxTotal;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void setDefaultMaxPerRoute(final int max) {
        Args.positive(max, "Max value");
        this.lock.lock();
        try {
            this.defaultMaxPerRoute = max;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getDefaultMaxPerRoute() {
        this.lock.lock();
        try {
            return this.defaultMaxPerRoute;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void setMaxPerRoute(final T route, final int max) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            if (max > -1)
                this.maxPerRoute.put(route, max);
            else
                this.maxPerRoute.remove(route);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMaxPerRoute(final T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            return getMax(route);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public PoolStats getTotalStats() {
        this.lock.lock();
        try {
            return new PoolStats(this.leased.size(), this.pendingRequests.size(), this.available.size(), this.maxTotal);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public PoolStats getStats(final T route) {
        Args.notNull(route, "Route");
        this.lock.lock();
        try {
            final PerRoutePool<T, C> pool = getPool(route);
            int pendingCount = 0;
            for (final LeaseRequest<T, C> request: pendingRequests) {
                if (Objects.equals(route, request.getRoute()))
                    pendingCount++;
            }
            return new PoolStats(pool.getLeasedCount(), pendingCount, pool.getAvailableCount(), getMax(route));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Set<T> getRoutes() {
        this.lock.lock();
        try {
            return new HashSet<>(routeToPool.keySet());
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 处理available里所有的entry。
     */
    public void enumAvailable(final Callback<PoolEntry<T, C>> callback) {
        this.lock.lock();
        try {
            final Iterator<PoolEntry<T, C>> it = this.available.iterator();
            while (it.hasNext()) {
                final PoolEntry<T, C> entry = it.next();
                // 先执行外部传入的回调
                callback.execute(entry);
                // 连接没了，就顺便清理了
                if (!entry.hasConnection()) {
                    final PerRoutePool<T, C> pool = getPool(entry.getRoute());
                    pool.remove(entry);
                    it.remove();
                }
            }
            processPendingRequests();
            purgePoolMap();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 处理leased里所有的entry。
     */
    public void enumLeased(final Callback<PoolEntry<T, C>> callback) {
        this.lock.lock();
        try {
            final Iterator<PoolEntry<T, C>> it = this.leased.iterator();
            while (it.hasNext()) {
                final PoolEntry<T, C> entry = it.next();
                callback.execute(entry);
            }
            processPendingRequests();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 清理空的route池。
     */
    private void purgePoolMap() {
        final Iterator<Map.Entry<T, PerRoutePool<T, C>>> it = this.routeToPool.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<T, PerRoutePool<T, C>> entry = it.next();
            final PerRoutePool<T, C> pool = entry.getValue();
            if(pool.getAllocatedCount() == 0)
                it.remove();
        }
    }

    @Override
    public void closeIdle(final TimeValue idleTime) {
        final long deadline = System.currentTimeMillis() - (TimeValue.isPositive(idleTime) ? idleTime.toMilliseconds() : 0);
        enumAvailable(entry -> {
            if (entry.getUpdated() <= deadline)
                entry.discardConnection(CloseMode.GRACEFUL);
        });
    }

    @Override
    public void closeExpired() {
        final long now = System.currentTimeMillis();
        enumAvailable(entry -> {
            if (entry.getExpiryDeadline().isBefore(now))
                entry.discardConnection(CloseMode.GRACEFUL);
        });
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased.size());
        buffer.append("][available: ");
        buffer.append(this.available.size());
        buffer.append("][pending: ");
        buffer.append(this.pendingRequests.size());
        buffer.append("]");
        return buffer.toString();
    }

    static class LeaseRequest<T, C extends ModalCloseable> {
        private final T route;
        private final Object state;
        private final Deadline deadline;
        private final BasicFuture<PoolEntry<T, C>> future;
        private final AtomicBoolean completed;
        private volatile PoolEntry<T, C> result;
        private volatile Exception ex;

        public LeaseRequest(final T route, final Object state, final Timeout requestTimeout, final BasicFuture<PoolEntry<T, C>> future) {
            super();
            this.route = route;
            this.state = state;
            this.deadline = Deadline.calculate(requestTimeout);
            this.future = future;
            this.completed = new AtomicBoolean(false);
        }

        public T getRoute() {
            return this.route;
        }

        public Object getState() {
            return this.state;
        }

        public Deadline getDeadline() {
            return this.deadline;
        }

        public boolean isDone() {
            return ex != null || result != null;
        }

        public void failed(final Exception ex) {
            if (this.completed.compareAndSet(false, true))
                this.ex = ex;
        }

        public void completed(final PoolEntry<T, C> result) {
            if (this.completed.compareAndSet(false, true))
                this.result = result;
        }

        public BasicFuture<PoolEntry<T, C>> getFuture() {
            return this.future;
        }

        public PoolEntry<T, C> getResult() {
            return this.result;
        }

        public Exception getException() {
            return this.ex;
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            buffer.append("[");
            buffer.append(this.route);
            buffer.append("][");
            buffer.append(this.state);
            buffer.append("]");
            return buffer.toString();
        }
    }

    static class PerRoutePool<T, C extends ModalCloseable> {
        private final T route;
        private final Set<PoolEntry<T, C>> leased;
        private final LinkedList<PoolEntry<T, C>> available;
        private final DisposalCallback<C> disposalCallback;

        PerRoutePool(final T route, final DisposalCallback<C> disposalCallback) {
            super();
            this.route = route;
            this.disposalCallback = disposalCallback;
            this.leased = new HashSet<>();
            this.available = new LinkedList<>();
        }

        public final T getRoute() {
            return route;
        }

        public int getLeasedCount() {
            return this.leased.size();
        }

        public int getAvailableCount() {
            return this.available.size();
        }

        public int getAllocatedCount() {
            return this.available.size() + this.leased.size();
        }

        /**
         * 从池中获取一个特定state的entry，并注册登记。
         */
        public PoolEntry<T, C> getFree(final Object state) {
            if(!available.isEmpty()) {  // 有空余的才能尝试获取
                if(state != null) { // 如果外部传入了特定的state，则优先返回符合要求的，没找到还会走后面的通用逻辑
                    final Iterator<PoolEntry<T, C>> it = this.available.iterator();
                    while (it.hasNext()) {
                        final PoolEntry<T, C> entry = it.next();
                        if (state.equals(entry.getState())) {
                            it.remove();    // 从available队列里面移除
                            leased.add(entry);  // 放入leased集合
                            return entry;   // 直接返回完事
                        }
                    }
                }
                final Iterator<PoolEntry<T, C>> it = this.available.iterator();
                while (it.hasNext()) {
                    final PoolEntry<T, C> entry = it.next();
                    if (entry.getState() == null) { // 注意是拿state为null的entry
                        it.remove();
                        this.leased.add(entry);
                        return entry;
                    }
                }
            }
            return null;
        }

        public PoolEntry<T, C> getLastUsed() {
            return this.available.peekLast();
        }

        public boolean remove(final PoolEntry<T, C> entry) {
            // 注意只会remove一个地方，因为同一个entry不可能同时存在于2个集合
            return this.available.remove(entry) || this.leased.remove(entry);
        }

        /**
         * 归还entry。
         */
        public void free(final PoolEntry<T, C> entry, final boolean reusable) {
            final boolean found = this.leased.remove(entry);
            Asserts.check(found, "Entry %s has not been leased from this pool", entry);
            if(reusable)    // 是否放到队列头，下一次还是获取这个
                available.addFirst(entry);
        }

        /**
         * 直接创建entry，并登记在leased里，然后返回这个entry。
         */
        public PoolEntry<T, C> createEntry(final TimeValue timeToLive) {
            final PoolEntry<T, C> entry = new PoolEntry<>(this.route, timeToLive, disposalCallback);
            this.leased.add(entry);
            return entry;
        }

        /**
         * 关闭池
         */
        public void shutdown(final CloseMode closeMode) {
            PoolEntry<T, C> availableEntry;
            // 先清理所有库存entry
            while ((availableEntry = available.poll()) != null)
                availableEntry.discardConnection(closeMode);
            // 再清理所有借出的entry
            for (final var entry : leased)
                entry.discardConnection(closeMode);
            leased.clear();
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            buffer.append("[route: ");
            buffer.append(this.route);
            buffer.append("][leased: ");
            buffer.append(this.leased.size());
            buffer.append("][available: ");
            buffer.append(this.available.size());
            buffer.append("]");
            return buffer.toString();
        }
    }
}
