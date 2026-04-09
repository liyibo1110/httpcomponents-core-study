package com.github.liyibo1110.hc.core5.http.io.ssl;

import com.github.liyibo1110.hc.core5.http.HttpHost;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * 可用于自定义TLS/SSL会话验证的回调接口。
 * @author liyibo
 * @date 2026-04-08 15:21
 */
public interface SSLSessionVerifier {
    void verify(HttpHost endpoint, SSLSession sslSession) throws SSLException;
}
