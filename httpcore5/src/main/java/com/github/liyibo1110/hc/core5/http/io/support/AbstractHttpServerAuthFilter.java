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
import com.github.liyibo1110.hc.core5.http.io.entity.EntityUtils;
import com.github.liyibo1110.hc.core5.http.io.entity.StringEntity;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.net.URIAuthority;

import java.io.IOException;

/**
 * 一个实现标准HTTP身份验证握手流程的抽象HTTP请求过滤器。
 * @author liyibo
 * @date 2026-04-08 16:59
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public abstract class AbstractHttpServerAuthFilter<T> implements HttpFilterHandler {

    private final boolean respondImmediately;

    protected AbstractHttpServerAuthFilter(final boolean respondImmediately) {
        this.respondImmediately = respondImmediately;
    }

    /**
     * 将授权header value解析为身份验证token，该token由客户端作为对身份验证申请的响应发送。
     */
    protected abstract T parseChallengeResponse(String authorizationValue, HttpContext context) throws HttpException;

    /**
     * 使用客户端作为对身份验证申请的响应而发送的身份验证token，对客户端进行身份验证。
     */
    protected abstract boolean authenticate(T challengeResponse, URIAuthority authority, String requestUri, HttpContext context);

    /**
     * 如果身份验证失败，则生成一个身份验证申请。
     */
    protected abstract String generateChallenge(T challengeResponse, URIAuthority authority, String requestUri, HttpContext context);

    protected HttpEntity generateResponseContent(final HttpResponse unauthorized) {
        return new StringEntity("Unauthorized");
    }

    @Override
    public final void handle(final ClassicHttpRequest request, final HttpFilterChain.ResponseTrigger responseTrigger,
                             final HttpContext context, final HttpFilterChain chain) throws HttpException, IOException {
        final Header h = request.getFirstHeader(HttpHeaders.AUTHORIZATION);
        final T challengeResponse = h != null ? parseChallengeResponse(h.getValue(), context) : null;

        final URIAuthority authority = request.getAuthority();
        final String requestUri = request.getRequestUri();

        // 这里是验证逻辑
        final boolean authenticated = authenticate(challengeResponse, authority, requestUri, context);
        final Header expect = request.getFirstHeader(HttpHeaders.EXPECT);
        final boolean expectContinue = expect != null && HeaderElements.CONTINUE.equalsIgnoreCase(expect.getValue());

        if (authenticated) {
            if (expectContinue)
                responseTrigger.sendInformation(new BasicClassicHttpResponse(HttpStatus.SC_CONTINUE));
            chain.proceed(request, responseTrigger, context);
        } else {
            final ClassicHttpResponse unauthorized = new BasicClassicHttpResponse(HttpStatus.SC_UNAUTHORIZED);
            unauthorized.addHeader(HttpHeaders.WWW_AUTHENTICATE, generateChallenge(challengeResponse, authority, requestUri, context));
            final HttpEntity responseContent = generateResponseContent(unauthorized);
            unauthorized.setEntity(responseContent);
            if (respondImmediately || expectContinue || request.getEntity() == null) {
                // Respond immediately
                responseTrigger.submitResponse(unauthorized);
                // Consume request body later
                EntityUtils.consume(request.getEntity());
            } else {
                // Consume request body first
                EntityUtils.consume(request.getEntity());
                // Respond later
                responseTrigger.submitResponse(unauthorized);
            }
        }
    }
}
