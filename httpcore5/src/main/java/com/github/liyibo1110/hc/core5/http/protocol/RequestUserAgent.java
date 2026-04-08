package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * RequestUserAgent负责添加User-Agent头部。
 * 建议在客户端协议处理器中使用此拦截器。
 * @author liyibo
 * @date 2026-04-07 16:02
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestUserAgent implements HttpRequestInterceptor {

    public static final HttpRequestInterceptor INSTANCE = new RequestUserAgent();

    private final String userAgent;

    public RequestUserAgent(final String userAgent) {
        super();
        this.userAgent = userAgent;
    }

    public RequestUserAgent() {
        this(null);
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (!request.containsHeader(HttpHeaders.USER_AGENT) && this.userAgent != null)
            request.addHeader(HttpHeaders.USER_AGENT, this.userAgent);
    }
}
