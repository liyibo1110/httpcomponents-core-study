package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.HttpConnectionMetrics;
import com.github.liyibo1110.hc.core5.http.io.HttpTransportMetrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * HttpConnectionMetrics接口的基础实现类。
 * @author liyibo
 * @date 2026-04-09 10:52
 */
public final class BasicHttpConnectionMetrics implements HttpConnectionMetrics {

    private final HttpTransportMetrics inTransportMetric;
    private final HttpTransportMetrics outTransportMetric;
    private final AtomicLong requestCount;
    private final AtomicLong responseCount;

    public BasicHttpConnectionMetrics(final HttpTransportMetrics inTransportMetric, final HttpTransportMetrics outTransportMetric) {
        super();
        this.inTransportMetric = inTransportMetric;
        this.outTransportMetric = outTransportMetric;
        this.requestCount = new AtomicLong(0);
        this.responseCount = new AtomicLong(0);
    }

    @Override
    public long getReceivedBytesCount() {
        if (this.inTransportMetric != null)
            return this.inTransportMetric.getBytesTransferred();
        return -1;
    }

    @Override
    public long getSentBytesCount() {
        if (this.outTransportMetric != null)
            return this.outTransportMetric.getBytesTransferred();
        return -1;
    }

    @Override
    public long getRequestCount() {
        return this.requestCount.get();
    }

    public void incrementRequestCount() {
        this.requestCount.incrementAndGet();
    }

    @Override
    public long getResponseCount() {
        return this.responseCount.get();
    }

    public void incrementResponseCount() {
        this.responseCount.incrementAndGet();
    }
}
