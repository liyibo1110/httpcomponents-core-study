package com.github.liyibo1110.hc.core5.concurrent;

import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TimeoutValueException;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future接口的基本实现。
 * 通过调用以下任一方法，可将BasicFuture置于已完成状态：cancel()、failed(Exception) 或 completed(Object)。
 * @author liyibo
 * @date 2026-04-13 11:05
 */
public class BasicFuture<T> implements Future<T>, Cancellable {

    private final FutureCallback<T> callback;

    private volatile boolean completed;
    private volatile boolean cancelled;
    private volatile T result;
    private volatile Exception ex;

    public BasicFuture(final FutureCallback<T> callback) {
        super();
        this.callback = callback;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if(ex != null)
            throw new ExecutionException(ex);
        if(cancelled)
            throw new CancellationException();
        return result;
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!completed)
            wait();
        return getResult();
    }

    @Override
    public synchronized T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Args.notNull(unit, "Time unit");
        final long msecs = unit.toMillis(timeout);  // 最多要等待的超时时间
        // msecs小于等于0，表示无限等待
        final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
        long waitTime = msecs;
        if (completed) {    // 已经完成了，则直接返回
            return getResult();
        } else if (waitTime <= 0) { // 超时了
            throw TimeoutValueException.fromMilliseconds(msecs, msecs + Math.abs(waitTime));
        } else {
            while (true) {
                wait(waitTime); // 等待notify
                if(completed)
                    return getResult();
                // 醒了但是还没完成，则重新计算waitTime继续循环
                waitTime = msecs - (System.currentTimeMillis() - startTime);
                if(waitTime <= 0)
                    throw TimeoutValueException.fromMilliseconds(msecs, msecs + Math.abs(waitTime));
            }
        }
    }

    /**
     * 计算完毕，将result写入（该方法只能被调用一次）。
     */
    public boolean completed(final T result) {
        synchronized (this) {
            if(completed)   // 说明已经调用过completed方法了，直接返回false
                return false;
            this.completed = true;
            this.result = result;
            notifyAll();    // 唤醒get中的阻塞
        }
        if(callback != null)    // 执行回调
            callback.completed(result);
        return true;
    }

    /**
     * 流程和completed方法一样。
     */
    public boolean failed(final Exception exception) {
        synchronized(this) {
            if (this.completed)
                return false;
            this.completed = true;
            this.ex = exception;
            notifyAll();
        }
        if (this.callback != null)
            this.callback.failed(exception);
        return true;
    }

    /**
     * 流程和completed方法一样。
     * 没用到mayInterruptIfRunning这个参数。
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        synchronized(this) {
            if (this.completed)
                return false;
            this.completed = true;
            this.cancelled = true;
            notifyAll();
        }
        if (this.callback != null)
            this.callback.cancelled();
        return true;
    }

    @Override
    public boolean cancel() {
        return cancel(true);
    }
}
