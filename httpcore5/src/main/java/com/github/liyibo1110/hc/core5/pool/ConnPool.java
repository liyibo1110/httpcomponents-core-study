package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.concurrent.FutureCallback;
import com.github.liyibo1110.hc.core5.io.ModalCloseable;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.util.concurrent.Future;

/**
 * ConnPool表示一个共享连接池，连接可以从中租用，也可以归还至该池。
 * @author liyibo
 * @date 2026-04-12 14:06
 */
public interface ConnPool<T, C extends ModalCloseable> {

    /**
     * 尝试从连接池中租用一个具有指定路由和指定状态的连接。
     * 请注意，如果请求超时，连接池可能会自动取消该连接请求。
     */
    Future<PoolEntry<T, C>> lease(T route, Object state, Timeout requestTimeout, FutureCallback<PoolEntry<T, C>> callback);

    /**
     * 将该entry释放回池中。
     */
    void release(PoolEntry<T, C> entry, boolean reusable);
}
