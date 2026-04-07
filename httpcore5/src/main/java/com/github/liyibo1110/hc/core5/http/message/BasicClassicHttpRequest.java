package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.HttpHost;
import com.github.liyibo1110.hc.core5.http.Method;
import com.github.liyibo1110.hc.core5.net.URIAuthority;

import java.net.URI;

/**
 * ClassicHttpRequest的基础实现类。
 * @author liyibo
 * @date 2026-04-07 13:34
 */
public class BasicClassicHttpRequest extends BasicHttpRequest implements ClassicHttpRequest {
    private static final long serialVersionUID = 1L;

    private HttpEntity entity;

    public BasicClassicHttpRequest(final String method, final String scheme, final URIAuthority authority, final String path) {
        super(method, scheme, authority, path);
    }

    public BasicClassicHttpRequest(final String method, final String path) {
        super(method, path);
    }

    public BasicClassicHttpRequest(final String method, final HttpHost host, final String path) {
        super(method, host, path);
    }

    public BasicClassicHttpRequest(final String method, final URI requestUri) {
        super(method, requestUri);
    }

    public BasicClassicHttpRequest(final Method method, final String path) {
        super(method, path);
    }

    public BasicClassicHttpRequest(final Method method, final HttpHost host, final String path) {
        super(method, host, path);
    }

    public BasicClassicHttpRequest(final Method method, final URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }
}
