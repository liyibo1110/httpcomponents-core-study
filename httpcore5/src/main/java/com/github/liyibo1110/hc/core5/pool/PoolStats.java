package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

import java.io.Serializable;

/**
 * 连接池统计值
 * @author liyibo
 * @date 2026-04-12 14:10
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class PoolStats implements Serializable {

    private static final long serialVersionUID = -2807686144795228544L;

    private final int leased;
    private final int pending;
    private final int available;
    private final int max;

    public PoolStats(final int leased, final int pending, final int free, final int max) {
        super();
        this.leased = leased;
        this.pending = pending;
        this.available = free;
        this.max = max;
    }

    public int getLeased() {
        return this.leased;
    }

    public int getPending() {
        return this.pending;
    }

    public int getAvailable() {
        return this.available;
    }

    public int getMax() {
        return this.max;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased);
        buffer.append("; pending: ");
        buffer.append(this.pending);
        buffer.append("; available: ");
        buffer.append(this.available);
        buffer.append("; max: ");
        buffer.append(this.max);
        buffer.append("]");
        return buffer.toString();
    }
}
