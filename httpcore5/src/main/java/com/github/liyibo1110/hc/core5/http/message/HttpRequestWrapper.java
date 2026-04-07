package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.net.URIAuthority;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * HttpRequest的wrapper组件。
 * @author liyibo
 * @date 2026-04-07 11:19
 */
public class HttpRequestWrapper extends AbstractMessageWrapper<HttpRequest> implements HttpRequest {

    public HttpRequestWrapper(final HttpRequest message) {
        super(message);
    }

    @Override
    public String getMethod() {
        return getMessage().getMethod();
    }

    @Override
    public String getPath() {
        return getMessage().getPath();
    }

    @Override
    public void setPath(final String path) {
        getMessage().setPath(path);
    }

    @Override
    public String getScheme() {
        return getMessage().getScheme();
    }

    @Override
    public void setScheme(final String scheme) {
        getMessage().setScheme(scheme);
    }

    @Override
    public URIAuthority getAuthority() {
        return getMessage().getAuthority();
    }

    @Override
    public void setAuthority(final URIAuthority authority) {
        getMessage().setAuthority(authority);
    }

    @Override
    public String getRequestUri() {
        return getMessage().getRequestUri();
    }

    @Override
    public URI getUri() throws URISyntaxException {
        return getMessage().getUri();
    }

    @Override
    public void setUri(final URI requestUri) {
        getMessage().setUri(requestUri);
    }
}
