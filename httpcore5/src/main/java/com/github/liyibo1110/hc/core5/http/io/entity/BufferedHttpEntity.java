package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 一种包装entity，可在必要时对其内容进行缓冲。被缓冲的entity总是可重复的。
 *
 * 如果被包装的entity本身是可重复的，则调用会被直接传递。
 * 如果被包装的entity不可重复，则内容会被一次性读入缓冲区，并从该缓冲区按需提供。
 * @author liyibo
 * @date 2026-04-08 16:25
 */
public class BufferedHttpEntity extends HttpEntityWrapper {

    private final byte[] buffer;

    public BufferedHttpEntity(final HttpEntity entity) throws IOException {
        super(entity);
        if (!entity.isRepeatable() || entity.getContentLength() < 0) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);
            out.flush();
            this.buffer = out.toByteArray();
        } else {
            this.buffer = null;
        }
    }

    @Override
    public long getContentLength() {
        if (this.buffer != null)
            return this.buffer.length;
        return super.getContentLength();
    }

    @Override
    public InputStream getContent() throws IOException {
        if (this.buffer != null)
            return new ByteArrayInputStream(this.buffer);
        return super.getContent();
    }

    @Override
    public boolean isChunked() {
        return (buffer == null) && super.isChunked();
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        if (this.buffer != null)
            outStream.write(this.buffer);
        else
            super.writeTo(outStream);
    }

    @Override
    public boolean isStreaming() {
        return (buffer == null) && super.isStreaming();
    }
}
