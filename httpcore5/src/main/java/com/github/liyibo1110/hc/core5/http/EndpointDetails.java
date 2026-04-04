package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.net.InetAddressUtils;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.net.SocketAddress;

/**
 * HTTP连接endpoint的details信息。
 * @author liyibo
 * @date 2026-04-03 15:52
 */
public abstract class EndpointDetails implements HttpConnectionMetrics {
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final Timeout socketTimeout;

    protected EndpointDetails(final SocketAddress remoteAddress, final SocketAddress localAddress, final Timeout socketTimeout) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.socketTimeout = socketTimeout;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public Timeout getSocketTimeout() {
        return socketTimeout;
    }

    @Override
    public abstract long getRequestCount();

    @Override
    public abstract long getResponseCount();

    @Override
    public abstract long getSentBytesCount();

    @Override
    public abstract long getReceivedBytesCount();

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(90);
        InetAddressUtils.formatAddress(buffer, localAddress);
        buffer.append("<->");
        InetAddressUtils.formatAddress(buffer, remoteAddress);
        return buffer.toString();
    }
}
