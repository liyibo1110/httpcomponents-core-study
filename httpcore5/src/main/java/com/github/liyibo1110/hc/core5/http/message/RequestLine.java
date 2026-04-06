package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.Serializable;

/**
 * request请求首行
 * @author liyibo
 * @date 2026-04-06 13:12
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestLine implements Serializable {
    private static final long serialVersionUID = 2810581718468737193L;

    private final ProtocolVersion protoversion;
    private final String method;
    private final String uri;

    public RequestLine(final HttpRequest request) {
        super();
        Args.notNull(request, "Request");
        this.method = request.getMethod();
        this.uri = request.getRequestUri();
        this.protoversion = request.getVersion() != null ? request.getVersion() : HttpVersion.HTTP_1_1;
    }

    public RequestLine(final String method, final String uri, final ProtocolVersion version) {
        super();
        this.method = Args.notNull(method, "Method");
        this.uri = Args.notNull(uri, "URI");
        this.protoversion = version != null ? version : HttpVersion.HTTP_1_1;
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoversion;
    }

    public String getUri() {
        return this.uri;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.method).append(" ").append(this.uri).append(" ").append(this.protoversion);
        return buf.toString();
    }
}
