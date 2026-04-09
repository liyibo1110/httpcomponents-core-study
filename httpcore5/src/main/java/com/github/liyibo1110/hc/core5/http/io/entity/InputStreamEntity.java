package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 一种流式、不可重复的实体，其内容来自InputStream。
 *
 * 注：和BasicHttpEntity某些方法实现不太一样。
 * @author liyibo
 * @date 2026-04-08 16:21
 */
public class InputStreamEntity extends AbstractHttpEntity {

    private final InputStream content;
    private final long length;

    public InputStreamEntity(final InputStream inStream, final long length, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        this.content = Args.notNull(inStream, "Source input stream");
        this.length = length;
    }

    public InputStreamEntity(final InputStream inStream, final long length, final ContentType contentType) {
        this(inStream, length, contentType, null);
    }

    public InputStreamEntity(final InputStream inStream, final ContentType contentType) {
        this(inStream, -1, contentType, null);
    }

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final long getContentLength() {
        return this.length;
    }

    @Override
    public final InputStream getContent() throws IOException {
        return this.content;
    }

    @Override
    public final void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        try (final InputStream is = this.content) {
            final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
            int readLen;
            if (this.length < 0) {
                // consume until EOF
                while ((readLen = is.read(buffer)) != -1)
                    outStream.write(buffer, 0, readLen);
            } else {
                // consume no more than length
                long remaining = this.length;
                while (remaining > 0) {
                    readLen = is.read(buffer, 0, (int) Math.min(OUTPUT_BUFFER_SIZE, remaining));
                    if (readLen == -1)
                        break;
                    outStream.write(buffer, 0, readLen);
                    remaining -= readLen;
                }
            }
        }
    }

    @Override
    public final boolean isStreaming() {
        return true;
    }

    @Override
    public final void close() throws IOException {
        content.close();
    }
}
