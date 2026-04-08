package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * ResponseContent是处理传出响应最重要的拦截器。
 * 它负责根据所包含实体的属性和协议版本，通过添加Content-Length或Transfer-Content头部来限定内容长度。
 * 该拦截器对于服务器端协议处理器的正常运行是必不可少的。
 * @author liyibo
 * @date 2026-04-07 16:08
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class ResponseContent implements HttpResponseInterceptor {

    private final boolean overwrite;

    public ResponseContent() {
        this(false);
    }

    public ResponseContent(final boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    @Override
    public void process(final HttpResponse response, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (this.overwrite) {
            response.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
            response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
        } else {
            if (response.containsHeader(HttpHeaders.TRANSFER_ENCODING))
                throw new ProtocolException("Transfer-encoding header already present");
            if (response.containsHeader(HttpHeaders.CONTENT_LENGTH))
                throw new ProtocolException("Content-Length header already present");
        }
        final ProtocolVersion ver = context.getProtocolVersion();
        if (entity != null) {
            final long len = entity.getContentLength();
            if (len >= 0 && !entity.isChunked()) {
                response.addHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(entity.getContentLength()));
            } else if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                response.addHeader(HttpHeaders.TRANSFER_ENCODING, HeaderElements.CHUNKED_ENCODING);
                MessageSupport.addTrailerHeader(response, entity);
            }
            MessageSupport.addContentTypeHeader(response, entity);
            MessageSupport.addContentEncodingHeader(response, entity);
        } else {
            final int status = response.getCode();
            if (status != HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_NOT_MODIFIED)
                response.addHeader(HttpHeaders.CONTENT_LENGTH, "0");
        }
    }
}
