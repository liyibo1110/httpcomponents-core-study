package com.github.liyibo1110.hc.core5.http;

/**
 * 表示不支持HTTP method的异常
 * @author liyibo
 * @date 2026-04-03 11:03
 */
public class MethodNotSupportedException extends ProtocolException {
    private static final long serialVersionUID = 1L;

    public MethodNotSupportedException(final String message) {
        super(message);
    }

    public MethodNotSupportedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
