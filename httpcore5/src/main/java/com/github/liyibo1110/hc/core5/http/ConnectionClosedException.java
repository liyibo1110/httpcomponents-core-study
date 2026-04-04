package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表connection已被关闭的异常。
 * @author liyibo
 * @date 2026-04-03 13:44
 */
public class ConnectionClosedException extends IOException {
    private static final long serialVersionUID = 617550366255636674L;

    public ConnectionClosedException() {
        super("Connection is closed");
    }

    public ConnectionClosedException(final String message) {
        super(HttpException.clean(message));
    }

    public ConnectionClosedException(final String format, final Object... args) {
        super(HttpException.clean(String.format(format, args)));
    }

    public ConnectionClosedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
