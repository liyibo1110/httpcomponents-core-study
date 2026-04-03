package com.github.liyibo1110.hc.core5.http;

import java.net.URI;

/**
 * HttpRequest实例的工厂
 * @author liyibo
 * @date 2026-04-03 11:02
 */
public interface HttpRequestFactory<T extends HttpRequest> {

    /**
     * 使用指定的method和请求uri创建HttpRequest。
     */
    T newHttpRequest(String method, String uri) throws MethodNotSupportedException;

    T newHttpRequest(String method, URI uri) throws MethodNotSupportedException;
}
