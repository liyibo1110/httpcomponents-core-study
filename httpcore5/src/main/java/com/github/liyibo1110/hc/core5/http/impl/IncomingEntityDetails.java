package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.MessageHeaders;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Collections;
import java.util.Set;

/**
 * 附带MessageHeaders的EntityDetails实现类。
 * @author liyibo
 * @date 2026-04-09 12:06
 */
@Internal
public class IncomingEntityDetails implements EntityDetails {

    private final MessageHeaders message;
    private final long contentLength;

    public IncomingEntityDetails(final MessageHeaders message, final long contentLength) {
        this.message = Args.notNull(message, "Message");
        this.contentLength = contentLength;
    }

    public IncomingEntityDetails(final MessageHeaders message) {
        this(message, -1);
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        final Header h = message.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        return h != null ? h.getValue() : null;
    }

    @Override
    public String getContentEncoding() {
        final Header h = message.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
        return h != null ? h.getValue() : null;
    }

    @Override
    public boolean isChunked() {
        return contentLength < 0;
    }

    @Override
    public Set<String> getTrailerNames() {
        final Header h = message.getFirstHeader(HttpHeaders.TRAILER);
        if (h == null)
            return Collections.emptySet();
        return MessageSupport.parseTokens(h);
    }
}
