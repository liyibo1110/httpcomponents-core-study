package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.io.IOCallback;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 将内容生成过程委托给IOCallback的实体，其中OutputStream作为输出接收器。
 * @author liyibo
 * @date 2026-04-08 16:37
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public final class EntityTemplate extends AbstractHttpEntity {

    private final long contentLength;
    private final IOCallback<OutputStream> callback;

    public EntityTemplate(final long contentLength, final ContentType contentType, final String contentEncoding,
                          final IOCallback<OutputStream> callback) {
        super(contentType, contentEncoding);
        this.contentLength = contentLength;
        this.callback = Args.notNull(callback, "I/O callback");
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        this.callback.execute(outStream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void close() throws IOException {}
}
