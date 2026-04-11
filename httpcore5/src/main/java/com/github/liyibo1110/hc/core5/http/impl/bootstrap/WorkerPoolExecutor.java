package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Worker对应的线程池组件
 * @author liyibo
 * @date 2026-04-10 15:59
 */
class WorkerPoolExecutor extends ThreadPoolExecutor {

    private final Map<Worker, Boolean> workerSet;

    public WorkerPoolExecutor(final int corePoolSize,
                              final int maximumPoolSize,
                              final long keepAliveTime,
                              final TimeUnit unit,
                              final BlockingQueue<Runnable> workQueue,
                              final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.workerSet = new ConcurrentHashMap<>();
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        if (r instanceof Worker)
            this.workerSet.put((Worker) r, Boolean.TRUE);
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        if (r instanceof Worker)
            this.workerSet.remove(r);
    }

    public Set<Worker> getWorkers() {
        return new HashSet<>(this.workerSet.keySet());
    }
}
