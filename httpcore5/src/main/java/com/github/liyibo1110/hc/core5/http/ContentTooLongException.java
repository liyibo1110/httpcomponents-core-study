package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表content length过长的异常。
 * @author liyibo
 * @date 2026-04-03 13:50
 */
public class ContentTooLongException extends IOException {
    private static final long serialVersionUID = -924287689552495383L;

    public ContentTooLongException(final String message) {
        super(HttpException.clean(message));
    }

    public ContentTooLongException(final String format, final Object... args) {
        super(HttpException.clean(String.format(format, args)));
    }
}
