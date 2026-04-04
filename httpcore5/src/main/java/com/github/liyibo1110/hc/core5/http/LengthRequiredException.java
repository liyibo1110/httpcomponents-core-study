package com.github.liyibo1110.hc.core5.http;

/**
 * 代表缺少content length而被拒绝的异常。
 * @author liyibo
 * @date 2026-04-03 13:43
 */
public class LengthRequiredException extends ProtocolException {
    private static final long serialVersionUID = 1049109801075840707L;

    public LengthRequiredException() {
        super("Length required");
    }

    public LengthRequiredException(final String message) {
        super(message);
    }
}
