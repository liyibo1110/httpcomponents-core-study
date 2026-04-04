package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表分块流格式出现错误的异常。
 * @author liyibo
 * @date 2026-04-03 13:55
 */
public class MalformedChunkCodingException extends IOException {
    private static final long serialVersionUID = 2158560246948994524L;

    public MalformedChunkCodingException() {
        super();
    }

    public MalformedChunkCodingException(final String message) {
        super(message);
    }

    public MalformedChunkCodingException(final String format, final Object... args) {
        super(HttpException.clean(String.format(format, args)));
    }
}
