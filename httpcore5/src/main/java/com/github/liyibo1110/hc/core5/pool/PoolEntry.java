package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.io.ModalCloseable;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Deadline;
import com.github.liyibo1110.hc.core5.util.TimeValue;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 包含连接对象及其路由的连接池entry。
 * 分配给此连接池entry的连接可能具有过期时间，并包含一个表示连接状态的对象（通常是安全主体或唯一令牌，用于标识在建立连接时所使用的凭据所属的用户）。
 * @author liyibo
 * @date 2026-04-10 18:03
 */
public final class PoolEntry<T, C extends ModalCloseable> {

    /** 池中连接的另一端点的路由类型，类似key的作用。 */
    private final T route;
    private final TimeValue timeToLive;
    private final AtomicReference<C> connRef;
    private final DisposalCallback<C> disposalCallback;
    private final Supplier<Long> currentTimeSupplier;

    private volatile Object state;
    private volatile long created;
    private volatile long updated;
    private volatile Deadline expiryDeadline = Deadline.MIN_VALUE;
    private volatile Deadline validityDeadline = Deadline.MIN_VALUE;

    PoolEntry(final T route, final TimeValue timeToLive, final DisposalCallback<C> disposalCallback,
              final Supplier<Long> currentTimeSupplier) {
        super();
        this.route = Args.notNull(route, "Route");
        this.timeToLive = TimeValue.defaultsToNegativeOneMillisecond(timeToLive);
        this.connRef = new AtomicReference<>();
        this.disposalCallback = disposalCallback;
        this.currentTimeSupplier = currentTimeSupplier;
    }

    PoolEntry(final T route, final TimeValue timeToLive, final Supplier<Long> currentTimeSupplier) {
        this(route, timeToLive, null, currentTimeSupplier);
    }

    public PoolEntry(final T route, final TimeValue timeToLive, final DisposalCallback<C> disposalCallback) {
        this(route, timeToLive, disposalCallback, null);
    }

    public PoolEntry(final T route, final TimeValue timeToLive) {
        this(route, timeToLive, null, null);
    }

    public PoolEntry(final T route) {
        this(route, null);
    }

    long getCurrentTime() {
        return currentTimeSupplier != null ? currentTimeSupplier.get() : System.currentTimeMillis();
    }

    public T getRoute() {
        return this.route;
    }

    public C getConnection() {
        return this.connRef.get();
    }

    public Deadline getValidityDeadline() {
        return this.validityDeadline;
    }

    public Object getState() {
        return this.state;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return this.updated;
    }

    public Deadline getExpiryDeadline() {
        return this.expiryDeadline;
    }

    public boolean hasConnection() {
        return this.connRef.get() != null;
    }

    /**
     * 绑定给定的Connection。
     */
    public void assignConnection(final C conn) {
        Args.notNull(conn, "connection");
        if (this.connRef.compareAndSet(null, conn)) {
            this.created = getCurrentTime();
            this.updated = this.created;
            this.validityDeadline = Deadline.calculate(this.created, this.timeToLive);
            this.expiryDeadline = this.validityDeadline;
            this.state = null;
        } else {
            throw new IllegalStateException("Connection already assigned");
        }
    }

    /**
     * 丢弃当前绑定的Connection。
     */
    public void discardConnection(final CloseMode closeMode) {
        final C connection = this.connRef.getAndSet(null);
        if (connection != null) {
            this.state = null;
            this.created = 0;
            this.updated = 0;
            this.expiryDeadline = Deadline.MIN_VALUE;
            this.validityDeadline = Deadline.MIN_VALUE;
            if (this.disposalCallback != null)
                this.disposalCallback.execute(connection, closeMode);
            else
                connection.close(closeMode);
        }
    }

    public void updateExpiry(final TimeValue expiryTime) {
        Args.notNull(expiryTime, "Expiry time");
        final long currentTime = getCurrentTime();
        final Deadline newExpiry = Deadline.calculate(currentTime, expiryTime);
        this.expiryDeadline = newExpiry.min(this.validityDeadline);
        this.updated = currentTime;
    }

    public void updateState(final Object state) {
        this.state = state;
        this.updated = getCurrentTime();
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[route:");
        buffer.append(this.route);
        buffer.append("][state:");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }
}
