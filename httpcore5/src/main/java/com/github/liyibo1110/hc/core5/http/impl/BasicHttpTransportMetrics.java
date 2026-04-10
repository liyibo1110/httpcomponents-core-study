package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.io.HttpTransportMetrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * HttpTransportMetrics接口的基础实现类。
 * @author liyibo
 * @date 2026-04-09 10:51
 */
public class BasicHttpTransportMetrics implements HttpTransportMetrics {

    private final AtomicLong bytesTransferred;

    public BasicHttpTransportMetrics() {
        this.bytesTransferred = new AtomicLong(0);
    }

    @Override
    public long getBytesTransferred() {
        return this.bytesTransferred.get();
    }

    public void incrementBytesTransferred(final long count) {
        this.bytesTransferred.addAndGet(count);
    }
}
