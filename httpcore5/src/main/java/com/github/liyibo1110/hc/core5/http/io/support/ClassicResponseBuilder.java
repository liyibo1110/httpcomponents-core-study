package com.github.liyibo1110.hc.core5.http.io.support;

import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.io.entity.ByteArrayEntity;
import com.github.liyibo1110.hc.core5.http.io.entity.StringEntity;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.support.AbstractResponseBuilder;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Arrays;

/**
 * ClassicHttpResponse实例的builder。
 * @author liyibo
 * @date 2026-04-08 17:21
 */
public class ClassicResponseBuilder extends AbstractResponseBuilder<ClassicHttpResponse> {

    private HttpEntity entity;

    ClassicResponseBuilder(final int status) {
        super(status);
    }

    public static ClassicResponseBuilder create(final int status) {
        Args.checkRange(status, 100, 599, "HTTP status code");
        return new ClassicResponseBuilder(status);
    }

    public static ClassicResponseBuilder copy(final ClassicHttpResponse response) {
        Args.notNull(response, "HTTP response");
        final ClassicResponseBuilder builder = new ClassicResponseBuilder(response.getCode());
        builder.digest(response);
        return builder;
    }

    protected void digest(final ClassicHttpResponse response) {
        super.digest(response);
        setEntity(response.getEntity());
    }

    @Override
    public ClassicResponseBuilder setVersion(final ProtocolVersion version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public ClassicResponseBuilder setHeaders(final Header... headers) {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public ClassicResponseBuilder addHeader(final Header header) {
        super.addHeader(header);
        return this;
    }

    @Override
    public ClassicResponseBuilder addHeader(final String name, final    String value) {
        super.addHeader(name, value);
        return this;
    }

    @Override
    public ClassicResponseBuilder removeHeader(final Header header) {
        super.removeHeader(header);
        return this;
    }

    @Override
    public ClassicResponseBuilder removeHeaders(final String name) {
        super.removeHeaders(name);
        return this;
    }

    @Override
    public ClassicResponseBuilder setHeader(final Header header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public ClassicResponseBuilder setHeader(final String name, final String value) {
        super.setHeader(name, value);
        return this;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public ClassicResponseBuilder setEntity(final HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public ClassicResponseBuilder setEntity(final String content, final ContentType contentType) {
        this.entity = new StringEntity(content, contentType);
        return this;
    }

    public ClassicResponseBuilder setEntity(final String content) {
        this.entity = new StringEntity(content);
        return this;
    }

    public ClassicResponseBuilder setEntity(final byte[] content, final ContentType contentType) {
        this.entity = new ByteArrayEntity(content, contentType);
        return this;
    }

    @Override
    public ClassicHttpResponse build() {
        final BasicClassicHttpResponse result = new BasicClassicHttpResponse(getStatus());
        result.setVersion(getVersion());
        result.setHeaders(getHeaders());
        result.setEntity(entity);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ClassicResponseBuilder [status=");
        builder.append(getStatus());
        builder.append(", headerGroup=");
        builder.append(Arrays.toString(getHeaders()));
        builder.append(", entity=");
        builder.append(entity != null ? entity.getClass() : null);
        builder.append("]");
        return builder.toString();
    }
}
