package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * RequestExpectContinue负责通过添加Expect头部来启用expect-continue握手。
 * 建议在客户端协议处理器中使用此拦截器。
 * @author liyibo
 * @date 2026-04-07 15:58
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestExpectContinue implements HttpRequestInterceptor {

    public static final RequestExpectContinue INSTANCE = new RequestExpectContinue();

    public RequestExpectContinue() {
        super();
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");

        if (!request.containsHeader(HttpHeaders.EXPECT)) {
            if (entity != null) {
                final ProtocolVersion ver = context.getProtocolVersion();
                if (entity.getContentLength() != 0 && !ver.lessEquals(HttpVersion.HTTP_1_0))
                    request.addHeader(HttpHeaders.EXPECT, HeaderElements.CONTINUE);
            }
        }
    }
}
