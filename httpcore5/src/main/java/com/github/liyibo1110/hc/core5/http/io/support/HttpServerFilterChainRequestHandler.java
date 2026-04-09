package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterChain;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * 一种HttpServerRequestHandler实现，它将请求处理委托给HttpServerFilterChainElement。
 * @author liyibo
 * @date 2026-04-08 17:13
 */
public class HttpServerFilterChainRequestHandler implements HttpServerRequestHandler {

    private final HttpServerFilterChainElement filterChain;

    public HttpServerFilterChainRequestHandler(final HttpServerFilterChainElement filterChain) {
        this.filterChain = Args.notNull(filterChain, "Filter chain");
    }

    @Override
    public void handle(final ClassicHttpRequest request, final ResponseTrigger trigger, final HttpContext context)
            throws HttpException, IOException {
        filterChain.handle(request, new HttpFilterChain.ResponseTrigger() {
            @Override
            public void sendInformation(final ClassicHttpResponse response) throws HttpException, IOException {
                trigger.sendInformation(response);
            }

            @Override
            public void submitResponse(final ClassicHttpResponse response) throws HttpException, IOException {
                trigger.submitResponse(response);
            }
        }, context);
    }
}
