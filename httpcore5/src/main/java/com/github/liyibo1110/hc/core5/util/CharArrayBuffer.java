package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.http.Chars;

import java.io.Serializable;
import java.nio.CharBuffer;

/**
 * 可扩容的char数组容器
 * @author liyibo
 * @date 2026-04-02 14:19
 */
public final class CharArrayBuffer implements CharSequence, Serializable {

    private static final long serialVersionUID = -6208952725094867135L;

    /** 底层数组 */
    private char[] array;

    /** 暗示了不会清理旧数据 */
    private int len;

    public CharArrayBuffer(final int capacity) {
        super();
        Args.notNegative(capacity, "Buffer capacity");
        this.array = new char[capacity];
    }

    private void expand(final int newlen) {
        final char[] newArray = new char[Math.max(this.array.length << 1, newlen)];
        System.arraycopy(this.array, 0, newArray, 0, this.len);
        this.array = newArray;
    }

    public void append(final char[] b, final int off, final int len) {
        if (b == null)
            return;

        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0) || ((off + len) > b.length))
            throw new IndexOutOfBoundsException("off: "+off+" len: "+len+" b.length: "+b.length);

        if (len == 0)
            return;

        final int newlen = this.len + len;
        if (newlen > this.array.length)
            expand(newlen);

        System.arraycopy(b, off, this.array, this.len, len);
        this.len = newlen;
    }

    public void append(final String str) {
        final String s = str != null ? str : "null";
        final int strlen = s.length();
        final int newlen = this.len + strlen;
        if (newlen > this.array.length)
            expand(newlen);

        s.getChars(0, strlen, this.array, this.len);
        this.len = newlen;
    }

    public void append(final CharArrayBuffer b, final int off, final int len) {
        if (b == null)
            return;
        append(b.array, off, len);
    }

    public void append(final CharArrayBuffer b) {
        if (b == null)
            return;
        append(b.array,0, b.len);
    }

    public void append(final char ch) {
        final int newlen = this.len + 1;
        if (newlen > this.array.length)
            expand(newlen);
        this.array[this.len] = ch;
        this.len = newlen;
    }

    public void append(final byte[] b, final int off, final int len) {
        if (b == null)
            return;

        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0) || ((off + len) > b.length))
            throw new IndexOutOfBoundsException("off: "+off+" len: "+len+" b.length: "+b.length);

        if (len == 0)
            return;

        final int oldlen = this.len;
        final int newlen = oldlen + len;
        if (newlen > this.array.length)
            expand(newlen);
        for (int i1 = off, i2 = oldlen; i2 < newlen; i1++, i2++)
            this.array[i2] = (char) (b[i1] & 0xff);
        this.len = newlen;
    }

    public void append(final ByteArrayBuffer b, final int off, final int len) {
        if (b == null)
            return;
        append(b.array(), off, len);
    }

    public void append(final Object obj) {
        append(String.valueOf(obj));
    }

    public void clear() {
        this.len = 0;
    }

    public char[] toCharArray() {
        final char[] b = new char[this.len];
        if (this.len > 0)
            System.arraycopy(this.array, 0, b, 0, this.len);
        return b;
    }

    @Override
    public char charAt(final int i) {
        return this.array[i];
    }

    public char[] array() {
        return this.array;
    }

    public int capacity() {
        return this.array.length;
    }

    @Override
    public int length() {
        return this.len;
    }

    public void ensureCapacity(final int required) {
        if (required <= 0)
            return;
        final int available = this.array.length - this.len;
        if (required > available)
            expand(this.len + required);
    }

    public void setLength(final int len) {
        if (len < 0 || len > this.array.length)
            throw new IndexOutOfBoundsException("len: "+len+" < 0 or > buffer len: "+this.array.length);
        this.len = len;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public boolean isFull() {
        return this.len == this.array.length;
    }

    public int indexOf(final int ch, final int from, final int to) {
        int beginIndex = from;
        if (beginIndex < 0)
            beginIndex = 0;
        int endIndex = to;
        if (endIndex > this.len)
            endIndex = this.len;
        if (beginIndex > endIndex)
            return -1;
        for (int i = beginIndex; i < endIndex; i++) {
            if (this.array[i] == ch)
                return i;
        }
        return -1;
    }

    public int indexOf(final int ch) {
        return indexOf(ch, 0, this.len);
    }

    /**
     * 截取并构造String返回
     */
    public String substring(final int beginIndex, final int endIndex) {
        if (beginIndex < 0)
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        if (endIndex > this.len)
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        if (beginIndex > endIndex)
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        return new String(this.array, beginIndex, endIndex - beginIndex);
    }

    private static boolean isWhitespace(final char ch) {
        return ch == Chars.SP || ch == Chars.HT || ch == Chars.CR || ch == Chars.LF;
    }

    /**
     * 截取区间并去掉首尾空格后，再构造成String返回
     */
    public String substringTrimmed(final int beginIndex, final int endIndex) {
        if (beginIndex < 0)
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        if (endIndex > this.len)
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        if (beginIndex > endIndex)
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);

        int beginIndex0 = beginIndex;
        int endIndex0 = endIndex;
        while (beginIndex0 < endIndex && isWhitespace(this.array[beginIndex0]))
            beginIndex0++;
        while (endIndex0 > beginIndex0 && isWhitespace(this.array[endIndex0 - 1]))
            endIndex0--;

        return new String(this.array, beginIndex0, endIndex0 - beginIndex0);
    }

    @Override
    public CharSequence subSequence(final int beginIndex, final int endIndex) {
        if (beginIndex < 0)
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        if (endIndex > this.len)
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        if (beginIndex > endIndex)
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        return CharBuffer.wrap(this.array, beginIndex, endIndex - beginIndex);
    }

    @Override
    public String toString() {
        return new String(this.array, 0, this.len);
    }
}
