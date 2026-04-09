package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 一个自包含且可重复的实体，其内容来自一个字符串。
 * @author liyibo
 * @date 2026-04-08 16:30
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class StringEntity extends AbstractHttpEntity {

    private final byte[] content;

    public StringEntity(final String string, final ContentType contentType, final String contentEncoding, final boolean chunked) {
        super(contentType, contentEncoding, chunked);
        Args.notNull(string, "Source string");
        final Charset charset = ContentType.getCharset(contentType, StandardCharsets.ISO_8859_1);
        this.content = string.getBytes(charset);
    }

    public StringEntity(final String string, final ContentType contentType, final boolean chunked) {
        this(string, contentType, null, chunked);
    }

    public StringEntity(final String string, final ContentType contentType) {
        this(string, contentType, null, false);
    }

    public StringEntity(final String string, final Charset charset) {
        this(string, ContentType.TEXT_PLAIN.withCharset(charset));
    }

    public StringEntity(final String string, final Charset charset, final boolean chunked) {
        this(string, ContentType.TEXT_PLAIN.withCharset(charset), chunked);
    }

    public StringEntity(final String string) {
        this(string, ContentType.DEFAULT_TEXT);
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        return this.content.length;
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public final void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        outStream.write(this.content);
        outStream.flush();
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {}
}
