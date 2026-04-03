package com.github.liyibo1110.hc.core5.http;

/**
 * 表示发生了HTTP协议违规。
 * 例如：状态行或header格式错误、消息正文缺失等。
 * @author liyibo
 * @date 2026-04-02 17:16
 */
public class ProtocolException extends HttpException {
    private static final long serialVersionUID = -2143571074341228994L;

    public ProtocolException() {
        super();
    }

    public ProtocolException(final String message) {
        super(message);
    }

    public ProtocolException(final String format, final Object... args) {
        super(format, args);
    }

    public ProtocolException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
