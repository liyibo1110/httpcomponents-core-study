package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表HTTP协议错误，导致实际的HTTP数据流不可靠的异常。
 * @author liyibo
 * @date 2026-04-03 13:54
 */
public class HttpStreamResetException extends IOException {
    private static final long serialVersionUID = 1L;

    public HttpStreamResetException(final String message) {
        super(message);
    }

    public HttpStreamResetException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
