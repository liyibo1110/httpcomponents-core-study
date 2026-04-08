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
 * RequestDate拦截器负责在发出的请求中添加Date头部。
 * 对于客户端协议处理器而言，此拦截器是可选的。
 * @author liyibo
 * @date 2026-04-07 15:54
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class RequestDate implements HttpRequestInterceptor {

    public static final HttpRequestInterceptor INSTANCE = new RequestDate();

    public RequestDate() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (entity != null && !request.containsHeader(HttpHeaders.DATE))
            request.setHeader(HttpHeaders.DATE, HttpDateGenerator.INSTANCE.getCurrentDate());
    }
}
