package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 用于传递ByteBuffer内容的实体。
 * @author liyibo
 * @date 2026-04-08 16:19
 */
public class ByteBufferEntity extends AbstractHttpEntity {

    private final ByteBuffer buffer;
    private final long length;

    public ByteBufferEntity(final ByteBuffer buffer, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        Args.notNull(buffer, "Source byte buffer");
        this.buffer = buffer;
        this.length = buffer.remaining();
    }

    public ByteBufferEntity(final ByteBuffer buffer, final ContentType contentType) {
        this(buffer, contentType, null);
    }

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final long getContentLength() {
        return length;
    }

    @Override
    public final InputStream getContent() throws IOException, UnsupportedOperationException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                if (!buffer.hasRemaining())
                    return -1;
                return buffer.get() & 0xFF;
            }

            @Override
            public int read(final byte[] bytes, final int off, final int len) throws IOException {
                if (!buffer.hasRemaining())
                    return -1;
                final int chunk = Math.min(len, buffer.remaining());
                buffer.get(bytes, off, chunk);
                return chunk;
            }
        };
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {}
}
