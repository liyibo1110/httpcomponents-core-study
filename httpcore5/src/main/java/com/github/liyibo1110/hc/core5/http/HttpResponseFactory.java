package com.github.liyibo1110.hc.core5.http;

/**
 * HttpResponse实例的工厂
 * @author liyibo
 * @date 2026-04-03 11:09
 */
public interface HttpResponseFactory<T extends HttpResponse> {

    /**
     * 使用指定的status和reasonPhrase创建HttpResponse。
     */
    T newHttpResponse(int status, String reasonPhrase);

    T newHttpResponse(int status);
}
