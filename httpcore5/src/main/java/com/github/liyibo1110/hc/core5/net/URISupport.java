package com.github.liyibo1110.hc.core5.net;

import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.net.URISyntaxException;
import java.util.BitSet;

/**
 * @author liyibo
 * @date 2026-04-07 10:28
 */
public class URISupport {
    static final BitSet HOST_SEPARATORS = new BitSet(256);
    static final BitSet IPV6_HOST_TERMINATORS = new BitSet(256);
    static final BitSet PORT_SEPARATORS = new BitSet(256);
    static final BitSet TERMINATORS = new BitSet(256);

    static {
        TERMINATORS.set('/');
        TERMINATORS.set('#');
        TERMINATORS.set('?');
        HOST_SEPARATORS.or(TERMINATORS);
        HOST_SEPARATORS.set('@');
        IPV6_HOST_TERMINATORS.set(']');
        PORT_SEPARATORS.or(TERMINATORS);
        PORT_SEPARATORS.set(':');
    }

    static URISyntaxException createException(final CharSequence input, final Tokenizer.Cursor cursor, final String reason) {
        return new URISyntaxException(
                input.subSequence(cursor.getLowerBound(), cursor.getUpperBound()).toString(),
                reason,
                cursor.getPos());
    }
}
