package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.util.TimeValue;

import java.util.Set;

/**
 * 用于控制连接池运行时属性的接口，例如允许的最大总连接数或每条路由的最大连接数。
 * @author liyibo
 * @date 2026-04-12 14:12
 */
public interface ConnPoolControl<T> extends ConnPoolStats<T> {

    void setMaxTotal(int max);

    int getMaxTotal();

    void setDefaultMaxPerRoute(int max);

    int getDefaultMaxPerRoute();

    void setMaxPerRoute(final T route, int max);

    int getMaxPerRoute(final T route);

    void closeIdle(TimeValue idleTime);

    void closeExpired();

    Set<T> getRoutes();
}
