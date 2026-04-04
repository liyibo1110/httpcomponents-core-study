package com.github.liyibo1110.hc.core5.http;

/**
 * 代表使用了不受支持的HTTP协议版本的异常。
 * @author liyibo
 * @date 2026-04-03 14:36
 */
public class UnsupportedHttpVersionException extends ProtocolException {
    private static final long serialVersionUID = -1348448090193107031L;

    public UnsupportedHttpVersionException() {
        super();
    }

    public UnsupportedHttpVersionException(final ProtocolVersion protocolVersion) {
        super("Unsupported version: " + protocolVersion);
    }

    public UnsupportedHttpVersionException(final String message) {
        super(message);
    }
}
