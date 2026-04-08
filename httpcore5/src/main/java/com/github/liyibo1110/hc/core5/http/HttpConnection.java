package com.github.liyibo1110.hc.core5.http;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * 通用的HTTP连接抽象，在客户端和服务器端均可使用。
 * @author liyibo
 * @date 2026-04-03 16:12
 */
public interface HttpConnection extends SocketModalCloseable {

    @Override
    void close() throws IOException;

    EndpointDetails getEndpointDetails();

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    ProtocolVersion getProtocolVersion();

    SSLSession getSSLSession();

    boolean isOpen();
}
