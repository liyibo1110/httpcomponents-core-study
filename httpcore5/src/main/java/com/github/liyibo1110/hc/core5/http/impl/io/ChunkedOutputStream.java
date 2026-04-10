package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.FormattedHeader;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.http.message.BasicLineFormatter;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 实现分块传输编码。内容以小块形式发送。通过此输出流传输的实体长度不受限制。写入操作会缓存到内部缓冲区中（默认大小为 2048）。
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，该流将被标记为已关闭，且不再允许进行任何输出操作。
 * @author liyibo
 * @date 2026-04-09 16:45
 */
public class ChunkedOutputStream extends OutputStream {

    private final SessionOutputBuffer buffer;
    private final OutputStream outputStream;

    private final byte[] cache;
    private int cachePosition;
    private boolean wroteLastChunk;
    private boolean closed;
    private final CharArrayBuffer lineBuffer;
    private final Supplier<List<? extends Header>> trailerSupplier;

    public ChunkedOutputStream(final SessionOutputBuffer buffer,
                               final OutputStream outputStream,
                               final byte[] chunkCache,
                               final Supplier<List<? extends Header>> trailerSupplier) {
        super();
        this.buffer = Args.notNull(buffer, "Session output buffer");
        this.outputStream = Args.notNull(outputStream, "Output stream");
        this.cache = Args.notNull(chunkCache, "Chunk cache");
        this.lineBuffer = new CharArrayBuffer(32);
        this.trailerSupplier = trailerSupplier;
    }

    public ChunkedOutputStream(final SessionOutputBuffer buffer,
                               final OutputStream outputStream,
                               final int chunkSizeHint,
                               final Supplier<List<? extends Header>> trailerSupplier) {
        this(buffer, outputStream, new byte[chunkSizeHint > 0 ? chunkSizeHint : 8192], trailerSupplier);
    }

    public ChunkedOutputStream(final SessionOutputBuffer buffer, final OutputStream outputStream, final int chunkSizeHint) {
        this(buffer, outputStream, chunkSizeHint, null);
    }

    private void flushCache() throws IOException {
        if (this.cachePosition > 0) {
            this.lineBuffer.clear();
            this.lineBuffer.append(Integer.toHexString(this.cachePosition));
            this.buffer.writeLine(this.lineBuffer, this.outputStream);
            this.buffer.write(this.cache, 0, this.cachePosition, this.outputStream);
            this.lineBuffer.clear();
            this.buffer.writeLine(this.lineBuffer, this.outputStream);
            this.cachePosition = 0;
        }
    }

    private void flushCacheWithAppend(final byte[] bufferToAppend, final int off, final int len) throws IOException {
        this.lineBuffer.clear();
        this.lineBuffer.append(Integer.toHexString(this.cachePosition + len));
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        this.buffer.write(this.cache, 0, this.cachePosition, this.outputStream);
        this.buffer.write(bufferToAppend, off, len, this.outputStream);
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        this.cachePosition = 0;
    }

    private void writeClosingChunk() throws IOException {
        // Write the final chunk.
        this.lineBuffer.clear();
        this.lineBuffer.append('0');
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
        writeTrailers();
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer, this.outputStream);
    }

    private void writeTrailers() throws IOException {
        final List<? extends Header> trailers = this.trailerSupplier != null ? this.trailerSupplier.get() : null;
        if (trailers != null) {
            for (int i = 0; i < trailers.size(); i++) {
                final Header header = trailers.get(i);
                if (header instanceof FormattedHeader) {
                    final CharArrayBuffer chbuffer = ((FormattedHeader) header).getBuffer();
                    this.buffer.writeLine(chbuffer, this.outputStream);
                } else {
                    this.lineBuffer.clear();
                    BasicLineFormatter.INSTANCE.formatHeader(this.lineBuffer, header);
                    this.buffer.writeLine(this.lineBuffer, this.outputStream);
                }
            }
        }
    }

    // ----------------------------------------------------------- Public Methods

    public void finish() throws IOException {
        if (!this.wroteLastChunk) {
            flushCache();
            writeClosingChunk();
            this.wroteLastChunk = true;
        }
    }

    // -------------------------------------------- OutputStream Methods

    @Override
    public void write(final int b) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        this.cache[this.cachePosition] = (byte) b;
        this.cachePosition++;
        if (this.cachePosition == this.cache.length)
            flushCache();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] src, final int off, final int len) throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        if (len >= this.cache.length - this.cachePosition) {
            flushCacheWithAppend(src, off, len);
        } else {
            System.arraycopy(src, off, cache, this.cachePosition, len);
            this.cachePosition += len;
        }
    }

    @Override
    public void flush() throws IOException {
        flushCache();
        this.buffer.flush(this.outputStream);
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            finish();
            this.buffer.flush(this.outputStream);
        }
    }
}
