package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 一个从Serializable中获取内容的流式实体。
 * @author liyibo
 * @date 2026-04-08 16:31
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class SerializableEntity extends AbstractHttpEntity {

    private final Serializable serializable;

    public SerializableEntity(final Serializable serializable, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        this.serializable = Args.notNull(serializable, "Source object");
    }

    public SerializableEntity(final Serializable serializable, final ContentType contentType) {
        this(serializable, contentType, null);
    }

    @Override
    public final InputStream getContent() throws IOException, IllegalStateException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public final long getContentLength() {
        return -1;
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        final ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(this.serializable);
        out.flush();
    }

    @Override
    public final void close() throws IOException {}
}
