package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

/**
 * HttpRequestHandler对象的外层封装了（多了hostname和uriPattern字段）。
 * @author liyibo
 * @date 2026-04-10 17:33
 */
public class HandlerEntry<T> {
    final String hostname;
    final String uriPattern;
    final T handler;

    public HandlerEntry(final String hostname, final String uriPattern, final T handler) {
        this.hostname = hostname;
        this.uriPattern = uriPattern;
        this.handler = handler;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HandlerEntry [hostname=");
        builder.append(hostname);
        builder.append(", uriPattern=");
        builder.append(uriPattern);
        builder.append(", handler=");
        builder.append(handler);
        builder.append("]");
        return builder.toString();
    }
}
