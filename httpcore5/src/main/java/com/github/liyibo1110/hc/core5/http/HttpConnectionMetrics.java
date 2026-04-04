package com.github.liyibo1110.hc.core5.http;

/**
 * HttpConnection的访问统计者
 * @author liyibo
 * @date 2026-04-03 15:46
 */
public interface HttpConnectionMetrics {

    /**
     * 通过该连接传输的请求数，若不可用则返回0。
     */
    long getRequestCount();

    /**
     * 通过该连接传输的响应数，若不可用则返回0。
     */
    long getResponseCount();

    /**
     * 通过该连接发送的字节数，若不可用则返回0。
     */
    long getSentBytesCount();

    /**
     * 通过该连接接收的字节数，若不可用则返回0。
     */
    long getReceivedBytesCount();
}
