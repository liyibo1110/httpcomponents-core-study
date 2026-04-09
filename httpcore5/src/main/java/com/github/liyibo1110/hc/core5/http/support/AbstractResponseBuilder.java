package com.github.liyibo1110.hc.core5.http.support;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;

/**
 * 创建BasicHttpResponse对象的工厂。
 * @author liyibo
 * @date 2026-04-08 11:44
 */
public abstract class AbstractResponseBuilder<T> extends AbstractMessageBuilder<T> {

    private int status;

    protected AbstractResponseBuilder(final int status) {
        super();
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    @Override
    public AbstractResponseBuilder<T> setVersion(final ProtocolVersion version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> setHeaders(final Header... headers) {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> addHeader(final Header header) {
        super.addHeader(header);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> addHeader(final String name, final String value) {
        super.addHeader(name, value);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> removeHeader(final Header header) {
        super.removeHeader(header);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> removeHeaders(final String name) {
        super.removeHeaders(name);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> setHeader(final Header header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public AbstractResponseBuilder<T> setHeader(final String name, final String value) {
        super.setHeader(name, value);
        return this;
    }

    @Override
    protected abstract T build();
}
