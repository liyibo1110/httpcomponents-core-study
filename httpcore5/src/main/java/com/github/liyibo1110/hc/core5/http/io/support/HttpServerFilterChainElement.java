package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterChain;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterHandler;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 代表请求处理链中的一个环节。
 * @author liyibo
 * @date 2026-04-08 17:12
 */
public class HttpServerFilterChainElement {

    private final HttpFilterHandler handler;
    private final HttpServerFilterChainElement next;
    private final HttpFilterChain filterChain;

    public HttpServerFilterChainElement(final HttpFilterHandler handler, final HttpServerFilterChainElement next) {
        this.handler = handler;
        this.next = next;
        this.filterChain = next != null ? next::handle : null;
    }

    public void handle(final ClassicHttpRequest request, final HttpFilterChain.ResponseTrigger responseTrigger,
                       final HttpContext context) throws IOException, HttpException {
        handler.handle(request, responseTrigger, context, filterChain);
    }

    @Override
    public String toString() {
        return "{" +
                "handler=" + handler.getClass() +
                ", next=" + (next != null ? next.handler.getClass() : "null") +
                '}';
    }
}
