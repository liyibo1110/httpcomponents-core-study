package com.github.liyibo1110.hc.core5.http.support;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.BasicHttpResponse;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Arrays;

/**
 * 创建BasicHttpResponse对象的工厂。
 * @author liyibo
 * @date 2026-04-08 13:03
 */
public class BasicResponseBuilder extends AbstractResponseBuilder {

    protected BasicResponseBuilder(final int status) {
        super(status);
    }

    public static BasicResponseBuilder create(final int status) {
        Args.checkRange(status, 100, 599, "HTTP status code");
        return new BasicResponseBuilder(status);
    }

    public static BasicResponseBuilder copy(final HttpResponse response) {
        Args.notNull(response, "HTTP response");
        final BasicResponseBuilder builder = new BasicResponseBuilder(response.getCode());
        builder.digest(response);
        return builder;
    }

    @Override
    public BasicResponseBuilder setVersion(final ProtocolVersion version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public BasicResponseBuilder setHeaders(final Header... headers) {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public BasicResponseBuilder addHeader(final Header header) {
        super.addHeader(header);
        return this;
    }

    @Override
    public BasicResponseBuilder addHeader(final String name, final String value) {
        super.addHeader(name, value);
        return this;
    }

    @Override
    public BasicResponseBuilder removeHeader(final Header header) {
        super.removeHeader(header);
        return this;
    }

    @Override
    public BasicResponseBuilder removeHeaders(final String name) {
        super.removeHeaders(name);
        return this;
    }

    @Override
    public BasicResponseBuilder setHeader(final Header header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public BasicResponseBuilder setHeader(final String name, final String value) {
        super.setHeader(name, value);
        return this;
    }

    @Override
    public BasicHttpResponse build() {
        final BasicHttpResponse result = new BasicHttpResponse(getStatus());
        result.setVersion(getVersion());
        result.setHeaders(getHeaders());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BasicResponseBuilder [status=");
        builder.append(getStatus());
        builder.append(", headerGroup=");
        builder.append(Arrays.toString(getHeaders()));
        builder.append("]");
        return builder.toString();
    }
}
