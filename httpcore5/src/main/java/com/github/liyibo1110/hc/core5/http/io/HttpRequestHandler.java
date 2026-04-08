package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 代表用于处理特定组HTTP请求的例程。协议处理程序旨在处理与协议相关的方面，而单个请求处理程序则负责处理与应用程序相关的HTTP处理。
 * 请求处理程序的主要目的是生成一个包含内容实体的响应对象，以响应给定的请求并将其发回给客户端。
 * @author liyibo
 * @date 2026-04-07 15:15
 */
public interface HttpRequestHandler {

    /**
     * 处理请求并生成响应，将其发回给客户端。
     */
    void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException;
}
