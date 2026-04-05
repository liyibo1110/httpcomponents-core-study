package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Header的Iterator
 * @author liyibo
 * @date 2026-04-05 15:21
 */
public class BasicHeaderIterator implements Iterator<Header> {
    private final Header[] allHeaders;

    private int currentIndex;

    private final String headerName;

    public BasicHeaderIterator(final Header[] headers, final String name) {
        super();
        this.allHeaders = Args.notNull(headers, "Header array");
        this.headerName = name;
        this.currentIndex = findNext(-1);
    }

    /**
     * 寻找下一个header的数组下标。
     * pos为起始下标，-1则表示从头开始。
     */
    private int findNext(final int pos) {
        int from = pos;
        if (from < -1)
            return -1;
        final int to = this.allHeaders.length - 1;
        boolean found = false;
        while(!found && (from < to)) {
            from++;
            found = filterHeader(from);
        }
        return found ? from : -1;
    }

    /**
     * 检查某个header是否属于迭代范围，返回true说明header包含在迭代中。
     */
    private boolean filterHeader(final int index) {
        return (this.headerName == null) || this.headerName.equalsIgnoreCase(this.allHeaders[index].getName());
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
        this.currentIndex = findNext(current);
        return this.allHeaders[current];
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing headers is not supported.");
    }
}
