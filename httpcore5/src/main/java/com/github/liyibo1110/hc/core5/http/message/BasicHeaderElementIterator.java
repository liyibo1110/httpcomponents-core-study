package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Iterator;

/**
 * HeaderElement的Iterator。
 * @author liyibo
 * @date 2026-04-05 15:30
 */
public class BasicHeaderElementIterator extends AbstractHeaderElementIterator<HeaderElement> {
    private final HeaderValueParser parser;

    public BasicHeaderElementIterator(final Iterator<Header> headerIterator, final HeaderValueParser parser) {
        super(headerIterator);
        this.parser = Args.notNull(parser, "Parser");
    }

    public BasicHeaderElementIterator(final Iterator<Header> headerIterator) {
        this(headerIterator, BasicHeaderValueParser.INSTANCE);
    }

    @Override
    HeaderElement parseHeaderElement(final CharSequence buf, final ParserCursor cursor) {
        final HeaderElement e = this.parser.parseHeaderElement(buf, cursor);
        if (!(e.getName().isEmpty() && e.getValue() == null))
            return e;
        return null;
    }
}
