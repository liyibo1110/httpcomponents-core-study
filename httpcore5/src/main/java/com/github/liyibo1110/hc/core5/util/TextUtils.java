package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.annotation.Internal;

import java.util.Locale;

/**
 * @author liyibo
 * @date 2026-04-02 13:27
 */
public final class TextUtils {

    private TextUtils() {}

    /**
     * 为null或者length真为0
     */
    public static boolean isEmpty(final CharSequence s) {
        return length(s) == 0;
    }

    /**
     * isEmpty基础上，也不能只有空格字符（和commons的StringUtils的语义应该是一样的）
     */
    public static boolean isBlank(final CharSequence s) {
        final int strLen = length(s);
        if (strLen == 0)
            return true;

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(s.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * 返回字符序列的长度，为null则返回0
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * 是否包含空格，为null或empty则返回false
     */
    public static boolean containsBlanks(final CharSequence s) {
        final int strLen = length(s);
        if (strLen == 0)
            return false;

        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i)))
                return true;
        }
        return false;
    }

    /**
     * byte[] -> String（16进制格式）
     */
    public static String toHexString(final byte[] bytes) {
        if (bytes == null)
            return null;

        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            final int unsignedB = bytes[i] & 0xff;
            if (unsignedB < 16)
                buffer.append('0');
            buffer.append(Integer.toHexString(unsignedB));
        }
        return buffer.toString();
    }

    /**
     * 字符串转小写
     */
    public static String toLowerCase(final String s) {
        if (s == null)
            return null;
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * 将字符转换为字节，并在转换前过滤掉不可见字符和非ASCII字符
     */
    @Internal
    public static byte castAsByte(final int c) {
        if ((c >= 0x20 && c <= 0x7E) || // Visible ASCII
                (c >= 0xA0 && c <= 0xFF) || // Visible ISO-8859-1
                c == 0x09) {               // TAB
            return (byte) c;
        } else {
            return '?';
        }
    }
}
