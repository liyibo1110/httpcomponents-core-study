package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 一种在达到指定字节数后停止输出的流。该类用于发送HTTP消息的内容，其中内容实体的结尾由Content-Length标头的值决定。
 * 通过此流传输的实体长度最多为Long.MAX_VALUE。
 *
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，该流将被标记为已关闭，且不再允许进行任何输出。
 *
 * 本质就是个装饰器模式。
 * @author liyibo
 * @date 2026-04-09 15:57
 */
public class ContentLengthOutputStream extends OutputStream {

    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;
    private final long contentLength;
    private long total;
    private boolean closed;

    public ContentLengthOutputStream(final SessionOutputBuffer buffer, final OutputStream outputStream, final long contentLength) {
        super();
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
        this.contentLength = Args.notNegative(contentLength, "Content length");
    }

    /**
     * 只标记，不真的关闭底层输出流。
     */
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
        if (this.total < this.contentLength) {
            final long max = this.contentLength - this.total;
            int chunk = len;
            if (chunk > max)
                chunk = (int) max;
            this.buffer.write(b, off, chunk, this.outputStream);
            this.total += chunk;
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        if (this.total < this.contentLength) {
            this.buffer.write(b, this.outputStream);
            this.total++;
        }
    }
}
