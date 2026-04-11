package com.github.liyibo1110.hc.core5.http.impl.io;

import java.io.IOException;

/**
 * 表示过早（异常）的响应。
 * @author liyibo
 * @date 2026-04-10 11:44
 */
class ResponseOutOfOrderException extends IOException {
    private static final long serialVersionUID = 7802054516041674757L;

    public ResponseOutOfOrderException() {
        super();
    }
}
