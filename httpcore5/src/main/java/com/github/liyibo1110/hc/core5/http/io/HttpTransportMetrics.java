package com.github.liyibo1110.hc.core5.http.io;

/**
 * 传输层的相关统计接口。
 * @author liyibo
 * @date 2026-04-07 14:53
 */
public interface HttpTransportMetrics {

    /**
     * 返回已传输的字节数
     */
    long getBytesTransferred();
}
