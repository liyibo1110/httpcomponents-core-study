package com.github.liyibo1110.hc.core5.util;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 可扩容的byte数组容器
 * @author liyibo
 * @date 2026-04-02 13:47
 */
public final class ByteArrayBuffer implements Serializable {
    private static final long serialVersionUID = 4359112959524048036L;

    /** 底层数组 */
    private byte[] array;

    /** 暗示了不会清理旧数据 */
    private int len;

    public ByteArrayBuffer(final int capacity) {
        super();
        Args.notNegative(capacity, "Buffer capacity");
        this.array = new byte[capacity];
    }

    /**
     * 扩容到给定newlen，但至少会扩一倍
     */
    private void expand(final int newlen) {
        final byte[] newArray = new byte[Math.max(this.array.length << 1, newlen)];
        System.arraycopy(this.array, 0, newArray, 0, this.len);
        this.array = newArray;
    }

    /**
     * 将b从off写len个字节到array
     */
    public void append(final byte[] b, final int off, final int len) {
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

    /**
     * 数组尾追加一个字节
     */
    public void append(final int b) {
        final int newlen = this.len + 1;
        if (newlen > this.array.length)
            expand(newlen);

        this.array[this.len] = (byte)b;
        this.len = newlen;
    }

    public void append(final char[] b, final int off, final int len) {
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
            this.array[i2] = TextUtils.castAsByte(b[i1]);

        this.len = newlen;
    }

    public void append(final CharArrayBuffer b, final int off, final int len) {
        if (b == null)
            return;
        append(b.array(), off, len);
    }

    public void append(final ByteBuffer buffer) {
        if (buffer == null)
            return;

        final int bufferLength = buffer.remaining();
        if (bufferLength > 0) {
            final int newLength = this.len + bufferLength;
            if (newLength > this.array.length)
                expand(newLength);

            buffer.get(this.array, this.len, bufferLength);
            this.len = newLength;
        }
    }

    /**
     * 直接改len，不移除旧数据
     */
    public void clear() {
        this.len = 0;
    }

    /**
     * 复制数组并将其返回
     */
    public byte[] toByteArray() {
        final byte[] b = new byte[this.len];
        if (this.len > 0)
            System.arraycopy(this.array, 0, b, 0, this.len);
        return b;
    }

    /**
     * 按给定下标返回对应字节
     */
    public int byteAt(final int i) {
        return this.array[i];
    }

    /**
     * 注意是返回数组本身的长度，即当前容量
     */
    public int capacity() {
        return this.array.length;
    }

    /**
     * 返回len字段
     */
    public int length() {
        return this.len;
    }

    /**
     * 检查够不够存required个位置了，不够就先扩容。
     */
    public void ensureCapacity(final int required) {
        if (required <= 0)
            return;

        final int available = this.array.length - this.len;
        if (required > available)
            expand(this.len + required);
    }

    public byte[] array() {
        return this.array;
    }

    /**
     * 直接改len字段值
     */
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

    public int indexOf(final byte b, final int from, final int to) {
        int beginIndex = from;
        if (beginIndex < 0)
            beginIndex = 0;
        int endIndex = to;
        if (endIndex > this.len)
            endIndex = this.len;
        if (beginIndex > endIndex)
            return -1;
        for (int i = beginIndex; i < endIndex; i++) {
            if (this.array[i] == b)
                return i;
        }
        return -1;
    }

    public int indexOf(final byte b) {
        return indexOf(b, 0, this.len);
    }
}
