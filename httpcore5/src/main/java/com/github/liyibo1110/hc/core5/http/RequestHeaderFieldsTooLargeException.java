package com.github.liyibo1110.hc.core5.http;

/**
 * 代表请求header字段长度或总字段大小违规的异常。
 * @author liyibo
 * @date 2026-04-03 14:30
 */
public class RequestHeaderFieldsTooLargeException extends ProtocolException {
    private static final long serialVersionUID = 1L;

    public RequestHeaderFieldsTooLargeException(final String message) {
        super(message);
    }

    public RequestHeaderFieldsTooLargeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
