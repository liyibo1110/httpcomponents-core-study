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
import com.github.liyibo1110.hc.core5.http.Method;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;

/**
 * RequestContent是处理出站请求最重要的拦截器。
 * 它负责根据所包含实体的属性和协议版本，通过添加Content-Length或Transfer-Content头部来限定内容长度。
 * 该拦截器是客户端协议处理器正常运行的必要条件。
 * @author liyibo
 * @date 2026-04-07 15:48
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestContent implements HttpRequestInterceptor {

    public static final HttpRequestInterceptor INSTANCE = new RequestContent();

    private final boolean overwrite;

    public RequestContent() {
        this(false);
    }

    public RequestContent(final boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        final String method = request.getMethod();
        if (Method.TRACE.isSame(method) && entity != null)
            throw new ProtocolException("TRACE request may not enclose an entity");

        if (this.overwrite) {
            request.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
            request.removeHeaders(HttpHeaders.CONTENT_LENGTH);
        } else {
            if (request.containsHeader(HttpHeaders.TRANSFER_ENCODING))
                throw new ProtocolException("Transfer-encoding header already present");
            if (request.containsHeader(HttpHeaders.CONTENT_LENGTH))
                throw new ProtocolException("Content-Length header already present");
        }

        if (entity != null) {
            final ProtocolVersion ver = context.getProtocolVersion();
            if (entity.isChunked() || entity.getContentLength() < 0) {
                if (ver.lessEquals(HttpVersion.HTTP_1_0))
                    throw new ProtocolException("Chunked transfer encoding not allowed for " + ver);
                request.addHeader(HttpHeaders.TRANSFER_ENCODING, HeaderElements.CHUNKED_ENCODING);
                MessageSupport.addTrailerHeader(request, entity);
            } else {
                // 主要任务就是设置这个content-length
                request.addHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(entity.getContentLength()));
            }
            MessageSupport.addContentTypeHeader(request, entity);
            MessageSupport.addContentEncodingHeader(request, entity);
        }
    }
}
