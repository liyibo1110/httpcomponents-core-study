package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 一种不进行任何转换即直接写入数据的输出流。内容实体的结尾通过关闭底层连接（EOF条件）来标记。使用此输出流传输的实体长度不受限制。
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，该流将被标记为已关闭，且不再允许进行任何输出。
 * @author liyibo
 * @date 2026-04-09 17:43
 */
public class IdentityOutputStream extends OutputStream {

    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;
    private boolean closed;

    public IdentityOutputStream(final SessionOutputBuffer buffer, final OutputStream outputStream) {
        super();
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.buffer.flush(this.outputStream);
        }
    }

    @Override
    public void flush() throws IOException {
        this.buffer.flush(this.outputStream);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        this.buffer.write(b, off, len, this.outputStream);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        this.buffer.write(b, this.outputStream);
    }
}
