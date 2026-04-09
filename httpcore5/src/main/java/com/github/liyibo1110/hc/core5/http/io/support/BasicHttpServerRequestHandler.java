package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpRequestMapper;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.io.HttpRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * 基本的HttpServerRequestHandler实现类。
 * 利用了HttpRequestMapper将请求分发给特定的HttpRequestHandler进行处理。
 * @author liyibo
 * @date 2026-04-08 17:04
 */
public class BasicHttpServerRequestHandler implements HttpServerRequestHandler {

    private final HttpRequestMapper<HttpRequestHandler> handlerMapper;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public BasicHttpServerRequestHandler(final HttpRequestMapper<HttpRequestHandler> handlerMapper,
                                         final HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.handlerMapper = Args.notNull(handlerMapper, "Handler mapper");
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public BasicHttpServerRequestHandler(final HttpRequestMapper<HttpRequestHandler> handlerMapper) {
        this(handlerMapper, null);
    }

    @Override
    public void handle(final ClassicHttpRequest request, final ResponseTrigger responseTrigger, final HttpContext context)
            throws HttpException, IOException {
        final ClassicHttpResponse response = responseFactory.newHttpResponse(HttpStatus.SC_OK);
        // 找匹配的handler
        final HttpRequestHandler handler = handlerMapper != null ? handlerMapper.resolve(request, context) : null;
        if(handler != null)
            handler.handle(request, response, context);
        else
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        // 最终触发response回调
        responseTrigger.submitResponse(response);
    }
}
