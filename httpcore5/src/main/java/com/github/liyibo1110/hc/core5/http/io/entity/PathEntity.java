package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 一个自包含且可重复的实体，其内容来自指定Path（即nio2）。
 * @author liyibo
 * @date 2026-04-08 16:33
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class PathEntity extends AbstractHttpEntity {

    private final Path path;

    public PathEntity(final Path path, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        this.path = Args.notNull(path, "Path");
    }

    public PathEntity(final Path path, final ContentType contentType) {
        super(contentType, null);
        this.path = Args.notNull(path, "Path");
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        try {
            return Files.size(this.path);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final InputStream getContent() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {}
}
