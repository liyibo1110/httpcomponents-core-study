package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * 用于封装entity的基类，该类将所有调用委托给被封装的entity。
 * 具体实现类可以从该类派生，并仅重写那些不应委托给被封装实体的方法。
 * @author liyibo
 * @date 2026-04-08 16:24
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpEntityWrapper implements HttpEntity {

    private final HttpEntity wrappedEntity;

    public HttpEntityWrapper(final HttpEntity wrappedEntity) {
        super();
        this.wrappedEntity = Args.notNull(wrappedEntity, "Wrapped entity");
    }

    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return wrappedEntity.isChunked();
    }

    @Override
    public long getContentLength() {
        return wrappedEntity.getContentLength();
    }

    @Override
    public String getContentType() {
        return wrappedEntity.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return wrappedEntity.getContentEncoding();
    }

    @Override
    public InputStream getContent()
            throws IOException {
        return wrappedEntity.getContent();
    }

    @Override
    public void writeTo(final OutputStream outStream)
            throws IOException {
        wrappedEntity.writeTo(outStream);
    }

    @Override
    public boolean isStreaming() {
        return wrappedEntity.isStreaming();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return wrappedEntity.getTrailers();
    }

    @Override
    public Set<String> getTrailerNames() {
        return wrappedEntity.getTrailerNames();
    }

    @Override
    public void close() throws IOException {
        wrappedEntity.close();
    }

    @Override
    public String toString() {
        return "Wrapper [" + wrappedEntity + "]";
    }
}
