package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.FormattedHeader;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriter;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.http.message.BasicLineFormatter;
import com.github.liyibo1110.hc.core5.http.message.LineFormatter;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * HTTP消息写入器的抽象基类，用于将输出序列化到SessionOutputBuffer的实例中。
 * @author liyibo
 * @date 2026-04-09 17:58
 */
public abstract class AbstractMessageWriter<T extends HttpMessage> implements HttpMessageWriter<T> {

    private final CharArrayBuffer lineBuf;
    private final LineFormatter lineFormatter;

    public AbstractMessageWriter(final LineFormatter formatter) {
        super();
        this.lineFormatter = formatter != null ? formatter : BasicLineFormatter.INSTANCE;
        this.lineBuf = new CharArrayBuffer(128);
    }

    LineFormatter getLineFormatter() {
        return this.lineFormatter;
    }

    protected abstract void writeHeadLine(T message, CharArrayBuffer lineBuf) throws IOException;

    @Override
    public void write(final T message, final SessionOutputBuffer buffer, final OutputStream outputStream) throws IOException, HttpException {
        Args.notNull(message, "HTTP message");
        Args.notNull(buffer, "Session output buffer");
        Args.notNull(outputStream, "Output stream");
        writeHeadLine(message, this.lineBuf);
        buffer.writeLine(this.lineBuf, outputStream);
        for (final Iterator<Header> it = message.headerIterator(); it.hasNext(); ) {
            final Header header = it.next();
            if (header instanceof FormattedHeader) {
                final CharArrayBuffer chbuffer = ((FormattedHeader) header).getBuffer();
                buffer.writeLine(chbuffer, outputStream);
            } else {
                this.lineBuf.clear();
                lineFormatter.formatHeader(this.lineBuf, header);
                buffer.writeLine(this.lineBuf, outputStream);
            }
        }
        this.lineBuf.clear();
        buffer.writeLine(this.lineBuf, outputStream);
    }
}
