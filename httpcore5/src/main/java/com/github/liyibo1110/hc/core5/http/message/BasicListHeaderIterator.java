package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Asserts;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Header的Iterator，HeadGroup组件专用。
 * @author liyibo
 * @date 2026-04-07 12:05
 */
class BasicListHeaderIterator implements Iterator<Header> {

    private final List<Header> allHeaders;

    private int currentIndex;

    private int lastIndex;

    private final String headerName;

    public BasicListHeaderIterator(final List<Header> headers, final String name) {
        super();
        this.allHeaders = Args.notNull(headers, "Header list");
        this.headerName = name;
        this.currentIndex = findNext(-1);
        this.lastIndex = -1;
    }

    protected int findNext(final int pos) {
        int from = pos;
        if (from < -1)
            return -1;

        final int to = this.allHeaders.size()-1;
        boolean found = false;
        while (!found && (from < to)) {
            from++;
            found = filterHeader(from);
        }
        return found ? from : -1;
    }

    private boolean filterHeader(final int index) {
        if (this.headerName == null)
            return true;
        // non-header elements, including null, will trigger exceptions
        final String name = (this.allHeaders.get(index)).getName();
        return this.headerName.equalsIgnoreCase(name);
    }

    @Override
    public boolean hasNext() {
        return this.currentIndex >= 0;
    }

    @Override
    public Header next() throws NoSuchElementException {
        final int current = this.currentIndex;
        if (current < 0)
            throw new NoSuchElementException("Iteration already finished.");

        this.lastIndex    = current;
        this.currentIndex = findNext(current);
        return this.allHeaders.get(current);
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        Asserts.check(this.lastIndex >= 0, "No header to remove");
        this.allHeaders.remove(this.lastIndex);
        this.lastIndex = -1;
        this.currentIndex--; // adjust for the removed element
    }
}
