package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpRequestMapper;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterChain;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterHandler;
import com.github.liyibo1110.hc.core5.http.io.HttpRequestHandler;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * HttpFilterHandler的实现代表请求处理管道中的一个终端处理器，
 * 它利用 HttpRequestMapper将请求分发给特定的HttpRequestHandler。
 * @author liyibo
 * @date 2026-04-08 17:09
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public final class TerminalServerFilter implements HttpFilterHandler {

    private final HttpRequestMapper<HttpRequestHandler> handlerMapper;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public TerminalServerFilter(final HttpRequestMapper<HttpRequestHandler> handlerMapper,
                                final HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.handlerMapper = Args.notNull(handlerMapper, "Handler mapper");
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    @Override
    public void handle(final ClassicHttpRequest request, final HttpFilterChain.ResponseTrigger responseTrigger,
                       final HttpContext context, final HttpFilterChain chain) throws HttpException, IOException {
        final ClassicHttpResponse response = responseFactory.newHttpResponse(HttpStatus.SC_OK);
        final HttpRequestHandler handler = handlerMapper.resolve(request, context);
        if (handler != null)
            handler.handle(request, response, context);
        else
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        responseTrigger.submitResponse(response);
    }
}
