package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.io.entity.AbstractHttpEntity;
import com.github.liyibo1110.hc.core5.http.io.entity.EmptyInputStream;
import com.github.liyibo1110.hc.core5.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author liyibo
 * @date 2026-04-09 18:04
 */
class IncomingHttpEntity implements HttpEntity {

    private final InputStream content;
    private final long len;
    private final boolean chunked;
    private final Header contentType;
    private final Header contentEncoding;

    IncomingHttpEntity(final InputStream content, final long len, final boolean chunked, final Header contentType, final Header contentEncoding) {
        this.content = content;
        this.len = len;
        this.chunked = chunked;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return chunked;
    }

    @Override
    public long getContentLength() {
        return len;
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType.getValue() : null;
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding != null ? contentEncoding.getValue() : null;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return content;
    }

    @Override
    public boolean isStreaming() {
        return content != null && content != EmptyInputStream.INSTANCE;
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        AbstractHttpEntity.writeTo(this, outStream);
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return Collections.emptySet();
    }

    @Override
    public void close() throws IOException {
        Closer.close(content);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("Content-Type: ");
        sb.append(getContentType());
        sb.append(',');
        sb.append("Content-Encoding: ");
        sb.append(getContentEncoding());
        sb.append(',');
        final long len = getContentLength();
        if (len >= 0) {
            sb.append("Content-Length: ");
            sb.append(len);
            sb.append(',');
        }
        sb.append("Chunked: ");
        sb.append(isChunked());
        sb.append(']');
        return sb.toString();
    }
}
