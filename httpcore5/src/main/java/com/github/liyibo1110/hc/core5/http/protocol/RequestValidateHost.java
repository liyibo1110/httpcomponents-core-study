package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.net.URIAuthority;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * RequestValidateHost负责将Host标头值复制到传入消息的HttpRequest.setAuthority(URIAuthority)中。此拦截器是服务器端协议处理器所必需的。
 * @author liyibo
 * @date 2026-04-07 16:03
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestValidateHost implements HttpRequestInterceptor {

    public RequestValidateHost() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");

        final Header header = request.getHeader(HttpHeaders.HOST);
        if (header != null) {
            final URIAuthority authority;
            try {
                authority = URIAuthority.create(header.getValue());
            } catch (URISyntaxException e) {
                throw new ProtocolException(e.getMessage(), e);
            }
            request.setAuthority(authority);
        } else {
            final ProtocolVersion version = request.getVersion() != null ? request.getVersion() : HttpVersion.HTTP_1_1;
            if (version.greaterEquals(HttpVersion.HTTP_1_1))
                throw new ProtocolException("Host header is absent");
        }
    }
}
