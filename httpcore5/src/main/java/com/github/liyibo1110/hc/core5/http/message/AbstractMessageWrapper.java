package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Iterator;

/**
 * HttpMessage的wrapper组件。
 * @author liyibo
 * @date 2026-04-07 11:16
 */
public abstract class AbstractMessageWrapper<T extends HttpMessage> implements HttpMessage {

    /** HttpMessage本体 */
    private final T message;

    public AbstractMessageWrapper(final T message) {
        this.message = Args.notNull(message, "Message");
    }

    @Override
    public void setVersion(final ProtocolVersion version) {
        message.setVersion(version);
    }

    @Override
    public ProtocolVersion getVersion() {
        return message.getVersion();
    }

    @Override
    public void addHeader(final Header header) {
        message.addHeader(header);
    }

    @Override
    public void addHeader(final String name, final Object value) {
        message.addHeader(name, value);
    }

    @Override
    public void setHeader(final Header header) {
        message.setHeader(header);
    }

    @Override
    public void setHeader(final String name, final Object value) {
        message.setHeader(name, value);
    }

    @Override
    public void setHeaders(final Header... headers) {
        message.setHeaders(headers);
    }

    @Override
    public boolean removeHeader(final Header header) {
        return message.removeHeader(header);
    }

    @Override
    public boolean removeHeaders(final String name) {
        return message.removeHeaders(name);
    }

    @Override
    public boolean containsHeader(final String name) {
        return message.containsHeader(name);
    }

    @Override
    public int countHeaders(final String name) {
        return message.countHeaders(name);
    }

    @Override
    public Header[] getHeaders(final String name) {
        return message.getHeaders(name);
    }

    @Override
    public Header getHeader(final String name) throws ProtocolException {
        return message.getHeader(name);
    }

    @Override
    public Header getFirstHeader(final String name) {
        return message.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(final String name) {
        return message.getLastHeader(name);
    }

    T getMessage() {
        return message;
    }

    @Override
    public Header[] getHeaders() {
        return message.getHeaders();
    }

    @Override
    public Iterator<Header> headerIterator() {
        return message.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(final String name) {
        return message.headerIterator(name);
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
