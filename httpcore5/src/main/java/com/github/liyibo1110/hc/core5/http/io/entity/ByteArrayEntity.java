package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 一个自包含且可重复的实体，其内容来自一个字节数组。
 * @author liyibo
 * @date 2026-04-08 16:17
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class ByteArrayEntity extends AbstractHttpEntity {

    private final byte[] b;
    private final int off;
    private final int len;

    public ByteArrayEntity(final byte[] b, final int off, final int len,
                           final ContentType contentType, final String contentEncoding, final boolean chunked) {
        super(contentType, contentEncoding, chunked);
        Args.notNull(b, "Source byte array");
        Args.notNegative(off, "offset");
        Args.notNegative(len, "length");
        Args.notNegative(off + len, "off + len");
        Args.check(off <= b.length, "off %s cannot be greater then b.length %s ", off, b.length);
        Args.check(off + len <= b.length, "off + len  %s cannot be less then b.length %s ", off + len, b.length);
        this.b = b;
        this.off = off;
        this.len = len;
    }

    public ByteArrayEntity(final byte[] b, final int off, final int len, final ContentType contentType, final String contentEncoding) {
        this(b, off, len, contentType, contentEncoding, false);
    }

    public ByteArrayEntity(final byte[] b, final ContentType contentType, final String contentEncoding, final boolean chunked) {
        super(contentType, contentEncoding, chunked);
        Args.notNull(b, "Source byte array");
        this.b = b;
        this.off = 0;
        this.len = this.b.length;
    }

    public ByteArrayEntity(final byte[] b, final ContentType contentType, final String contentEncoding) {
        this(b, contentType, contentEncoding, false);
    }

    public ByteArrayEntity(final byte[] b, final ContentType contentType, final boolean chunked) {
        this(b, contentType, null, chunked);
    }

    public ByteArrayEntity(final byte[] b, final ContentType contentType) {
        this(b, contentType, null, false);
    }

    public ByteArrayEntity(final byte[] b, final int off, final int len, final ContentType contentType,  final boolean chunked) {
        this(b, off, len, contentType, null, chunked);
    }

    public ByteArrayEntity(final byte[] b, final int off, final int len, final ContentType contentType) {
        this(b, off, len, contentType, null, false);
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        return this.len;
    }

    @Override
    public final InputStream getContent() {
        return new ByteArrayInputStream(this.b, this.off, this.len);
    }

    @Override
    public final void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        outStream.write(this.b, this.off, this.len);
        outStream.flush();
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {}
}
