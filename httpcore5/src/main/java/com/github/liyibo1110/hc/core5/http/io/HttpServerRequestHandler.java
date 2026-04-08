package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 代表用于处理特定组HTTP请求的例程。
 * 请求执行过滤器旨在处理协议特有的方面，而各个请求处理程序则负责处理应用程序特有的HTTP处理。
 * 请求处理程序的主要目的是生成一个包含内容实体的响应对象，以响应给定的请求并将其发回给客户端。
 * @author liyibo
 * @date 2026-04-07 14:11
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpServerRequestHandler {

    /**
     * 响应触发器，可用于提交最终的HTTP响应并终止HTTP请求的处理。
     */
    interface ResponseTrigger {

        /**
         * 向客户端发送一个中间状态的HTTP响应。
         * ClassicHttpResponse状态为非1xx。
         */
        void sendInformation(ClassicHttpResponse response) throws HttpException, IOException;

        /**
         * 向客户端发送最终的HTTP响应。
         */
        void submitResponse(ClassicHttpResponse response) throws HttpException, IOException;
    }

    /**
     * 处理该请求，并提交最终响应以发回给客户端。
     */
    void handle(ClassicHttpRequest request, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException;
}
