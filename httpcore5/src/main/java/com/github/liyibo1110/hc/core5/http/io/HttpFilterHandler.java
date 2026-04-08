package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 代表了在服务器端请求处理链中处理所有传入请求的例程。
 * @author liyibo
 * @date 2026-04-07 14:17
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpFilterHandler {

    /**
     * 处理传入的HTTP请求，并在处理完成后向客户端提交最终响应。
     * 处理程序在使用HttpFilterChain.proceed(ClassicHttpRequest, HttpFilterChain.ResponseTrigger, HttpContext)方法将控制权传递给下一个过滤器后，不得再使用响应触发器。
     */
    void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger,
                HttpContext context, HttpFilterChain chain) throws HttpException, IOException;
}
