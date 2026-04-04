package com.github.liyibo1110.hc.core5.http;

import java.io.InterruptedIOException;

/**
 * 代表请求连接发生超时的异常。
 * @author liyibo
 * @date 2026-04-03 13:46
 */
public class ConnectionRequestTimeoutException extends InterruptedIOException {

    public ConnectionRequestTimeoutException() {
        super();
    }

    public ConnectionRequestTimeoutException(final String message) {
        super(HttpException.clean(message));
    }
}
