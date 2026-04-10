package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ConnectionClosedException;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.MalformedChunkCodingException;
import com.github.liyibo1110.hc.core5.http.StreamClosedException;
import com.github.liyibo1110.hc.core5.http.TruncatedChunkException;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 实现分块传输编码。内容以小块形式接收。通过此输入流传输的实体长度不受限制。当流读取至末尾后，若存在尾部数据，该流将提供对其的访问。
 * 请注意，即使调用了close()方法，该类也绝不会关闭底层流。相反，在关闭时，它会读取至其分块处理的“结束”位置，
 * 这使得后续的HTTP 1.1请求能够无缝执行，同时无需客户端记住读取响应的全部内容。
 * @author liyibo
 * @date 2026-04-09 17:21
 */
public class ChunkedInputStream extends InputStream {

    private enum State {
        CHUNK_LEN, CHUNK_DATA, CHUNK_CRLF, CHUNK_INVALID
    }

    private static final int BUFFER_SIZE = 2048;
    private static final Header[] EMPTY_FOOTERS = {};

    /** The session input buffer */
    private final SessionInputBuffer buffer;
    private final InputStream inputStream;
    private final CharArrayBuffer lineBuffer;
    private final Http1Config http1Config;

    private State state;

    /** The chunk size */
    private long chunkSize;

    /** The current position within the current chunk */
    private long pos;

    /** True if we've reached the end of stream */
    private boolean eof;

    /** True if this stream is closed */
    private boolean closed;

    private Header[] footers = EMPTY_FOOTERS;

    public ChunkedInputStream(final SessionInputBuffer buffer, final InputStream inputStream, final Http1Config http1Config) {
        super();
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
        this.pos = 0L;
        this.lineBuffer = new CharArrayBuffer(16);
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.state = State.CHUNK_LEN;
    }

    public ChunkedInputStream(final SessionInputBuffer buffer, final InputStream inputStream) {
        this(buffer, inputStream, null);
    }

    @Override
    public int available() throws IOException {
        final int len = this.buffer.length();
        return (int) Math.min(len, this.chunkSize - this.pos);
    }

    @Override
    public int read() throws IOException {
        if (this.closed)
            throw new StreamClosedException();
        if (this.eof)
            return -1;
        if (state != State.CHUNK_DATA) {
            nextChunk();
            if (this.eof)
                return -1;
        }
        final int b = buffer.read(inputStream);
        if (b != -1) {
            pos++;
            if (pos >= chunkSize)
                state = State.CHUNK_CRLF;
        }
        return b;
    }

    @Override
    public int read (final byte[] b, final int off, final int len) throws IOException {
        if (closed)
            throw new StreamClosedException();

        if (eof)
            return -1;
        if (state != State.CHUNK_DATA) {
            nextChunk();
            if (eof)
                return -1;
        }
        final int bytesRead = buffer.read(b, off, (int) Math.min(len, chunkSize - pos), inputStream);
        if (bytesRead != -1) {
            pos += bytesRead;
            if (pos >= chunkSize)
                state = State.CHUNK_CRLF;
            return bytesRead;
        }
        eof = true;
        throw new TruncatedChunkException("Truncated chunk (expected size: %d; actual size: %d)", chunkSize, pos);
    }

    @Override
    public int read (final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    private void nextChunk() throws IOException {
        if (state == State.CHUNK_INVALID)
            throw new MalformedChunkCodingException("Corrupt data stream");
        try {
            chunkSize = getChunkSize();
            if (chunkSize < 0L)
                throw new MalformedChunkCodingException("Negative chunk size");
            state = State.CHUNK_DATA;
            pos = 0L;
            if (chunkSize == 0L) {
                eof = true;
                parseTrailerHeaders();
            }
        } catch (final MalformedChunkCodingException ex) {
            state = State.CHUNK_INVALID;
            throw ex;
        }
    }

    private long getChunkSize() throws IOException {
        final State st = this.state;
        switch (st) {
            case CHUNK_CRLF:
                lineBuffer.clear();
                final int bytesRead1 = this.buffer.readLine(lineBuffer, inputStream);
                if (bytesRead1 == -1)
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                if (!lineBuffer.isEmpty())
                    throw new MalformedChunkCodingException("Unexpected content at the end of chunk");
                state = State.CHUNK_LEN;
                //$FALL-THROUGH$
            case CHUNK_LEN:
                lineBuffer.clear();
                final int bytesRead2 = this.buffer.readLine(lineBuffer, inputStream);
                if (bytesRead2 == -1)
                    throw new ConnectionClosedException("Premature end of chunk coded message body: closing chunk expected");
                int separator = lineBuffer.indexOf(';');
                if (separator < 0)
                    separator = lineBuffer.length();
                final String s = this.lineBuffer.substringTrimmed(0, separator);
                try {
                    return Long.parseLong(s, 16);
                } catch (final NumberFormatException e) {
                    throw new MalformedChunkCodingException("Bad chunk header: " + s);
                }
            default:
                throw new IllegalStateException("Inconsistent codec state");
        }
    }

    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParser.parseHeaders(buffer, inputStream,
                    http1Config.getMaxHeaderCount(),
                    http1Config.getMaxLineLength(),
                    null);
        } catch (final HttpException ex) {
            final IOException ioe = new MalformedChunkCodingException("Invalid trailing header: " + ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                if (!eof && state != State.CHUNK_INVALID) {
                    // Optimistically check if the content has been fully read
                    // when there's no data remaining in the current chunk.
                    // This is common when self-terminating content (e.g. JSON)
                    // is parsed from response streams.
                    if (chunkSize == pos && chunkSize > 0 && read() == -1) {
                        return;
                    }
                    // read and discard the remainder of the message
                    final byte[] buff = new byte[BUFFER_SIZE];
                    while (read(buff) >= 0) {
                    }
                }
            } finally {
                eof = true;
                closed = true;
            }
        }
    }

    public Header[] getFooters() {
        return footers.length > 0 ? footers.clone() : EMPTY_FOOTERS;
    }
}
