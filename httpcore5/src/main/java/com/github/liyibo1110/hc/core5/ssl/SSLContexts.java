package com.github.liyibo1110.hc.core5.ssl;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * SSLContext 工厂方法。
 * 请注意：Oracle JSSE对SSLContext#init(KeyManager[], TrustManager[], SecureRandom) 的默认实现支持多个密钥管理器和信任管理器，
 * 但实际上只会使用第一个匹配的类型，例如请参见：SSLContext.html#init。
 * @author liyibo
 * @date 2026-04-16 14:52
 */
public final class SSLContexts {

    private SSLContexts() {}

    /**
     * 根据标准的JSSE信任材料（位于security properties目录中的cacerts文件）创建默认工厂。
     * 系统属性不予考虑。
     */
    public static SSLContext createDefault() throws SSLInitializationException {
        try {
            final SSLContext sslContext = SSLContext.getInstance(SSLContextBuilder.TLS);
            sslContext.init(null, null, null);
            return sslContext;
        } catch (final NoSuchAlgorithmException | KeyManagementException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        }
    }

    /**
     * 根据系统属性创建默认的SSL上下文。
     * 此方法通过调用SSLContext.getInstance(“Default”)来获取默认的SSL上下文。
     * 请注意默认算法自Java 6起才受支持。如果默认算法不可用，此方法将回退到createDefault()。
     */
    public static SSLContext createSystemDefault() throws SSLInitializationException {
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException ex) {
            return createDefault();
        }
    }

    public static SSLContextBuilder custom() {
        return SSLContextBuilder.create();
    }
}
