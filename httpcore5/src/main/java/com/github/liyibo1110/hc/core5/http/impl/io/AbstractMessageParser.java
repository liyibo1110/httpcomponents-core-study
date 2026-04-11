package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ConnectionClosedException;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.MessageConstraintException;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParser;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.http.message.LazyLineParser;
import com.github.liyibo1110.hc.core5.http.message.LineParser;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 用于从SessionInputBuffer实例获取输入的HTTP消息解析器的抽象基类。
 * @author liyibo
 * @date 2026-04-09 17:47
 */
public abstract class AbstractMessageParser<T extends HttpMessage> implements HttpMessageParser<T> {

    private static final int HEAD_LINE = 0;
    private static final int HEADERS = 1;

    private final Http1Config http1Config;
    private final List<CharArrayBuffer> headerLines;
    private final CharArrayBuffer headLine;
    private final LineParser lineParser;

    private int state;
    private T message;

    public AbstractMessageParser(final LineParser lineParser, final Http1Config http1Config) {
        super();
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.headerLines = new ArrayList<>();
        this.headLine = new CharArrayBuffer(128);
        this.state = HEAD_LINE;
    }

    LineParser getLineParser() {
        return this.lineParser;
    }

    public static Header[] parseHeaders(final SessionInputBuffer inBuffer,
                                        final InputStream inputStream,
                                        final int maxHeaderCount,
                                        final int maxLineLen,
                                        final LineParser lineParser) throws HttpException, IOException {
        final List<CharArrayBuffer> headerLines = new ArrayList<>();
        return parseHeaders(inBuffer, inputStream, maxHeaderCount, maxLineLen, lineParser != null ? lineParser : LazyLineParser.INSTANCE, headerLines);
    }

    public static Header[] parseHeaders(final SessionInputBuffer inBuffer,
                                        final InputStream inputStream,
                                        final int maxHeaderCount,
                                        final int maxLineLen,
                                        final LineParser parser,
                                        final List<CharArrayBuffer> headerLines) throws HttpException, IOException {
        Args.notNull(inBuffer, "Session input buffer");
        Args.notNull(inputStream, "Input stream");
        Args.notNull(parser, "Line parser");
        Args.notNull(headerLines, "Header line list");

        CharArrayBuffer current = null;
        CharArrayBuffer previous = null;
        for (;;) {
            if (current == null)
                current = new CharArrayBuffer(64);
            else
                current.clear();
            final int readLen = inBuffer.readLine(current, inputStream);
            if (readLen == -1 || current.length() < 1)
                break;
            // Parse the header name and value
            // Check for folded headers first
            // Detect LWS-char see HTTP/1.0 or HTTP/1.1 Section 2.2
            // discussion on folded headers
            if ((current.charAt(0) == ' ' || current.charAt(0) == '\t') && previous != null) {
                // we have continuation folded header
                // so append value
                int i = 0;
                while (i < current.length()) {
                    final char ch = current.charAt(i);
                    if (ch != ' ' && ch != '\t')
                        break;
                    i++;
                }
                if (maxLineLen > 0 && previous.length() + 1 + current.length() - i > maxLineLen)
                    throw new MessageConstraintException("Maximum line length limit exceeded");
                previous.append(' ');
                previous.append(current, i, current.length() - i);
            } else {
                headerLines.add(current);
                previous = current;
                current = null;
            }
            if (maxHeaderCount > 0 && headerLines.size() >= maxHeaderCount)
                throw new MessageConstraintException("Maximum header count exceeded");
        }
        final Header[] headers = new Header[headerLines.size()];
        for (int i = 0; i < headerLines.size(); i++) {
            final CharArrayBuffer buffer = headerLines.get(i);
            headers[i] = parser.parseHeader(buffer);
        }
        return headers;
    }

    protected abstract T createMessage(CharArrayBuffer buffer) throws IOException, HttpException;

    @Deprecated
    protected IOException createConnectionClosedException() {
        return new ConnectionClosedException();
    }

    @Override
    public T parse(final SessionInputBuffer buffer, final InputStream inputStream) throws IOException, HttpException {
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(inputStream, "Input stream");
        final int st = this.state;
        switch (st) {
            case HEAD_LINE:
                for (int n = 0; n < this.http1Config.getMaxEmptyLineCount(); n++) {
                    this.headLine.clear();
                    final int i = buffer.readLine(this.headLine, inputStream);
                    if (i == -1)
                        return null;
                    if (this.headLine.length() > 0) {
                        this.message = createMessage(this.headLine);
                        if (this.message != null)
                            break;
                    }
                }
                if (this.message == null)
                    throw new MessageConstraintException("Maximum empty line limit exceeded");
                this.state = HEADERS;
                //$FALL-THROUGH$
            case HEADERS:
                final Header[] headers = AbstractMessageParser.parseHeaders(
                        buffer,
                        inputStream,
                        this.http1Config.getMaxHeaderCount(),
                        this.http1Config.getMaxLineLength(),
                        this.lineParser,
                        this.headerLines);
                this.message.setHeaders(headers);
                final T result = this.message;
                this.message = null;
                this.headerLines.clear();
                this.state = HEAD_LINE;
                return result;
            default:
                throw new IllegalStateException("Inconsistent parser state");
        }
    }
}
