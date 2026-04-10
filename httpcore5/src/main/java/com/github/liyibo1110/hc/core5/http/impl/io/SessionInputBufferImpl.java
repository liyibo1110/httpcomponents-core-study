package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.Chars;
import com.github.liyibo1110.hc.core5.http.MessageConstraintException;
import com.github.liyibo1110.hc.core5.http.impl.BasicHttpTransportMetrics;
import com.github.liyibo1110.hc.core5.http.io.HttpTransportMetrics;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.ByteArrayBuffer;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * 这是一个用于session输入缓冲区的抽象基类，可从任意InputStream流式读取数据。该类将输入数据缓存到内部字节数组中，以实现最佳的输入性能。
 * 该类的readLine(CharArrayBuffer, InputStream)方法不仅支持HTTP规范要求的CR-LF换行符，还将单个LF字符视为有效的行分隔符。
 * @author liyibo
 * @date 2026-04-09 15:29
 */
public class SessionInputBufferImpl implements SessionInputBuffer {

    private final BasicHttpTransportMetrics metrics;

    /** 底层存储容器，注意不是个循环数组，读满就不读了 */
    private final byte[] buffer;
    private final ByteArrayBuffer lineBuffer;
    private final int minChunkLimit;
    private final int maxLineLen;
    private final CharsetDecoder decoder;

    /** 有效数据的起始 */
    private int bufferPos;

    /** 有效数据的末尾 */
    private int bufferLen;
    private CharBuffer cbuf;

    public SessionInputBufferImpl(final BasicHttpTransportMetrics metrics,
                                  final int bufferSize,
                                  final int minChunkLimit,
                                  final int maxLineLen,
                                  final CharsetDecoder charDecoder) {
        Args.notNull(metrics, "HTTP transport metrics");
        Args.positive(bufferSize, "Buffer size");
        this.metrics = metrics;
        this.buffer = new byte[bufferSize];
        this.bufferPos = 0;
        this.bufferLen = 0;
        this.minChunkLimit = minChunkLimit >= 0 ? minChunkLimit : 512;
        this.maxLineLen = Math.max(maxLineLen, 0);
        this.lineBuffer = new ByteArrayBuffer(bufferSize);
        this.decoder = charDecoder;
    }

    public SessionInputBufferImpl(final BasicHttpTransportMetrics metrics, final int bufferSize) {
        this(metrics, bufferSize, bufferSize, 0, null);
    }

    public SessionInputBufferImpl(final int bufferSize, final int maxLineLen) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, maxLineLen, null);
    }

    public SessionInputBufferImpl(final int bufferSize, final CharsetDecoder decoder) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, 0, decoder);
    }

    public SessionInputBufferImpl(final int bufferSize) {
        this(new BasicHttpTransportMetrics(), bufferSize, bufferSize, 0, null);
    }

    @Override
    public int capacity() {
        return this.buffer.length;
    }

    @Override
    public int length() {
        return this.bufferLen - this.bufferPos;
    }

    @Override
    public int available() {
        return capacity() - length();
    }

    /**
     * 核心方法，从is里读数据到buffer
     */
    public int fillBuffer(final InputStream inputStream) throws IOException {
        Args.notNull(inputStream, "Input stream");
        // 尝试压缩数据，即把要读的数据挪到数组最前面，直接覆盖掉已经读过的数据
        if (this.bufferPos > 0) {
            final int len = this.bufferLen - this.bufferPos;
            if (len > 0)
                System.arraycopy(this.buffer, this.bufferPos, this.buffer, 0, len);
            this.bufferPos = 0;
            this.bufferLen = len;
        }
        final int readLen;
        final int off = this.bufferLen;
        final int len = this.buffer.length - off;
        readLen = inputStream.read(this.buffer, off, len);
        if (readLen == -1)
            return -1;
        this.bufferLen = off + readLen;
        this.metrics.incrementBytesTransferred(readLen);
        return readLen;
    }

    public boolean hasBufferedData() {
        return this.bufferPos < this.bufferLen;
    }

    public void clear() {
        this.bufferPos = 0;
        this.bufferLen = 0;
    }

    @Override
    public int read(final InputStream inputStream) throws IOException {
        Args.notNull(inputStream, "Input stream");
        int readLen;
        // 先把buffer读满
        while (!hasBufferedData()) {
            readLen = fillBuffer(inputStream);
            if (readLen == -1)
                return -1;
        }
        // 返回第一个字节
        return this.buffer[this.bufferPos++] & 0xff;
    }

    @Override
    public int read(final byte[] b, final int off, final int len, final InputStream inputStream) throws IOException {
        Args.notNull(inputStream, "Input stream");
        if (b == null)
            return 0;

        if (hasBufferedData()) {
            final int chunk = Math.min(len, this.bufferLen - this.bufferPos);
            System.arraycopy(this.buffer, this.bufferPos, b, off, chunk);
            this.bufferPos += chunk;
            return chunk;
        }
        // If the remaining capacity is big enough, read directly from the
        // underlying input stream bypassing the buffer.
        if (len > this.minChunkLimit) {
            final int read = inputStream.read(b, off, len);
            if (read > 0)
                this.metrics.incrementBytesTransferred(read);
            return read;
        }
        // otherwise read to the buffer first
        while (!hasBufferedData()) {
            final int readLen = fillBuffer(inputStream);
            if (readLen == -1)
                return -1;
        }
        final int chunk = Math.min(len, this.bufferLen - this.bufferPos);
        System.arraycopy(this.buffer, this.bufferPos, b, off, chunk);
        this.bufferPos += chunk;
        return chunk;
    }

    @Override
    public int read(final byte[] b, final InputStream inputStream) throws IOException {
        if (b == null)
            return 0;
        return read(b, 0, b.length, inputStream);
    }

    @Override
    public int readLine(final CharArrayBuffer charBuffer, final InputStream inputStream) throws IOException {
        Args.notNull(charBuffer, "Char array buffer");
        Args.notNull(inputStream, "Input stream");
        int readLen = 0;
        boolean retry = true;
        while (retry) {
            // attempt to find end of line (LF)
            int pos = -1;
            for (int i = this.bufferPos; i < this.bufferLen; i++) {
                if (this.buffer[i] == Chars.LF) {
                    pos = i;
                    break;
                }
            }

            if (this.maxLineLen > 0) {
                final int currentLen = this.lineBuffer.length() + (pos >= 0 ? pos : this.bufferLen) - this.bufferPos;
                if (currentLen >= this.maxLineLen)
                    throw new MessageConstraintException("Maximum line length limit exceeded");
            }

            if (pos != -1) {
                // end of line found.
                if (this.lineBuffer.isEmpty()) {
                    // the entire line is preset in the read buffer
                    return lineFromReadBuffer(charBuffer, pos);
                }
                retry = false;
                final int len = pos + 1 - this.bufferPos;
                this.lineBuffer.append(this.buffer, this.bufferPos, len);
                this.bufferPos = pos + 1;
            } else {
                // end of line not found
                if (hasBufferedData()) {
                    final int len = this.bufferLen - this.bufferPos;
                    this.lineBuffer.append(this.buffer, this.bufferPos, len);
                    this.bufferPos = this.bufferLen;
                }
                readLen = fillBuffer(inputStream);
                if (readLen == -1)
                    retry = false;
            }
        }
        if (readLen == -1 && this.lineBuffer.isEmpty()) {
            // indicate the end of stream
            return -1;
        }
        return lineFromLineBuffer(charBuffer);
    }

    private int lineFromLineBuffer(final CharArrayBuffer charBuffer) throws IOException {
        // discard LF if found
        int len = this.lineBuffer.length();
        if (len > 0) {
            if (this.lineBuffer.byteAt(len - 1) == Chars.LF)
                len--;
            // discard CR if found
            if (len > 0 && this.lineBuffer.byteAt(len - 1) == Chars.CR)
                len--;
        }
        if (this.decoder == null) {
            charBuffer.append(this.lineBuffer, 0, len);
        } else {
            final ByteBuffer bbuf =  ByteBuffer.wrap(this.lineBuffer.array(), 0, len);
            len = appendDecoded(charBuffer, bbuf);
        }
        this.lineBuffer.clear();
        return len;
    }

    private int lineFromReadBuffer(final CharArrayBuffer charbuffer, final int position) throws IOException {
        int pos = position;
        final int off = this.bufferPos;
        int len;
        this.bufferPos = pos + 1;
        if (pos > off && this.buffer[pos - 1] == Chars.CR) {
            // skip CR if found
            pos--;
        }
        len = pos - off;
        if (this.decoder == null) {
            charbuffer.append(this.buffer, off, len);
        } else {
            final ByteBuffer bbuf =  ByteBuffer.wrap(this.buffer, off, len);
            len = appendDecoded(charbuffer, bbuf);
        }
        return len;
    }

    private int appendDecoded(final CharArrayBuffer charbuffer, final ByteBuffer bbuf) throws IOException {
        if (!bbuf.hasRemaining())
            return 0;
        if (this.cbuf == null)
            this.cbuf = CharBuffer.allocate(1024);
        this.decoder.reset();
        int len = 0;
        while (bbuf.hasRemaining()) {
            final CoderResult result = this.decoder.decode(bbuf, this.cbuf, true);
            len += handleDecodingResult(result, charbuffer);
        }
        final CoderResult result = this.decoder.flush(this.cbuf);
        len += handleDecodingResult(result, charbuffer);
        this.cbuf.clear();
        return len;
    }

    private int handleDecodingResult(final CoderResult result, final CharArrayBuffer charBuffer) throws IOException {
        if (result.isError())
            result.throwException();
        this.cbuf.flip();
        final int len = this.cbuf.remaining();
        while (this.cbuf.hasRemaining())
            charBuffer.append(this.cbuf.get());
        this.cbuf.compact();
        return len;
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}
