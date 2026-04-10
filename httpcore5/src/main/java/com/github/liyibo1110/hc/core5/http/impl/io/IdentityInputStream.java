package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一种不进行任何转换的读取数据输入流。内容实体的结尾通过关闭底层连接来标记（EOF条件）。通过此输入流传输的实体长度不受限制。
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，该流将被标记为已关闭，且不再允许进行进一步读取。
 * @author liyibo
 * @date 2026-04-09 17:44
 */
public class IdentityInputStream extends InputStream {

    private final SessionInputBuffer buffer;
    private final InputStream inputStream;
    private boolean closed;

    public IdentityInputStream(final SessionInputBuffer buffer, final InputStream inputStream) {
        super();
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
    }

    @Override
    public int available() throws IOException {
        if (this.closed)
            return 0;
        final int n = this.buffer.length();
        return n > 0 ? n : this.inputStream.available();
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public int read() throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        return this.buffer.read(this.inputStream);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        return this.buffer.read(b, off, len, this.inputStream);
    }
}
