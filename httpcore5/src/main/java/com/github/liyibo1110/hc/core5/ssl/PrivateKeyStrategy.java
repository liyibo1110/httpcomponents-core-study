package com.github.liyibo1110.hc.core5.ssl;

import javax.net.ssl.SSLParameters;
import java.util.Map;

/**
 * 一种允许在SSL身份验证过程中选择别名的策略。
 * @author liyibo
 * @date 2026-04-16 15:04
 */
public interface PrivateKeyStrategy {

    /**
     * 确定用于SSL身份验证的密钥材料。
     */
    String chooseAlias(Map<String, PrivateKeyDetails> aliases, SSLParameters sslParameters);
}
