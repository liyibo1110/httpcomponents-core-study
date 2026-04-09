package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * 一种HttpServerRequestHandler实现。
 * 用于为现有的HttpServerRequestHandler添加对Expect-Continue握手协议的支持。
 * @author liyibo
 * @date 2026-04-08 17:11
 */
public class BasicHttpServerExpectationDecorator implements HttpServerRequestHandler {

    private final HttpServerRequestHandler requestHandler;

    public BasicHttpServerExpectationDecorator(final HttpServerRequestHandler requestHandler) {
        this.requestHandler = Args.notNull(requestHandler, "Request handler");
    }

    protected ClassicHttpResponse verify(final ClassicHttpRequest request, final HttpContext context) {
        return null;
    }

    @Override
    public final void handle(final ClassicHttpRequest request, final ResponseTrigger responseTrigger, final HttpContext context)
            throws HttpException, IOException {
        final Header expect = request.getFirstHeader(HttpHeaders.EXPECT);
        if (expect != null && HeaderElements.CONTINUE.equalsIgnoreCase(expect.getValue())) {
            final ClassicHttpResponse response = verify(request, context);
            if (response == null) {
                responseTrigger.sendInformation(new BasicClassicHttpResponse(HttpStatus.SC_CONTINUE));
            } else {
                responseTrigger.submitResponse(response);
                return;
            }
        }
        requestHandler.handle(request, responseTrigger, context);
    }
}
