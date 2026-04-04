package com.github.liyibo1110.hc.core5.io;

import com.github.liyibo1110.hc.core5.util.Timeout;

import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * 创建SocketTimeoutException对象的工厂。
 * @author liyibo
 * @date 2026-04-03 15:04
 */
public final class SocketTimeoutExceptionFactory {
    static public SocketTimeoutException create(final Timeout timeout) {
        return new SocketTimeoutException(Objects.toString(timeout));
    }
}
