package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.Chars;
import com.github.liyibo1110.hc.core5.http.impl.BasicHttpTransportMetrics;
import com.github.liyibo1110.hc.core5.http.io.HttpTransportMetrics;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.ByteArrayBuffer;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * 这是一个用于session输出缓冲区的抽象基类，可将数据流式传输到任意的OutputStream。该类将输出数据分块缓存到内部字节数组中，以实现最佳的输出性能。
 * 该类的writeLine(CharArrayBuffer, OutputStream)方法使用CR-LF作为行分隔符。
 * @author liyibo
 * @date 2026-04-09 15:46
 */
public class SessionOutputBufferImpl implements SessionOutputBuffer {

    private static final byte[] CRLF = new byte[] { Chars.CR, Chars.LF };

    private final BasicHttpTransportMetrics metrics;

    /** 底层存储容器 */
    private final ByteArrayBuffer buffer;
    private final int fragmentSizeHint;
    private final CharsetEncoder encoder;

    private ByteBuffer bbuf;

    public SessionOutputBufferImpl(final BasicHttpTransportMetrics metrics,
                                   final int bufferSize,
                                   final int fragmentSizeHint,
                                   final CharsetEncoder charEncoder) {
        super();
        Args.positive(bufferSize, "Buffer size");
        Args.notNull(metrics, "HTTP transport metrics");
        this.metrics = metrics;
        this.buffer = new ByteArrayBuffer(bufferSize);
        this.fragmentSizeHint = fragmentSizeHint >= 0 ? fragmentSizeHint : bufferSize;
        this.encoder = charEncoder;
    }

    public SessionOutputBufferImpl(final int bufferSize) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, null);
    }

    public SessionOutputBufferImpl(final int bufferSize, final CharsetEncoder encoder) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, encoder);
    }

    @Override
    public int capacity() {
        return this.buffer.capacity();
    }

    @Override
    public int length() {
        return this.buffer.length();
    }

    @Override
    public int available() {
        return capacity() - length();
    }

    private void flushBuffer(final OutputStream outputStream) throws IOException {
        final int len = this.buffer.length();
        if (len > 0) {
            outputStream.write(this.buffer.array(), 0, len);
            this.buffer.clear();
            this.metrics.incrementBytesTransferred(len);
        }
    }

    @Override
    public void flush(final OutputStream outputStream) throws IOException {
        Args.notNull(outputStream, "Output stream");
        flushBuffer(outputStream);
        outputStream.flush();
    }

    @Override
    public void write(final byte[] b, final int off, final int len, final OutputStream outputStream) throws IOException {
        if (b == null)
            return;
        Args.notNull(outputStream, "Output stream");
        // Do not want to buffer large-ish chunks
        // if the byte array is larger then MIN_CHUNK_LIMIT
        // write it directly to the output stream
        if (len > this.fragmentSizeHint || len > this.buffer.capacity()) {
            // flush the buffer
            flushBuffer(outputStream);
            // write directly to the out stream
            outputStream.write(b, off, len);
            this.metrics.incrementBytesTransferred(len);
        } else {
            // Do not let the buffer grow unnecessarily
            final int freecapacity = this.buffer.capacity() - this.buffer.length();
            if (len > freecapacity) {
                // flush the buffer
                flushBuffer(outputStream);
            }
            // buffer
            this.buffer.append(b, off, len);
        }
    }

    @Override
    public void write(final byte[] b, final OutputStream outputStream) throws IOException {
        if (b == null)
            return;
        write(b, 0, b.length, outputStream);
    }

    @Override
    public void write(final int b, final OutputStream outputStream) throws IOException {
        Args.notNull(outputStream, "Output stream");
        if (this.fragmentSizeHint > 0) {
            if (this.buffer.isFull())
                flushBuffer(outputStream);
            this.buffer.append(b);
        } else {
            flushBuffer(outputStream);
            outputStream.write(b);
        }
    }

    @Override
    public void writeLine(final CharArrayBuffer charbuffer, final OutputStream outputStream) throws IOException {
        if (charbuffer == null)
            return;
        Args.notNull(outputStream, "Output stream");
        if (this.encoder == null) {
            int off = 0;
            int remaining = charbuffer.length();
            while (remaining > 0) {
                int chunk = this.buffer.capacity() - this.buffer.length();
                chunk = Math.min(chunk, remaining);
                if (chunk > 0)
                    this.buffer.append(charbuffer, off, chunk);
                if (this.buffer.isFull())
                    flushBuffer(outputStream);
                off += chunk;
                remaining -= chunk;
            }
        } else {
            final CharBuffer cbuf = CharBuffer.wrap(charbuffer.array(), 0, charbuffer.length());
            writeEncoded(cbuf, outputStream);
        }
        write(CRLF, outputStream);
    }

    private void writeEncoded(final CharBuffer cbuf, final OutputStream outputStream) throws IOException {
        if (!cbuf.hasRemaining())
            return;
        if (this.bbuf == null)
            this.bbuf = ByteBuffer.allocate(1024);
        this.encoder.reset();
        while (cbuf.hasRemaining()) {
            final CoderResult result = this.encoder.encode(cbuf, this.bbuf, true);
            handleEncodingResult(result, outputStream);
        }
        final CoderResult result = this.encoder.flush(this.bbuf);
        handleEncodingResult(result, outputStream);
        this.bbuf.clear();
    }

    private void handleEncodingResult(final CoderResult result, final OutputStream outputStream) throws IOException {
        if (result.isError())
            result.throwException();
        this.bbuf.flip();
        while (this.bbuf.hasRemaining())
            write(this.bbuf.get(), outputStream);
        this.bbuf.compact();
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}
