package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

/**
 * 该类可用于解析与特定HttpRequest匹配的对象。
 * 通常映射出的对象将是一个用于处理该请求的请求处理程序。
 * @author liyibo
 * @date 2026-04-03 14:56
 */
public interface HttpRequestMapper<T> {

    /**
     * 解析给定的request。
     */
    T resolve(HttpRequest request, HttpContext context) throws HttpException;
}
