package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.TextUtils;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.util.BitSet;
import java.util.Iterator;

/**
 * Header tokens的Iterator。
 * @author liyibo
 * @date 2026-04-07 12:08
 */
public class BasicTokenIterator extends AbstractHeaderElementIterator<String> {

    private static final BitSet COMMA = Tokenizer.INIT_BITSET(',');

    private final Tokenizer tokenizer;

    public BasicTokenIterator(final Iterator<Header> headerIterator) {
        super(headerIterator);
        this.tokenizer = Tokenizer.INSTANCE;
    }

    @Override
    String parseHeaderElement(final CharSequence buf, final ParserCursor cursor) {
        final String token = this.tokenizer.parseToken(buf, cursor, COMMA);
        if (!cursor.atEnd()) {
            final int pos = cursor.getPos();
            if (buf.charAt(pos) == ',')
                cursor.updatePos(pos + 1);
        }
        return !TextUtils.isBlank(token) ? token : null;
    }
}
