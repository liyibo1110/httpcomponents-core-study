package com.github.liyibo1110.hc.core5.http;

/**
 * 代表发生了HTTP异常
 * @author liyibo
 * @date 2026-04-02 17:13
 */
public class HttpException extends Exception {
    private static final int FIRST_VALID_CHAR = Chars.SP;
    private static final long serialVersionUID = -5437299376222011036L;

    /**
     * 将给定字符串中值小于32的字符转换为相应的十六进制代码，从而对字符串进行清理。
     */
    static String clean(final String message) {
        final char[] chars = message.toCharArray();
        int i;
        for (i = 0; i < chars.length; i++) {
            if (chars[i] < FIRST_VALID_CHAR)
                break;
        }
        if (i == chars.length)
            return message;
        final StringBuilder builder = new StringBuilder(chars.length * 2);
        for (i = 0; i < chars.length; i++) {
            final char ch = chars[i];
            if (ch < FIRST_VALID_CHAR) {
                builder.append("[0x");
                final String hexString = Integer.toHexString(i);
                if (hexString.length() == 1)
                    builder.append("0");
                builder.append(hexString);
                builder.append("]");
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public HttpException() {
        super();
    }

    public HttpException(final String message) {
        super(clean(message));
    }

    public HttpException(final String format, final Object... args) {
        super(HttpException.clean(String.format(format, args)));
    }

    public HttpException(final String message, final Throwable cause) {
        super(clean(message));
        initCause(cause);
    }
}
