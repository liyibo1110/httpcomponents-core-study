package com.github.liyibo1110.hc.core5.pool;

/**
 * 连接池的event listener，会监听lease和release动作。
 * @author liyibo
 * @date 2026-04-13 10:10
 */
public interface ConnPoolListener<T> {

    void onLease(T route, ConnPoolStats<T> connPoolStats);

    void onRelease(T route, ConnPoolStats<T> connPoolStats);
}
