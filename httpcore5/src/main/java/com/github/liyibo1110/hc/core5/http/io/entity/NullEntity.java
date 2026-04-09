package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 一个没有内容类型的空entity。
 * 出于方便起见，可以使用此类型来代替空的ByteArrayEntity。
 * @author liyibo
 * @date 2026-04-08 16:29
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class NullEntity implements HttpEntity {

    public static final NullEntity INSTANCE = new NullEntity();

    private NullEntity() {}

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return EmptyInputStream.INSTANCE;
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {}

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return Collections.emptySet();
    }
}
