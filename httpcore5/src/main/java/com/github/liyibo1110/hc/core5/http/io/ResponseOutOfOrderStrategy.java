package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * 用于确定客户端应以何种频率检查是否收到“未按序响应”的一种策略。
 *
 * 所谓“未按序响应”，是指在服务器尚未读取完整请求之前就发送的响应。
 * 如果客户端未能检查是否收到提前响应，那么当客户端或服务器一方超时后，在写入请求实体时可能会抛出java.net.SocketException或java.net.SocketTimeoutException异常。
 * @author liyibo
 * @date 2026-04-07 15:17
 */
@Internal
public interface ResponseOutOfOrderStrategy {

    /**
     * 在向套接字调用java.io.OutputStream进行每次写入之前，会调用该方法。
     * 传入已发送的字节数，以及如果此次检查未遇到顺序错误响应时将要进行的写入操作的大小。
     */
    boolean isEarlyResponseDetected(
            ClassicHttpRequest request,
            HttpClientConnection connection,
            InputStream inputStream,
            long totalBytesSent,
            long nextWriteSize) throws IOException;
}
