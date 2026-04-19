package com.github.liyibo1110.hc.core5.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 一种无需参考实际SSL上下文中配置的信任管理器即可建立证书可信度的策略。
 * 该接口可用于覆盖标准的JSSE证书验证流程。
 * @author liyibo
 * @date 2026-04-16 15:05
 */
public interface TrustStrategy {

    /**
     * 确定是否无需查询实际SSL上下文中配置的信任管理器即可信任该证书链。
     * 此方法可用于覆盖标准的JSSE证书验证流程。
     *
     * 请注意，如果此方法返回false，实际SSL上下文中配置的信任管理器仍可将该证书标记为受信任。
     */
    boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException;
}
