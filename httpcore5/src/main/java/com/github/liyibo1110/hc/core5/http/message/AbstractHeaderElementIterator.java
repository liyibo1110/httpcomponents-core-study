package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.FormattedHeader;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * HeaderElement的Iterator。
 * @author liyibo
 * @date 2026-04-05 14:30
 */
abstract class AbstractHeaderElementIterator<T> implements Iterator<T> {
    private final Iterator<Header> headerIt;

    private T currentElement;
    private CharSequence buffer;
    private ParserCursor cursor;

    AbstractHeaderElementIterator(final Iterator<Header> headerIterator) {
        this.headerIt = Args.notNull(headerIterator, "Header iterator");
    }

    private void bufferHeaderValue() {
        this.cursor = null;
        this.buffer = null;
        while (this.headerIt.hasNext()) {
            final Header h = this.headerIt.next();
            if (h instanceof FormattedHeader) {
                this.buffer = ((FormattedHeader) h).getBuffer();
                this.cursor = new ParserCursor(0, this.buffer.length());
                this.cursor.updatePos(((FormattedHeader) h).getValuePos());
                break;
            }
            final String value = h.getValue();
            if (value != null) {
                this.buffer = value;
                this.cursor = new ParserCursor(0, value.length());
                break;
            }
        }
    }

    abstract T parseHeaderElement(CharSequence buf, ParserCursor cursor);

    private void parseNextElement() {
        // loop while there are headers left to parse
        while (this.headerIt.hasNext() || this.cursor != null) {
            if (this.cursor == null || this.cursor.atEnd()) {
                // get next header value
                bufferHeaderValue();
            }
            // Anything buffered?
            if (this.cursor != null) {
                // loop while there is data in the buffer
                while (!this.cursor.atEnd()) {
                    final T e = parseHeaderElement(this.buffer, this.cursor);
                    if (e != null) {
                        // Found something
                        this.currentElement = e;
                        return;
                    }
                }
                // if at the end of the buffer
                if (this.cursor.atEnd()) {
                    // discard it
                    this.cursor = null;
                    this.buffer = null;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (this.currentElement == null)
            parseNextElement();
        return this.currentElement != null;
    }

    @Override
    public T next() throws NoSuchElementException {
        if (this.currentElement == null)
            parseNextElement();
        if (this.currentElement == null)
            throw new NoSuchElementException("No more header elements available");
        final T element = this.currentElement;
        this.currentElement = null;
        return element;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Remove not supported");
    }
}
