package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ConnectionClosedException;
import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一种在读取指定字节数后自动截断的输入流。该类用于接收HTTP消息的内容，其中内容实体的结尾由Content-Length标头的值决定。
 * 通过此流传输的实体长度最多可达Long.MAX_VALUE。
 *
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，它会在关闭时读取至其限制的“末尾”，这使得后续的 HTTP1.1请求能够无缝执行，同时无需客户端记住读取响应的全部内容。
 *
 * 本质就是个装饰器模式。
 * @author liyibo
 * @date 2026-04-09 16:32
 */
public class ContentLengthInputStream extends InputStream {

    private static final int BUFFER_SIZE = 2048;

    private final SessionInputBuffer buffer;
    private final InputStream inputStream;

    private final long contentLength;
    private long pos;
    private boolean closed;

    public ContentLengthInputStream(final SessionInputBuffer buffer, final InputStream inputStream, final long contentLength) {
        super();
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
        this.contentLength = Args.notNegative(contentLength, "Content length");
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                if (pos < contentLength) {
                    final byte[] buffer = new byte[BUFFER_SIZE];
                    while (read(buffer) >= 0) {
                        // keep reading
                    }
                }
            } finally {
                // close after above so that we don't throw an exception trying
                // to read after closed!
                closed = true;
            }
        }
    }

    @Override
    public int available() throws IOException {
        final int len = this.buffer.length();
        return Math.min(len, (int) (this.contentLength - this.pos));
    }

    @Override
    public int read() throws IOException {
        if (closed)
            throw new StreamClosedException();

        if (pos >= contentLength)
            return -1;
        final int b = this.buffer.read(this.inputStream);
        if (b == -1) {
            if (pos < contentLength) {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: %d; received: %d)",
                        contentLength, pos);
            }
        } else {
            pos++;
        }
        return b;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws java.io.IOException {
        if (closed)
            throw new StreamClosedException();

        if (pos >= contentLength)
            return -1;

        int chunk = len;
        if (pos + len > contentLength)
            chunk = (int) (contentLength - pos);
        final int count = this.buffer.read(b, off, chunk, this.inputStream);
        if (count == -1 && pos < contentLength) {
            throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: %d; received: %d)",
                    contentLength, pos);
        }
        if (count > 0)
            pos += count;
        return count;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(final long n) throws IOException {
        if (n <= 0)
            return 0;
        final byte[] buffer = new byte[BUFFER_SIZE];
        // make sure we don't skip more bytes than are
        // still available
        long remaining = Math.min(n, this.contentLength - this.pos);
        // skip and keep track of the bytes actually skipped
        long count = 0;
        while (remaining > 0) {
            final int readLen = read(buffer, 0, (int)Math.min(BUFFER_SIZE, remaining));
            if (readLen == -1)
                break;
            count += readLen;
            remaining -= readLen;
        }
        return count;
    }
}
