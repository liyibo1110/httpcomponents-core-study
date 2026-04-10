package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.EndpointDetails;
import com.github.liyibo1110.hc.core5.http.HttpConnectionMetrics;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.net.SocketAddress;

/**
 * EndpointDetails的基础实现类（就是多了个HttpConnectionMetrics组件）。
 * @author liyibo
 * @date 2026-04-09 10:38
 */
public final class BasicEndpointDetails extends EndpointDetails {

    private final HttpConnectionMetrics metrics;

    public BasicEndpointDetails(final SocketAddress remoteAddress, final SocketAddress localAddress,
                                final HttpConnectionMetrics metrics, final Timeout socketTimeout) {
        super(remoteAddress, localAddress, socketTimeout);
        this.metrics = metrics;
    }

    @Override
    public long getRequestCount() {
        return metrics != null ? metrics.getRequestCount() : 0;
    }

    @Override
    public long getResponseCount() {
        return metrics != null ? metrics.getResponseCount() : 0;
    }

    @Override
    public long getSentBytesCount() {
        return metrics != null ? metrics.getSentBytesCount() : 0;
    }

    @Override
    public long getReceivedBytesCount() {
        return metrics != null ? metrics.getReceivedBytesCount() : 0;
    }
}
