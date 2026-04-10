package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.http.EntityDetails;

import java.util.Set;

/**
 * EntityDetails接口的基础实现。
 * @author liyibo
 * @date 2026-04-09 10:37
 */
public final class BasicEntityDetails implements EntityDetails {

    private final long len;
    private final ContentType contentType;

    public BasicEntityDetails(final long len, final ContentType contentType) {
        this.len = len;
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return len;
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }
}
