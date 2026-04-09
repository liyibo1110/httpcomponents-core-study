package com.github.liyibo1110.hc.core5.http.io.ssl;

import com.github.liyibo1110.hc.core5.function.Callback;
import com.github.liyibo1110.hc.core5.http.ssl.TLS;
import com.github.liyibo1110.hc.core5.http.ssl.TlsCiphers;

import javax.net.ssl.SSLParameters;

/**
 * 默认TLS会话建立处理器。
 * @author liyibo
 * @date 2026-04-08 15:23
 */
public final class DefaultTlsSetupHandler implements Callback<SSLParameters> {

    @Override
    public void execute(final SSLParameters sslParameters) {
        sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
        sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
    }
}
