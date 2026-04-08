package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseInterceptor;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * ResponseServer负责添加Server头部。
 * 建议在服务器端协议处理器中使用此拦截器。
 * @author liyibo
 * @date 2026-04-07 16:10
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class ResponseServer implements HttpResponseInterceptor {

    private final String originServer;

    public ResponseServer(final String originServer) {
        super();
        this.originServer = originServer;
    }

    public ResponseServer() {
        this(null);
    }

    @Override
    public void process(final HttpResponse response, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (!response.containsHeader(HttpHeaders.SERVER) && this.originServer != null)
            response.addHeader(HttpHeaders.SERVER, this.originServer);
    }
}
