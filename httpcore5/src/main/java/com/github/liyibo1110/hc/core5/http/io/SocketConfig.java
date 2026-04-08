package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TimeValue;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞式I/O network socket相关配置。
 * @author liyibo
 * @date 2026-04-07 14:05
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class SocketConfig {

    private static final Timeout DEFAULT_SOCKET_TIMEOUT = Timeout.ofMinutes(3);

    private final Timeout soTimeout;
    private final boolean soReuseAddress;
    private final TimeValue soLinger;
    private final boolean soKeepAlive;
    private final boolean tcpNoDelay;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int backlogSize;
    private final SocketAddress socksProxyAddress;

    SocketConfig(final Timeout soTimeout,
                 final boolean soReuseAddress,
                 final TimeValue soLinger,
                 final boolean soKeepAlive,
                 final boolean tcpNoDelay,
                 final int sndBufSize,
                 final int rcvBufSize,
                 final int backlogSize,
                 final SocketAddress socksProxyAddress) {
        super();
        this.soTimeout = soTimeout;
        this.soReuseAddress = soReuseAddress;
        this.soLinger = soLinger;
        this.soKeepAlive = soKeepAlive;
        this.tcpNoDelay = tcpNoDelay;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.backlogSize = backlogSize;
        this.socksProxyAddress = socksProxyAddress;
    }

    public Timeout getSoTimeout() {
        return soTimeout;
    }

    public boolean isSoReuseAddress() {
        return soReuseAddress;
    }

    public TimeValue getSoLinger() {
        return soLinger;
    }

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getSndBufSize() {
        return sndBufSize;
    }

    public int getRcvBufSize() {
        return rcvBufSize;
    }

    public int getBacklogSize() {
        return backlogSize;
    }

    public SocketAddress getSocksProxyAddress() {
        return this.socksProxyAddress;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[soTimeout=").append(this.soTimeout)
               .append(", soReuseAddress=").append(this.soReuseAddress)
               .append(", soLinger=").append(this.soLinger)
               .append(", soKeepAlive=").append(this.soKeepAlive)
               .append(", tcpNoDelay=").append(this.tcpNoDelay)
               .append(", sndBufSize=").append(this.sndBufSize)
               .append(", rcvBufSize=").append(this.rcvBufSize)
               .append(", backlogSize=").append(this.backlogSize)
               .append(", socksProxyAddress=").append(this.socksProxyAddress)
               .append("]");
        return builder.toString();
    }

    public static SocketConfig.Builder custom() {
        return new Builder();
    }

    public static SocketConfig.Builder copy(final SocketConfig config) {
        Args.notNull(config, "Socket config");
        return new Builder()
            .setSoTimeout(config.getSoTimeout())
            .setSoReuseAddress(config.isSoReuseAddress())
            .setSoLinger(config.getSoLinger())
            .setSoKeepAlive(config.isSoKeepAlive())
            .setTcpNoDelay(config.isTcpNoDelay())
            .setSndBufSize(config.getSndBufSize())
            .setRcvBufSize(config.getRcvBufSize())
            .setBacklogSize(config.getBacklogSize())
            .setSocksProxyAddress(config.getSocksProxyAddress());
    }

    public static class Builder {
        private Timeout soTimeout;
        private boolean soReuseAddress;
        private TimeValue soLinger;
        private boolean soKeepAlive;
        private boolean tcpNoDelay;
        private int sndBufSize;
        private int rcvBufSize;
        private int backlogSize;
        private SocketAddress socksProxyAddress;

        Builder() {
            this.soTimeout = DEFAULT_SOCKET_TIMEOUT;
            this.soReuseAddress = false;
            this.soLinger = TimeValue.NEG_ONE_SECOND;
            this.soKeepAlive = false;
            this.tcpNoDelay = true;
            this.sndBufSize = 0;
            this.rcvBufSize = 0;
            this.backlogSize = 0;
            this.socksProxyAddress = null;
        }

        public Builder setSoTimeout(final int soTimeout, final TimeUnit timeUnit) {
            this.soTimeout = Timeout.of(soTimeout, timeUnit);
            return this;
        }

        public Builder setSoTimeout(final Timeout soTimeout) {
            this.soTimeout = soTimeout;
            return this;
        }

        public Builder setSoReuseAddress(final boolean soReuseAddress) {
            this.soReuseAddress = soReuseAddress;
            return this;
        }

        public Builder setSoLinger(final int soLinger, final TimeUnit timeUnit) {
            this.soLinger = Timeout.of(soLinger, timeUnit);
            return this;
        }

        public Builder setSoLinger(final TimeValue soLinger) {
            this.soLinger = soLinger;
            return this;
        }

        public Builder setSoKeepAlive(final boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder setTcpNoDelay(final boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public Builder setSndBufSize(final int sndBufSize) {
            this.sndBufSize = sndBufSize;
            return this;
        }

        public Builder setRcvBufSize(final int rcvBufSize) {
            this.rcvBufSize = rcvBufSize;
            return this;
        }

        public Builder setBacklogSize(final int backlogSize) {
            this.backlogSize = backlogSize;
            return this;
        }

        public Builder setSocksProxyAddress(final SocketAddress socksProxyAddress) {
            this.socksProxyAddress = socksProxyAddress;
            return this;
        }

        public SocketConfig build() {
            return new SocketConfig(
                    Timeout.defaultsToDisabled(soTimeout),
                    soReuseAddress,
                    soLinger != null ? soLinger : TimeValue.NEG_ONE_SECOND,
                    soKeepAlive, tcpNoDelay, sndBufSize, rcvBufSize, backlogSize,
                    socksProxyAddress);
        }
    }
}
