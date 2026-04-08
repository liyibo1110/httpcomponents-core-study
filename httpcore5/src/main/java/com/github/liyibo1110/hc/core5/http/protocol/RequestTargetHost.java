package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.Method;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.net.URIAuthority;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * RequestTargetHost负责在发出的消息中添加Host标头。
 * 此拦截器是客户端协议处理器所必需的。
 * @author liyibo
 * @date 2026-04-07 16:00
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestTargetHost implements HttpRequestInterceptor {

    public static final HttpRequestInterceptor INSTANCE = new RequestTargetHost();

    public RequestTargetHost() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");

        final ProtocolVersion ver = context.getProtocolVersion();
        final String method = request.getMethod();
        if (Method.CONNECT.isSame(method) && ver.lessEquals(HttpVersion.HTTP_1_0))
            return;

        if (!request.containsHeader(HttpHeaders.HOST)) {
            URIAuthority authority = request.getAuthority();
            if (authority == null) {
                if (ver.lessEquals(HttpVersion.HTTP_1_0))
                    return;
                throw new ProtocolException("Target host is unknown");
            }
            if (authority.getUserInfo() != null)
                authority = new URIAuthority(authority.getHostName(), authority.getPort());
            request.addHeader(HttpHeaders.HOST, authority);
        }
    }
}
