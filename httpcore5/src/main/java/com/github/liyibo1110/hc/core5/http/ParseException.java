package com.github.liyibo1110.hc.core5.http;

/**
 * 代表了由于无法解析Message元素而触发的异常。
 * @author liyibo
 * @date 2026-04-03 14:29
 */
public class ParseException extends ProtocolException {
    private static final long serialVersionUID = -7288819855864183578L;

    private final int errorOffset;

    public ParseException() {
        super();
        this.errorOffset = -1;
    }

    public ParseException(final String message) {
        super(message);
        this.errorOffset = -1;
    }

    public ParseException(final String description, final CharSequence text, final int off, final int len, final int errorOffset) {
        super(description +
                (errorOffset >= 0 ? "; error at offset " + errorOffset : "") +
                (text != null && len < 1024 ? ": <" + text.subSequence(off, off + len) + ">" : ""));
        this.errorOffset = errorOffset;
    }

    public ParseException(final String description, final CharSequence text, final int off, final int len) {
        this(description, text, off, len, -1);
    }

    public int getErrorOffset() {
        return errorOffset;
    }
}
