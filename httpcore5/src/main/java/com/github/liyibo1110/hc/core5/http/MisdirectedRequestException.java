package com.github.liyibo1110.hc.core5.http;

/**
 * 代表了请求发送错误（即该服务器无权处理此请求）的异常
 * @author liyibo
 * @date 2026-04-03 14:24
 */
public class MisdirectedRequestException extends ProtocolException {
    private static final long serialVersionUID = 1L;

    public MisdirectedRequestException() {
        super();
    }

    public MisdirectedRequestException(final String message) {
        super(message);
    }
}
