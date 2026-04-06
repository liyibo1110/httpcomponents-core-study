package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;
import com.github.liyibo1110.hc.core5.util.TextUtils;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.util.BitSet;

/**
 * 默认的LineParser接口实现类
 * @author liyibo
 * @date 2026-04-06 13:47
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicLineParser implements LineParser {

    public final static BasicLineParser INSTANCE = new BasicLineParser();

    private static final BitSet FULL_STOP = Tokenizer.INIT_BITSET('.');
    private static final BitSet BLANKS = Tokenizer.INIT_BITSET(' ', '\t');
    private static final BitSet COLON = Tokenizer.INIT_BITSET(':');

    private final ProtocolVersion protocol;
    private final Tokenizer tokenizer;

    public BasicLineParser(final ProtocolVersion proto) {
        this.protocol = proto != null? proto : HttpVersion.HTTP_1_1;
        this.tokenizer = Tokenizer.INSTANCE;
    }

    public BasicLineParser() {
        this(null);
    }

    ProtocolVersion parseProtocolVersion(final CharArrayBuffer buffer, final ParserCursor cursor) throws ParseException {
        final String protoname = this.protocol.getProtocol();
        final int protolength  = protoname.length();

        this.tokenizer.skipWhiteSpace(buffer, cursor);

        final int pos = cursor.getPos();

        // long enough for "HTTP/1.1"?
        if (pos + protolength + 4 > cursor.getUpperBound())
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        // check the protocol name and slash
        boolean ok = true;
        for (int i = 0; ok && (i < protolength); i++)
            ok = buffer.charAt(pos + i) == protoname.charAt(i);
        if (ok)
            ok = buffer.charAt(pos + protolength) == '/';
        if (!ok)
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        cursor.updatePos(pos + protolength + 1);

        final String token1 = this.tokenizer.parseToken(buffer, cursor, FULL_STOP);
        final int major;
        try {
            major = Integer.parseInt(token1);
        } catch (final NumberFormatException e) {
            throw new ParseException("Invalid protocol major version number", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        if (cursor.atEnd())
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        cursor.updatePos(cursor.getPos() + 1);
        final String token2 = this.tokenizer.parseToken(buffer, cursor, BLANKS);
        final int minor;
        try {
            minor = Integer.parseInt(token2);
        } catch (final NumberFormatException e) {
            throw new ParseException("Invalid protocol minor version number", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        return HttpVersion.get(major, minor);
    }

    @Override
    public RequestLine parseRequestLine(final CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");

        final ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenizer.skipWhiteSpace(buffer, cursor);
        final String method = this.tokenizer.parseToken(buffer, cursor, BLANKS);
        if (TextUtils.isEmpty(method))
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        this.tokenizer.skipWhiteSpace(buffer, cursor);
        final String uri = this.tokenizer.parseToken(buffer, cursor, BLANKS);
        if (TextUtils.isEmpty(uri))
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        final ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
        this.tokenizer.skipWhiteSpace(buffer, cursor);
        if (!cursor.atEnd())
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());

        return new RequestLine(method, uri, ver);
    }

    @Override
    public StatusLine parseStatusLine(final CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");

        final ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenizer.skipWhiteSpace(buffer, cursor);
        final ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
        this.tokenizer.skipWhiteSpace(buffer, cursor);
        final String s = this.tokenizer.parseToken(buffer, cursor, BLANKS);
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)))
                throw new ParseException("Status line contains invalid status code", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        final int statusCode;
        try {
            statusCode = Integer.parseInt(s);
        } catch (final NumberFormatException e) {
            throw new ParseException("Status line contains invalid status code", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        final String text = buffer.substringTrimmed(cursor.getPos(), cursor.getUpperBound());
        return new StatusLine(ver, statusCode, text);
    }

    @Override
    public Header parseHeader(final CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");

        final ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenizer.skipWhiteSpace(buffer, cursor);
        final String name = this.tokenizer.parseToken(buffer, cursor, COLON);
        if (cursor.getPos() == cursor.getLowerBound() || cursor.getPos() == cursor.getUpperBound() ||
                buffer.charAt(cursor.getPos()) != ':' ||
                TextUtils.isEmpty(name) ||
                Tokenizer.isWhitespace(buffer.charAt(cursor.getPos() - 1))) {
            throw new ParseException("Invalid header", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        final String value = buffer.substringTrimmed(cursor.getPos() + 1, cursor.getUpperBound());
        return new BasicHeader(name, value);
    }
}
