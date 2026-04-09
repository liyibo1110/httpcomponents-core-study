package com.github.liyibo1110.hc.core5.http.ssl;

import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.util.BitSet;

/**
 * @author liyibo
 * @date 2026-04-08 13:09
 */
final class TlsVersionParser {
    public final static TlsVersionParser INSTANCE = new TlsVersionParser();

    private final Tokenizer tokenizer;

    TlsVersionParser() {
        this.tokenizer = Tokenizer.INSTANCE;
    }

    ProtocolVersion parse(final CharSequence buffer, final Tokenizer.Cursor cursor, final BitSet delimiters) throws ParseException {
        final int lowerBound = cursor.getLowerBound();
        final int upperBound = cursor.getUpperBound();

        int pos = cursor.getPos();
        if (pos + 4 > cursor.getUpperBound())
            throw new ParseException("Invalid TLS protocol version", buffer, lowerBound, upperBound, pos);

        if (buffer.charAt(pos) != 'T'
                || buffer.charAt(pos + 1) != 'L'
                || buffer.charAt(pos + 2) != 'S'
                || buffer.charAt(pos + 3) != 'v') {
            throw new ParseException("Invalid TLS protocol version", buffer, lowerBound, upperBound, pos);
        }
        pos = pos + 4;
        cursor.updatePos(pos);
        if (cursor.atEnd())
            throw new ParseException("Invalid TLS version", buffer, lowerBound, upperBound, pos);

        final String s = this.tokenizer.parseToken(buffer, cursor, delimiters);
        final int idx = s.indexOf('.');
        if (idx == -1) {
            final int major;
            try {
                major = Integer.parseInt(s);
            } catch (final NumberFormatException e) {
                throw new ParseException("Invalid TLS major version", buffer, lowerBound, upperBound, pos);
            }
            return new ProtocolVersion("TLS", major, 0);
        } else {
            final String s1 = s.substring(0, idx);
            final int major;
            try {
                major = Integer.parseInt(s1);
            } catch (final NumberFormatException e) {
                throw new ParseException("Invalid TLS major version", buffer, lowerBound, upperBound, pos);
            }
            final String s2 = s.substring(idx + 1);
            final int minor;
            try {
                minor = Integer.parseInt(s2);
            } catch (final NumberFormatException e) {
                throw new ParseException("Invalid TLS minor version", buffer, lowerBound, upperBound, pos);
            }
            return new ProtocolVersion("TLS", major, minor);
        }
    }

    ProtocolVersion parse(final String s) throws ParseException {
        if (s == null)
            return null;
        final Tokenizer.Cursor cursor = new Tokenizer.Cursor(0, s.length());
        return parse(s, cursor, null);
    }
}
