package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.MessageHeaders;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 一个用于合并一组Header的类。
 * 该类支持存在多个同名Header，并记录Header的添加顺序。
 * @author liyibo
 * @date 2026-04-07 11:21
 */
public class HeaderGroup implements MessageHeaders, Serializable {
    private static final long serialVersionUID = 2608834160639271617L;

    private static final Header[] EMPTY = new Header[] {};

    /** header列表，按添加顺序排列。 */
    private final List<Header> headers;

    public HeaderGroup() {
        this.headers = new ArrayList<>(16);
    }

    public void clear() {
        headers.clear();
    }

    public void addHeader(final Header header) {
        if (header == null)
            return;
        headers.add(header);
    }

    public boolean removeHeader(final Header header) {
        if (header == null)
            return false;
        for (int i = 0; i < this.headers.size(); i++) {
            final Header current = this.headers.get(i);
            if (headerEquals(header, current)) {
                this.headers.remove(current);
                return true;
            }
        }
        return false;
    }

    /**
     * name和value都要相等。
     */
    private boolean headerEquals(final Header header1, final Header header2) {
        return header2 == header1 || header2.getName().equalsIgnoreCase(header1.getName())
                && Objects.equals(header1.getValue(), header2.getValue());
    }

    public boolean removeHeaders(final Header header) {
        if (header == null)
            return false;
        boolean removed = false;
        for (final Iterator<Header> iterator = headerIterator(); iterator.hasNext();) {
            final Header current = iterator.next();
            if (headerEquals(header, current)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * 找到一样的就覆盖，否则就新增。
     */
    public void setHeader(final Header header) {
        if (header == null)
            return;
        for (int i = 0; i < this.headers.size(); i++) {
            final Header current = this.headers.get(i);
            if (current.getName().equalsIgnoreCase(header.getName())) {
                this.headers.set(i, header);
                return;
            }
        }
        this.headers.add(header);
    }

    public void setHeaders(final Header... headers) {
        clear();
        if (headers == null)
            return;
        Collections.addAll(this.headers, headers);
    }

    /**
     * 获取一个代表所有具有指定名称的head value的header。如果存在多个具有指定名称的header，则这些值将用“,”分隔组合在一起。
     * 标头名称的比较不区分大小写。
     */
    public Header getCondensedHeader(final String name) {
        final Header[] hdrs = getHeaders(name);

        if (hdrs.length == 0) {
            return null;
        } else if (hdrs.length == 1) {
            return hdrs[0];
        } else {
            final CharArrayBuffer valueBuffer = new CharArrayBuffer(128);
            valueBuffer.append(hdrs[0].getValue());
            for (int i = 1; i < hdrs.length; i++) {
                valueBuffer.append(", ");
                valueBuffer.append(hdrs[i].getValue());
            }

            return new BasicHeader(TextUtils.toLowerCase(name), valueBuffer.toString());
        }
    }

    @Override
    public Header[] getHeaders(final String name) {
        List<Header> headersFound = null;
        for (int i = 0; i < this.headers.size(); i++) {
            final Header header = this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                if (headersFound == null)
                    headersFound = new ArrayList<>();
                headersFound.add(header);
            }
        }
        return headersFound != null ? headersFound.toArray(EMPTY) : EMPTY;
    }

    @Override
    public Header getFirstHeader(final String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            final Header header = this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name))
                return header;
        }
        return null;
    }

    @Override
    public Header getHeader(final String name) throws ProtocolException {
        int count = 0;
        Header singleHeader = null;
        for (int i = 0; i < this.headers.size(); i++) {
            final Header header = this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                singleHeader = header;
                count++;
            }
        }
        if (count > 1)
            throw new ProtocolException("multiple '%s' headers found", name);
        return singleHeader;
    }

    @Override
    public Header getLastHeader(final String name) {
        // start at the end of the list and work backwards
        for (int i = headers.size() - 1; i >= 0; i--) {
            final Header header = headers.get(i);
            if (header.getName().equalsIgnoreCase(name))
                return header;
        }

        return null;
    }

    @Override
    public Header[] getHeaders() {
        return headers.toArray(EMPTY);
    }

    @Override
    public boolean containsHeader(final String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            final Header header = this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    @Override
    public int countHeaders(final String name) {
        int count = 0;
        for (int i = 0; i < this.headers.size(); i++) {
            final Header header = this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name))
                count++;
        }
        return count;
    }

    @Override
    public Iterator<Header> headerIterator() {
        return new BasicListHeaderIterator(this.headers, null);
    }

    @Override
    public Iterator<Header> headerIterator(final String name) {
        return new BasicListHeaderIterator(this.headers, name);
    }

    public boolean removeHeaders(final String name) {
        if (name == null)
            return false;

        boolean removed = false;
        for (final Iterator<Header> iterator = headerIterator(); iterator.hasNext(); ) {
            final Header header = iterator.next();
            if (header.getName().equalsIgnoreCase(name)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public String toString() {
        return this.headers.toString();
    }
}
