package com.github.liyibo1110.hc.core5.http.support;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.BasicHeader;
import com.github.liyibo1110.hc.core5.http.message.HeaderGroup;

import java.util.Iterator;

/**
 * 创建HttpMessage对象的工厂。
 * @author liyibo
 * @date 2026-04-08 10:33
 */
public abstract class AbstractMessageBuilder<T> {

    private ProtocolVersion version;
    private HeaderGroup headerGroup;

    protected AbstractMessageBuilder() {}

    protected void digest(final HttpMessage message) {
        if (message == null)
            return;
        setVersion(message.getVersion());
        setHeaders(message.headerIterator());
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public AbstractMessageBuilder<T> setVersion(final ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public Header[] getHeaders() {
        return headerGroup != null ? headerGroup.getHeaders() : null;
    }

    public Header[] getHeaders(final String name) {
        return headerGroup != null ? headerGroup.getHeaders(name) : null;
    }

    public AbstractMessageBuilder<T> setHeaders(final Header... headers) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.setHeaders(headers);
        return this;
    }

    public AbstractMessageBuilder<T> setHeaders(final Iterator<Header> it) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        else
            headerGroup.clear();

        while (it.hasNext())
            headerGroup.addHeader(it.next());
        return this;
    }

    public Header[] getFirstHeaders() {
        return headerGroup != null ? headerGroup.getHeaders() : null;
    }

    public Header getFirstHeader(final String name) {
        return headerGroup != null ? headerGroup.getFirstHeader(name) : null;
    }

    public Header getLastHeader(final String name) {
        return headerGroup != null ? headerGroup.getLastHeader(name) : null;
    }

    public AbstractMessageBuilder<T> addHeader(final Header header) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.addHeader(header);
        return this;
    }

    public AbstractMessageBuilder<T> addHeader(final String name, final String value) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public AbstractMessageBuilder<T> removeHeader(final Header header) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.removeHeader(header);
        return this;
    }

    public AbstractMessageBuilder<T> removeHeaders(final String name) {
        if (name == null || headerGroup == null)
            return this;
        for (final Iterator<Header> i = headerGroup.headerIterator(); i.hasNext(); ) {
            final Header header = i.next();
            if (name.equalsIgnoreCase(header.getName()))
                i.remove();
        }
        return this;
    }

    public AbstractMessageBuilder<T> setHeader(final Header header) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.setHeader(header);
        return this;
    }

    public AbstractMessageBuilder<T> setHeader(final String name, final String value) {
        if (headerGroup == null)
            headerGroup = new HeaderGroup();
        headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    protected abstract T build();
}
