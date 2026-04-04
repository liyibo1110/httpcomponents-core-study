package com.github.liyibo1110.hc.core5.http;

/**
 * 代表分块流中存在截断分块的异常。
 * @author liyibo
 * @date 2026-04-03 14:35
 */
public class TruncatedChunkException extends MalformedChunkCodingException {
    private static final long serialVersionUID = -23506263930279460L;

    public TruncatedChunkException(final String message) {
        super(HttpException.clean(message));
    }

    public TruncatedChunkException(final String format, final Object... args) {
        super(format, args);
    }
}
