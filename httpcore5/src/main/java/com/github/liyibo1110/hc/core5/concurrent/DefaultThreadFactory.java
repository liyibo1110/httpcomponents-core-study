package com.github.liyibo1110.hc.core5.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认的ThreadFactory实现。
 * @author liyibo
 * @date 2026-04-10 17:01
 */
public class DefaultThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final ThreadGroup group;
    private final AtomicLong count;
    private final boolean daemon;

    public DefaultThreadFactory(final String namePrefix, final ThreadGroup group, final boolean daemon) {
        this.namePrefix = namePrefix;
        this.group = group;
        this.daemon = daemon;
        this.count = new AtomicLong();
    }

    public DefaultThreadFactory(final String namePrefix, final boolean daemon) {
        this(namePrefix, null, daemon);
    }

    public DefaultThreadFactory(final String namePrefix) {
        this(namePrefix, null, false);
    }

    @Override
    public Thread newThread(final Runnable target) {
        final Thread thread = new Thread(this.group, target, this.namePrefix + "-"  + this.count.incrementAndGet());
        thread.setDaemon(daemon);
        return thread;
    }
}
