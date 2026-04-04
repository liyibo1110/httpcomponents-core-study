package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Internal;

/**
 * ConnectionClosedException的子类，用于指示因连接已关闭而无法执行请求。
 * 通常因该异常失败的请求可以安全地重新执行。
 * @author liyibo
 * @date 2026-04-03 14:32
 */
@Internal
public class RequestNotExecutedException extends ConnectionClosedException {

    public RequestNotExecutedException() {
        super("Connection is closed");
    }

    public RequestNotExecutedException(final String message) {
        super(message);
    }
}
