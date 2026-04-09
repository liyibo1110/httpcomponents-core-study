package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 一个自包含且可重复使用的实体，其内容来自文件。
 * @author liyibo
 * @date 2026-04-08 16:27
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class FileEntity extends AbstractHttpEntity {

    private final File file;

    public FileEntity(final File file, final ContentType contentType, final String contentEncoding) {
        super(contentType, contentEncoding);
        this.file = Args.notNull(file, "File");
    }

    public FileEntity(final File file, final ContentType contentType) {
        super(contentType, null);
        this.file = Args.notNull(file, "File");
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        return this.file.length();
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {}
}
