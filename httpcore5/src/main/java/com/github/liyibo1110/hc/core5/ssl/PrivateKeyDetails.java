package com.github.liyibo1110.hc.core5.ssl;

import com.github.liyibo1110.hc.core5.util.Args;

import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Private key details.
 * @author liyibo
 * @date 2026-04-16 15:04
 */
public final class PrivateKeyDetails {
    private final String type;
    private final X509Certificate[] certChain;

    public PrivateKeyDetails(final String type, final X509Certificate[] certChain) {
        super();
        this.type = Args.notNull(type, "Private key type");
        this.certChain = certChain;
    }

    public String getType() {
        return type;
    }

    public X509Certificate[] getCertChain() {
        return certChain;
    }

    @Override
    public String toString() {
        return type + ':' + Arrays.toString(certChain);
    }
}
