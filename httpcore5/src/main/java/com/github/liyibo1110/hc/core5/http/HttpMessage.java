package com.github.liyibo1110.hc.core5.http;

/**
 * 由客户端发往服务器的请求，以及服务器发往客户端的响应组成。
 * 注意上面说的，这一层是请求和响应共享的部分方法
 * @author liyibo
 * @date 2026-04-02 17:20
 */
public interface HttpMessage extends MessageHeaders {

    /**
     * 对于传入的Message，它表示该Message传输时使用的协议版本。
     * 对于传出的Message，它表示应使用何种协议版本来传输该Message。
     */
    void setVersion(ProtocolVersion version);

    /**
     * 对于传入的Message，它表示该Message传输时使用的协议版本。
     * 对于传出的Message，它表示应使用何种协议版本来传输该Message。
     */
    ProtocolVersion getVersion();

    /**
     * 尾部追加header
     */
    void addHeader(Header header);

    void addHeader(String name, Object value);

    /**
     * 会覆盖name相同的header
     */
    void setHeader(Header header);

    void setHeader(String name, Object value);

    void setHeaders(Header... headers);

    boolean removeHeader(Header header);

    boolean removeHeaders(String name);
}
