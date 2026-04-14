package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.Experimental;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.concurrent.BasicFuture;
import com.github.liyibo1110.hc.core5.concurrent.Cancellable;
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

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * 连接池的最终实现（lex模式）。
 * 具有更高的并发能力，但对连接数量的限制较为宽松。
 * 1、没有全局锁，而是改成各种的并发安全的容器。
 * 2、只关心每个route池的max。
 * 3、pending队列不再是全局的，而是每个route池子有自己的pending队列。
 * 4、不会为了新来的request而踢掉其它route池里面的available队列的entry。
 * 5、追求更高并发效率和更低的协调成本。
 * 因此全局代码变简单了，但是route池子里面的代码变复杂了。
 * @author liyibo
 * @date 2026-04-13 14:38
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Experimental
public class LaxConnPool<T, C extends ModalCloseable> implements ManagedConnPool<T, C> {

    private final TimeValue timeToLive;
    private final PoolReusePolicy policy;
    private final DisposalCallback<C> disposalCallback;
    private final ConnPoolListener<T> connPoolListener;
    private final ConcurrentMap<T, PerRoutePool<T, C>> routeToPool;
    private final AtomicBoolean isShutDown;
    private volatile int defaultMaxPerRoute;

    public LaxConnPool(final int defaultMaxPerRoute,
                       final TimeValue timeToLive,
                       final PoolReusePolicy policy,
                       final DisposalCallback<C> disposalCallback,
                       final ConnPoolListener<T> connPoolListener) {
        super();
        Args.positive(defaultMaxPerRoute, "Max per route value");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.policy = policy != null ? policy : PoolReusePolicy.LIFO;
        this.disposalCallback = disposalCallback;
        this.connPoolListener = connPoolListener;
        this.routeToPool = new ConcurrentHashMap<>();
        this.isShutDown = new AtomicBoolean(false);
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public LaxConnPool(final int defaultMaxPerRoute,
                       final TimeValue timeToLive,
                       final PoolReusePolicy policy,
                       final ConnPoolListener<T> connPoolListener) {
        this(defaultMaxPerRoute, timeToLive, policy, null, connPoolListener);
    }

    public LaxConnPool(final int defaultMaxPerRoute) {
        this(defaultMaxPerRoute, TimeValue.NEG_ONE_MILLISECOND, PoolReusePolicy.LIFO, null, null);
    }

    public boolean isShutdown() {
        return isShutDown.get();
    }

    @Override
    public void close(final CloseMode closeMode) {
        if (isShutDown.compareAndSet(false, true)) {
            for (final Iterator<PerRoutePool<T, C>> it = routeToPool.values().iterator(); it.hasNext(); ) {
                final PerRoutePool<T, C> routePool = it.next();
                routePool.shutdown(closeMode);
            }
            routeToPool.clear();
        }
    }

    @Override
    public void close() {
        close(CloseMode.GRACEFUL);
    }

    private PerRoutePool<T, C> getPool(final T route) {
        PerRoutePool<T, C> routePool = routeToPool.get(route);
        if (routePool == null) {
            final PerRoutePool<T, C> newRoutePool = new PerRoutePool<>(route, defaultMaxPerRoute, timeToLive, policy, this, disposalCallback, connPoolListener);
            routePool = routeToPool.putIfAbsent(route, newRoutePool);
            if (routePool == null)
                routePool = newRoutePool;
        }
        return routePool;
    }

    @Override
    public Future<PoolEntry<T, C>> lease(final T route, final Object state,
                                         final Timeout requestTimeout,
                                         final FutureCallback<PoolEntry<T, C>> callback) {
        Args.notNull(route, "Route");
        Asserts.check(!isShutDown.get(), "Connection pool shut down");
        // 直接尝试从route池子里lease
        final PerRoutePool<T, C> routePool = getPool(route);
        return routePool.lease(state, requestTimeout, callback);
    }

    public Future<PoolEntry<T, C>> lease(final T route, final Object state) {
        return lease(route, state, Timeout.DISABLED, null);
    }

    @Override
    public void release(final PoolEntry<T, C> entry, final boolean reusable) {
        if (entry == null)
            return;
        if (isShutDown.get())
            return;
        // 直接往route池子里release
        final PerRoutePool<T, C> routePool = getPool(entry.getRoute());
        routePool.release(entry, reusable);
    }

    public void validatePendingRequests() {
        // 直接调用每个route池子的validatePendingRequests方法
        for (final PerRoutePool<T, C> routePool : routeToPool.values())
            routePool.validatePendingRequests();
    }

    @Override
    public void setMaxTotal(final int max) {
        // nothing to do
    }

    @Override
    public int getMaxTotal() {
        return 0;
    }

    @Override
    public void setDefaultMaxPerRoute(final int max) {
        Args.positive(max, "Max value");
        defaultMaxPerRoute = max;
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return defaultMaxPerRoute;
    }

    @Override
    public void setMaxPerRoute(final T route, final int max) {
        Args.notNull(route, "Route");
        final PerRoutePool<T, C> routePool = getPool(route);
        routePool.setMax(max > -1 ? max : defaultMaxPerRoute);
    }

    @Override
    public int getMaxPerRoute(final T route) {
        Args.notNull(route, "Route");
        final PerRoutePool<T, C> routePool = getPool(route);
        return routePool.getMax();
    }

    @Override
    public PoolStats getTotalStats() {
        int leasedTotal = 0;
        int pendingTotal = 0;
        int availableTotal = 0;
        int maxTotal = 0;
        for (final PerRoutePool<T, C> routePool : routeToPool.values()) {
            leasedTotal += routePool.getLeasedCount();
            pendingTotal += routePool.getPendingCount();
            availableTotal += routePool.getAvailableCount();
            maxTotal += routePool.getMax();
        }
        return new PoolStats(leasedTotal, pendingTotal, availableTotal, maxTotal);
    }

    @Override
    public PoolStats getStats(final T route) {
        Args.notNull(route, "Route");
        final PerRoutePool<T, C> routePool = getPool(route);
        return new PoolStats(routePool.getLeasedCount(), routePool.getPendingCount(), routePool.getAvailableCount(), routePool.getMax());
    }

    @Override
    public Set<T> getRoutes() {
        return new HashSet<>(routeToPool.keySet());
    }

    public void enumAvailable(final Callback<PoolEntry<T, C>> callback) {
        // 直接调用每个route池子的enumAvailable方法
        for (final PerRoutePool<T, C> routePool : routeToPool.values())
            routePool.enumAvailable(callback);
    }

    public void enumLeased(final Callback<PoolEntry<T, C>> callback) {
        // 直接调用每个route池子的enumLeased方法
        for (final PerRoutePool<T, C> routePool : routeToPool.values())
            routePool.enumLeased(callback);
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
        final PoolStats totalStats = getTotalStats();
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(totalStats.getLeased());
        buffer.append("][available: ");
        buffer.append(totalStats.getAvailable());
        buffer.append("][pending: ");
        buffer.append(totalStats.getPending());
        buffer.append("]");
        return buffer.toString();
    }

    static class LeaseRequest<T, C extends ModalCloseable> implements Cancellable {
        private final Object state;
        private final Deadline deadline;
        private final BasicFuture<PoolEntry<T, C>> future;

        LeaseRequest(final Object state, final Timeout requestTimeout, final BasicFuture<PoolEntry<T, C>> future) {
            super();
            this.state = state;
            this.deadline = Deadline.calculate(requestTimeout);
            this.future = future;
        }

        BasicFuture<PoolEntry<T, C>> getFuture() {
            return this.future;
        }

        public Object getState() {
            return this.state;
        }

        public Deadline getDeadline() {
            return this.deadline;
        }

        public boolean isDone() {
            return this.future.isDone();
        }

        public boolean completed(final PoolEntry<T, C> result) {
            return future.completed(result);
        }

        public boolean failed(final Exception ex) {
            return future.failed(ex);
        }

        @Override
        public boolean cancel() {
            return future.cancel();
        }
    }

    /**
     * 和Strict版本实现相比，基本所有代码都移到了这里面。
     */
    static class PerRoutePool<T, C extends ModalCloseable> {
        private enum RequestServiceStrategy { FIRST_SUCCESSFUL, ALL }

        private final T route;
        private final TimeValue timeToLive;
        private final PoolReusePolicy policy;
        private final DisposalCallback<C> disposalCallback;
        private final ConnPoolListener<T> connPoolListener;
        private final ConnPoolStats<T> connPoolStats;
        private final ConcurrentMap<PoolEntry<T, C>, Boolean> leased;

        /** 注意用到了AtomicMarkableReference，即内部的boolean用来表示：这个entry是否已经被某线程拿走了 */
        private final Deque<AtomicMarkableReference<PoolEntry<T, C>>> available;
        private final Deque<LeaseRequest<T, C>> pending;
        private final AtomicBoolean terminated;
        private final AtomicInteger allocated;

        /**
         * 可用资源状态发生变化的版本号，作用是判断一轮调度期间，route内资源是否发生过新的变化。
         * 只要当前route里面的available/pending等，因release或枚举清理等动作发生变化，这个数字就会加1。
         */
        private final AtomicLong releaseSeqNum;

        private volatile int max;

        PerRoutePool(final T route,
                     final int max,
                     final TimeValue timeToLive,
                     final PoolReusePolicy policy,
                     final ConnPoolStats<T> connPoolStats,
                     final DisposalCallback<C> disposalCallback,
                     final ConnPoolListener<T> connPoolListener) {
            super();
            this.route = route;
            this.timeToLive = timeToLive;
            this.policy = policy;
            this.connPoolStats = connPoolStats;
            this.disposalCallback = disposalCallback;
            this.connPoolListener = connPoolListener;
            this.leased = new ConcurrentHashMap<>();
            this.available = new ConcurrentLinkedDeque<>();
            this.pending = new ConcurrentLinkedDeque<>();
            this.terminated = new AtomicBoolean(false);
            this.allocated = new AtomicInteger(0);
            this.releaseSeqNum = new AtomicLong(0);
            this.max = max;
        }

        public void shutdown(final CloseMode closeMode) {
            if (terminated.compareAndSet(false, true)) {
                AtomicMarkableReference<PoolEntry<T, C>> entryRef;
                // 清理available
                while ((entryRef = available.poll()) != null)
                    entryRef.getReference().discardConnection(closeMode);
                // 清理leased
                for (final PoolEntry<T, C> entry : leased.keySet())
                    entry.discardConnection(closeMode);
                leased.clear();
                // 清理pending
                LeaseRequest<T, C> leaseRequest;
                while ((leaseRequest = pending.poll()) != null)
                    leaseRequest.cancel();
            }
        }

        private PoolEntry<T, C> createPoolEntry() {
            final int poolMax = max;
            int prev;
            int next;
            do {
                prev = allocated.get();
                next = (prev < poolMax) ? prev + 1 : prev;
            } while (!allocated.compareAndSet(prev, next));
            return (prev < next)? new PoolEntry<>(route, timeToLive, disposalCallback) : null;
        }

        private void deallocatePoolEntry() {
            allocated.decrementAndGet();
        }

        private void addLeased(final PoolEntry<T, C> entry) {
            if (leased.putIfAbsent(entry, Boolean.TRUE) != null)
                throw new IllegalStateException("Pool entry already present in the set of leased entries");
            else if (connPoolListener != null)
                connPoolListener.onLease(route, connPoolStats);
        }

        private void removeLeased(final PoolEntry<T, C> entry) {
            if (connPoolListener != null)
                connPoolListener.onRelease(route, connPoolStats);
            if (!leased.remove(entry, Boolean.TRUE))
                throw new IllegalStateException("Pool entry is not present in the set of leased entries");
        }

        public Future<PoolEntry<T, C>> lease(final Object state, final Timeout requestTimeout, final FutureCallback<PoolEntry<T, C>> callback) {
            Asserts.check(!terminated.get(), "Connection pool shut down");
            final BasicFuture<PoolEntry<T, C>> future = new BasicFuture<>(callback) {
                @Override
                public synchronized PoolEntry<T, C> get(final long timeout, final TimeUnit unit)
                        throws InterruptedException, ExecutionException, TimeoutException {
                    try {
                        return super.get(timeout, unit);
                    } catch (final TimeoutException ex) {
                        cancel();
                        throw ex;
                    }
                }
            };
            final long releaseState = releaseSeqNum.get();
            PoolEntry<T, C> entry = null;
            if (pending.isEmpty()) {
                // 如果没有线程在池里排队，则直接从available里拿，拿不到直接new新的entry
                entry = getAvailableEntry(state);
                if (entry == null)
                    entry = createPoolEntry();
            }
            if (entry != null) {
                // 在上面拿到了entry，返回即可
                addLeased(entry);
                future.completed(entry);
            } else {
                // 在上面没有拿到entry，说明要么pending已有线程在排队，或者池子容量满了，所以只能去排队
                pending.add(new LeaseRequest<>(state, requestTimeout, future));
                // 重要：排队之前会再确认队列版本，如果变了，说明可能有entry了，于是会立即调用servicePendingRequest尝试entry获取
                if (releaseState != releaseSeqNum.get())
                    servicePendingRequest();
            }
            return future;
        }

        private PoolEntry<T, C> getAvailableEntry(final Object state) {
            for (final var it = available.iterator(); it.hasNext(); ) {
                final var ref = it.next();
                final PoolEntry<T, C> entry = ref.getReference();
                if (ref.compareAndSet(entry, entry, false, true)) {
                    // 进入说明抢到了空闲的entry（mark原来为false）
                    it.remove();
                    if (entry.getExpiryDeadline().isExpired())
                        entry.discardConnection(CloseMode.GRACEFUL);
                    if (!Objects.equals(entry.getState(), state))
                        entry.discardConnection(CloseMode.GRACEFUL);
                    return entry;
                }
            }
            return null;
        }

        public void release(final PoolEntry<T, C> releasedEntry, final boolean reusable) {
            removeLeased(releasedEntry);
            if (!reusable || releasedEntry.getExpiryDeadline().isExpired())
                releasedEntry.discardConnection(CloseMode.GRACEFUL);
            if (releasedEntry.hasConnection()) {
                switch (policy) {
                    case LIFO:
                        available.addFirst(new AtomicMarkableReference<>(releasedEntry, false));
                        break;
                    case FIFO:
                        available.addLast(new AtomicMarkableReference<>(releasedEntry, false));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected ConnPoolPolicy value: " + policy);
                }
            } else {
                deallocatePoolEntry();
            }
            releaseSeqNum.incrementAndGet();
            servicePendingRequest();
        }

        private void servicePendingRequest() {
            servicePendingRequests(RequestServiceStrategy.FIRST_SUCCESSFUL);
        }

        /**
         * 处理当前route池子里，pending队列的lease请求。
         * 尽量把能满足的请求分配到available entry或新建entry上。
         */
        private void servicePendingRequests(final RequestServiceStrategy serviceStrategy) {
            LeaseRequest<T, C> leaseRequest;
            // 循环从pending队列取出entry
            while ((leaseRequest = pending.poll()) != null) {
                if (leaseRequest.isDone())
                    continue;
                final Object state = leaseRequest.getState();
                final Deadline deadline = leaseRequest.getDeadline();

                if (deadline.isExpired()) {
                    leaseRequest.failed(DeadlineTimeoutException.from(deadline));
                } else {
                    final long releaseState = releaseSeqNum.get();
                    // 尝试去available找一个可用的匹配entry
                    PoolEntry<T, C> entry = getAvailableEntry(state);
                    if (entry == null)
                        entry = createPoolEntry();
                    if(entry != null) {
                        addLeased(entry);
                        /**
                         * 重要的判断逻辑：如果借出来的entry绑定失败了（比如调用了request的cancel方法），则直接release这个entry
                         */
                        if (!leaseRequest.completed(entry))
                            release(entry, true);
                        // 如果策略是FIRST_SUCCESSFUL，一旦成功服务了1个请求，则终止循环，否则会继续扫描更多pending请求
                        if (serviceStrategy == RequestServiceStrategy.FIRST_SUCCESSFUL)
                            break;
                    } else {
                        // 如果还是没有成功拿到entry，就只能把拿出来的request再放回去
                        pending.addFirst(leaseRequest);
                        // 重点：这里会看这段时间版本号有没有变化，如果没有变化，就不用循环了，因为说明不会有新的entry可以用
                        if(releaseState == releaseSeqNum.get())
                            break;
                    }
                }
            }
        }

        public void validatePendingRequests() {
            final Iterator<LeaseRequest<T, C>> it = pending.iterator();
            while (it.hasNext()) {
                final LeaseRequest<T, C> request = it.next();
                final var future = request.getFuture();
                if (future.isCancelled() && !request.isDone()) {
                    it.remove();
                } else {
                    final Deadline deadline = request.getDeadline();
                    if (deadline.isExpired())
                        request.failed(DeadlineTimeoutException.from(deadline));
                    if (request.isDone())
                        it.remove();
                }
            }
        }

        public final T getRoute() {
            return route;
        }

        public int getMax() {
            return max;
        }

        public void setMax(final int max) {
            this.max = max;
        }

        public int getPendingCount() {
            return pending.size();
        }

        public int getLeasedCount() {
            return leased.size();
        }

        public int getAvailableCount() {
            return available.size();
        }

        public void enumAvailable(final Callback<PoolEntry<T, C>> callback) {
            for (final var it = available.iterator(); it.hasNext(); ) {
                final AtomicMarkableReference<PoolEntry<T, C>> ref = it.next();
                final PoolEntry<T, C> entry = ref.getReference();
                // available空闲的才可以进入
                if (ref.compareAndSet(entry, entry, false, true)) {
                    callback.execute(entry);
                    if (!entry.hasConnection()) {   // 可以清理的
                        deallocatePoolEntry();
                        it.remove();
                    } else {    // 没有清理的，mark要改回false
                        ref.set(entry, false);
                    }
                }
            }
            releaseSeqNum.incrementAndGet();    // 清理后要加版本号
            servicePendingRequests(RequestServiceStrategy.ALL);
        }

        public void enumLeased(final Callback<PoolEntry<T, C>> callback) {
            for (final var it = leased.keySet().iterator(); it.hasNext(); ) {
                final PoolEntry<T, C> entry = it.next();
                callback.execute(entry);
                if (!entry.hasConnection()) {
                    deallocatePoolEntry();
                    it.remove();
                }
            }
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();
            buffer.append("[route: ");
            buffer.append(route);
            buffer.append("][leased: ");
            buffer.append(leased.size());
            buffer.append("][available: ");
            buffer.append(available.size());
            buffer.append("][pending: ");
            buffer.append(pending.size());
            buffer.append("]");
            return buffer.toString();
        }
    }
}
