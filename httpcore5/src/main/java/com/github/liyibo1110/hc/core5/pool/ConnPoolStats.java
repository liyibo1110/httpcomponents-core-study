package com.github.liyibo1110.hc.core5.pool;

/**
 * 用于获取连接池统计信息的接口。
 * @author liyibo
 * @date 2026-04-12 14:10
 */
public interface ConnPoolStats<T> {

    PoolStats getTotalStats();

    PoolStats getStats(final T route);
}
