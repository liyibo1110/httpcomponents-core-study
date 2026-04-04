package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表了目标服务器未能返回有效的HTTP响应的异常。
 * @author liyibo
 * @date 2026-04-03 14:26
 */
public class NoHttpResponseException extends IOException {
    private static final long serialVersionUID = -7658940387386078766L;

    public NoHttpResponseException(final String message) {
        super(HttpException.clean(message));
    }

    public NoHttpResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
