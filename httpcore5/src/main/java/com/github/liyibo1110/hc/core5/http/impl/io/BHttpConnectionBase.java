package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.ConnectionClosedException;
import com.github.liyibo1110.hc.core5.http.ContentLengthStrategy;
import com.github.liyibo1110.hc.core5.http.EndpointDetails;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.impl.BasicEndpointDetails;
import com.github.liyibo1110.hc.core5.http.impl.BasicHttpConnectionMetrics;
import com.github.liyibo1110.hc.core5.http.impl.BasicHttpTransportMetrics;
import com.github.liyibo1110.hc.core5.http.io.BHttpConnection;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.http.io.SessionOutputBuffer;
import com.github.liyibo1110.hc.core5.http.io.entity.EmptyInputStream;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.io.Closer;
import com.github.liyibo1110.hc.core5.net.InetAddressUtils;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Timeout;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阻塞式HTTP连接的公共传输底座，不负责完整的request/response解析流程，为上层DefaultBHttpServerConnection、DefaultBHttpClientConnection提供通用能力：
 * 1、socket绑定与打开/关闭。
 * 2、输入输出缓冲区。
 * 3、entity输入/输出流创建。
 * 4、连接状态检测。
 * 5、timeout、SSL、endpoint、metrics等连接信息管理。
 * @author liyibo
 * @date 2026-04-09 15:27
 */
class BHttpConnectionBase implements BHttpConnection {

    private static final Timeout STALE_CHECK_TIMEOUT = Timeout.ofMilliseconds(1);
    final Http1Config http1Config;
    final SessionInputBufferImpl inBuffer;
    final SessionOutputBufferImpl outbuffer;
    final BasicHttpConnectionMetrics connMetrics;
    final AtomicReference<SocketHolder> socketHolderRef;

    /** 注意有延迟初始化的特性。 */
    private byte[] chunkedRequestBuffer;

    volatile ProtocolVersion version;
    volatile EndpointDetails endpointDetails;

    BHttpConnectionBase(final Http1Config http1Config, final CharsetDecoder charDecoder, final CharsetEncoder charEncoder) {
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        final BasicHttpTransportMetrics inTransportMetrics = new BasicHttpTransportMetrics();
        final BasicHttpTransportMetrics outTransportMetrics = new BasicHttpTransportMetrics();
        this.inBuffer = new SessionInputBufferImpl(inTransportMetrics, this.http1Config.getBufferSize(), -1, this.http1Config.getMaxLineLength(), charDecoder);
        this.outbuffer = new SessionOutputBufferImpl(outTransportMetrics, this.http1Config.getBufferSize(), this.http1Config.getChunkSizeHint(), charEncoder);
        this.connMetrics = new BasicHttpConnectionMetrics(inTransportMetrics, outTransportMetrics);
        this.socketHolderRef = new AtomicReference<>();
    }

    protected SocketHolder ensureOpen() throws IOException {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder == null)
            throw new ConnectionClosedException();
        return socketHolder;
    }

    /**
     * 将给定的Socket绑定到内部。
     */
    protected void bind(final Socket socket) throws IOException {
        Args.notNull(socket, "Socket");
        bind(new SocketHolder(socket));
    }

    protected void bind(final SocketHolder socketHolder) throws IOException {
        Args.notNull(socketHolder, "Socket holder");
        this.socketHolderRef.set(socketHolder);
        this.endpointDetails = null;
    }

    @Override
    public boolean isOpen() {
        return this.socketHolderRef.get() != null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version;
    }

    protected SocketHolder getSocketHolder() {
        return this.socketHolderRef.get();
    }

    /**
     * 根据len参数值不同，构建不同的OutputStream特定子类。
     */
    protected OutputStream createContentOutputStream(final long len,
                                                     final SessionOutputBuffer buffer,
                                                     final OutputStream outputStream,
                                                     final Supplier<List<? extends Header>> trailers) {
        if (len >= 0)
            return new ContentLengthOutputStream(buffer, outputStream, len);
        else if (len == ContentLengthStrategy.CHUNKED)
            return new ChunkedOutputStream(buffer, outputStream, getChunkedRequestBuffer(), trailers);
        else
            return new IdentityOutputStream(buffer, outputStream);
    }

    private byte[] getChunkedRequestBuffer() {
        // 延迟初始化
        if (chunkedRequestBuffer == null) {
            final int chunkSizeHint = this.http1Config.getChunkSizeHint();
            chunkedRequestBuffer = new byte[chunkSizeHint > 0 ? chunkSizeHint : 8192];
        }
        return chunkedRequestBuffer;
    }

    /**
     * 根据len参数值不同，构建不同的InputStream特定子类。
     */
    protected InputStream createContentInputStream(final long len, final SessionInputBuffer buffer, final InputStream inputStream) {
        if (len > 0)
            return new ContentLengthInputStream(buffer, inputStream, len);
        else if (len == 0)
            return EmptyInputStream.INSTANCE;
        else if (len == ContentLengthStrategy.CHUNKED)
            return new ChunkedInputStream(buffer, inputStream, this.http1Config);
        else
            return new IdentityInputStream(buffer, inputStream);
    }

    HttpEntity createIncomingEntity(final HttpMessage message,
                                    final SessionInputBuffer inBuffer,
                                    final InputStream inputStream,
                                    final long len) {
        return new IncomingHttpEntity(
                createContentInputStream(len, inBuffer, inputStream),
                len >= 0 ? len : -1, len == ContentLengthStrategy.CHUNKED,
                message.getFirstHeader(HttpHeaders.CONTENT_TYPE),
                message.getFirstHeader(HttpHeaders.CONTENT_ENCODING));
    }

    @Override
    public SocketAddress getRemoteAddress() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        return socketHolder != null ? socketHolder.getSocket().getRemoteSocketAddress() : null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        return socketHolder != null ? socketHolder.getSocket().getLocalSocketAddress() : null;
    }

    @Override
    public void setSocketTimeout(final Timeout timeout) {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                socketHolder.getSocket().setSoTimeout(Timeout.defaultsToDisabled(timeout).toMillisecondsIntBound());
            } catch (final SocketException ignore) {
                // It is not quite clear from the Sun's documentation if there are any
                // other legitimate cases for a socket exception to be thrown when setting
                // SO_TIMEOUT besides the socket being already closed
            }
        }
    }

    @Override
    public Timeout getSocketTimeout() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                return Timeout.ofMilliseconds(socketHolder.getSocket().getSoTimeout());
            } catch (final SocketException ignore) {

            }
        }
        return Timeout.DISABLED;
    }

    @Override
    public void close(final CloseMode closeMode) {
        final SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            try {
                if (closeMode == CloseMode.IMMEDIATE) {
                    // force abortive close (RST)
                    socket.setSoLinger(true, 0);
                }
            } catch (final IOException ignore) {

            } finally {
                Closer.closeQuietly(socket);
            }
        }
    }

    @Override
    public void close() throws IOException {
        final SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        if (socketHolder != null) {
            try (final Socket socket = socketHolder.getSocket()) {
                this.inBuffer.clear();
                this.outbuffer.flush(socketHolder.getOutputStream());
            }
        }
    }

    private int fillInputBuffer(final Timeout timeout) throws IOException {
        final SocketHolder socketHolder = ensureOpen();
        final Socket socket = socketHolder.getSocket();
        final int oldtimeout = socket.getSoTimeout();
        try {
            socket.setSoTimeout(timeout.toMillisecondsIntBound());
            return this.inBuffer.fillBuffer(socketHolder.getInputStream());
        } finally {
            socket.setSoTimeout(oldtimeout);
        }
    }

    protected boolean awaitInput(final Timeout timeout) throws IOException {
        if (this.inBuffer.hasBufferedData())
            return true;
        fillInputBuffer(timeout);
        return this.inBuffer.hasBufferedData();
    }

    @Override
    public boolean isDataAvailable(final Timeout timeout) throws IOException {
        ensureOpen();
        try {
            return awaitInput(timeout);
        } catch (final SocketTimeoutException ex) {
            return false;
        }
    }

    @Override
    public boolean isStale() throws IOException {
        if (!isOpen())
            return true;
        try {
            final int bytesRead = fillInputBuffer(STALE_CHECK_TIMEOUT);
            return bytesRead < 0;
        } catch (final SocketTimeoutException ex) {
            return false;
        } catch (final SocketException ex) {
            return true;
        }
    }

    @Override
    public void flush() throws IOException {
        final SocketHolder socketHolder = ensureOpen();
        this.outbuffer.flush(socketHolder.getOutputStream());
    }

    protected void incrementRequestCount() {
        this.connMetrics.incrementRequestCount();
    }

    protected void incrementResponseCount() {
        this.connMetrics.incrementResponseCount();
    }

    @Override
    public SSLSession getSSLSession() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession() : null;
        }
        return null;
    }

    @Override
    public EndpointDetails getEndpointDetails() {
        if (endpointDetails == null) {
            final SocketHolder socketHolder = this.socketHolderRef.get();
            if (socketHolder != null) {
                @SuppressWarnings("resource")
                final Socket socket = socketHolder.getSocket();
                Timeout socketTimeout;
                try {
                    socketTimeout = Timeout.ofMilliseconds(socket.getSoTimeout());
                } catch (final SocketException e) {
                    socketTimeout = Timeout.DISABLED;
                }
                endpointDetails = new BasicEndpointDetails(
                        socket.getRemoteSocketAddress(),
                        socket.getLocalSocketAddress(),
                        this.connMetrics,
                        socketTimeout);
            }
        }
        return endpointDetails;
    }

    @Override
    public String toString() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            final StringBuilder buffer = new StringBuilder();
            final SocketAddress remoteAddress = socket.getRemoteSocketAddress();
            final SocketAddress localAddress = socket.getLocalSocketAddress();
            if (remoteAddress != null && localAddress != null) {
                InetAddressUtils.formatAddress(buffer, localAddress);
                buffer.append("<->");
                InetAddressUtils.formatAddress(buffer, remoteAddress);
            }
            return buffer.toString();
        }
        return "[Not bound]";
    }
}
