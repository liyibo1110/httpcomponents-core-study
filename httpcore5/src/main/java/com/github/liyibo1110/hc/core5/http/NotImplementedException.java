package com.github.liyibo1110.hc.core5.http;

/**
 * 代表了HTTP协议中某项不支持或未实现功能的异常
 * @author liyibo
 * @date 2026-04-03 14:28
 */
public class NotImplementedException extends ProtocolException {
    private static final long serialVersionUID = 7929295893253266373L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(final String message) {
        super(message);
    }
}
