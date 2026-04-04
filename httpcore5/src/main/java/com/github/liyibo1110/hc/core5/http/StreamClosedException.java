package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表数据流已关闭的异常。
 * @author liyibo
 * @date 2026-04-03 14:35
 */
public class StreamClosedException extends IOException {
    private static final long serialVersionUID = 1L;

    public StreamClosedException() {
        super("Stream already closed");
    }

    public StreamClosedException(final String message) {
        super(message);
    }
}
