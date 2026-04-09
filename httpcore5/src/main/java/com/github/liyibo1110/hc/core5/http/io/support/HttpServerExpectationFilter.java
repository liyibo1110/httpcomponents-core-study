package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterChain;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterHandler;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * HttpServerExpectationFilter为请求处理管道添加了对Expect-Continue握手协议的支持。
 * @author liyibo
 * @date 2026-04-08 17:07
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public class HttpServerExpectationFilter implements HttpFilterHandler {

    /**
     * 验证HTTP请求，并判断其是否符合服务器预期，以及请求处理是否可以继续。
     */
    protected boolean verify(final ClassicHttpRequest request, final HttpContext context) throws HttpException {
        return true;
    }

    /**
     * 生成最终HTTP响应的响应内容实体，其中包含表示预期失败原因的错误状态码。
     */
    protected HttpEntity generateResponseContent(final HttpResponse expectationFailed) throws HttpException {
        return null;
    }

    @Override
    public final void handle(final ClassicHttpRequest request, final HttpFilterChain.ResponseTrigger responseTrigger,
                             final HttpContext context, final HttpFilterChain chain) throws HttpException, IOException {
        final Header expect = request.getFirstHeader(HttpHeaders.EXPECT);
        final boolean expectContinue = expect != null && HeaderElements.CONTINUE.equalsIgnoreCase(expect.getValue());
        if (expectContinue) {
            final boolean verified = verify(request, context);
            if (verified) {
                responseTrigger.sendInformation(new BasicClassicHttpResponse(HttpStatus.SC_CONTINUE));
            } else {
                final ClassicHttpResponse expectationFailed = new BasicClassicHttpResponse(HttpStatus.SC_EXPECTATION_FAILED);
                final HttpEntity responseContent = generateResponseContent(expectationFailed);
                expectationFailed.setEntity(responseContent);
                responseTrigger.submitResponse(expectationFailed);
                return;
            }
        }
        chain.proceed(request, responseTrigger, context);
    }
}
