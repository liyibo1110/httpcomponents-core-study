package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.Method;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * RequestConnControl负责在发出的请求中添加Connection头部，这对管理HTTP/1.0连接的持久性至关重要。
 * 建议在客户端协议处理器中使用此拦截器。
 * @author liyibo
 * @date 2026-04-07 15:41
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestConnControl implements HttpRequestInterceptor {

    public static final HttpRequestInterceptor INSTANCE = new RequestConnControl();

    public RequestConnControl() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");

        final String method = request.getMethod();
        if (Method.CONNECT.isSame(method))
            return;

        if (!request.containsHeader(HttpHeaders.CONNECTION)) {
            if (request.containsHeader(HttpHeaders.UPGRADE))
                request.addHeader(HttpHeaders.CONNECTION, HeaderElements.UPGRADE);
            else
                request.addHeader(HttpHeaders.CONNECTION, HeaderElements.KEEP_ALIVE);
        }
    }
}
