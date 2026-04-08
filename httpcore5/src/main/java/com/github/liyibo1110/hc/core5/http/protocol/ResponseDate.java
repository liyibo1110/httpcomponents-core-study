package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * ResponseDate负责在发出的响应中添加Date头部。
 * 建议在服务器端协议处理器中使用此拦截器。
 * @author liyibo
 * @date 2026-04-07 16:09
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class ResponseDate implements HttpResponseInterceptor {

    public ResponseDate() {
        super();
    }

    @Override
    public void process(final HttpResponse response, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        final int status = response.getCode();
        if ((status >= HttpStatus.SC_OK) && !response.containsHeader(HttpHeaders.DATE))
            response.setHeader(HttpHeaders.DATE, HttpDateGenerator.INSTANCE.getCurrentDate());
    }
}
