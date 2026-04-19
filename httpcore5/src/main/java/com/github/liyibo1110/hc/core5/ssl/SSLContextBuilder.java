package com.github.liyibo1110.hc.core5.ssl;

import com.github.liyibo1110.hc.core5.util.Args;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * SSLContext对象的builder。
 * 请注意：Oracle JSSE对SSLContext.init(KeyManager[], TrustManager[], SecureRandom)的默认实现支持多个密钥管理器和信任管理器，
 * 但实际上只会使用第一个匹配的类型。例如请参阅：SSLContext.html#init
 * @author liyibo
 * @date 2026-04-16 15:07
 */
public class SSLContextBuilder {
    static final String TLS   = "TLS";

    private String protocol;
    private final Set<KeyManager> keyManagers;
    private String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private String keyStoreType = KeyStore.getDefaultType();
    private final Set<TrustManager> trustManagers;
    private String trustManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private SecureRandom secureRandom;
    private Provider provider;
    private Provider tsProvider;
    private Provider ksProvider;

    private static final KeyManager[] EMPTY_KEY_MANAGER_ARRAY = {};

    private static final TrustManager[] EMPTY_TRUST_MANAGER_ARRAY = {};

    public static SSLContextBuilder create() {
        return new SSLContextBuilder();
    }

    public SSLContextBuilder() {
        this.keyManagers = new LinkedHashSet<>();
        this.trustManagers = new LinkedHashSet<>();
    }

    public SSLContextBuilder setProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public SSLContextBuilder setProvider(final Provider provider) {
        this.provider = provider;
        return this;
    }

    public SSLContextBuilder setProvider(final String name) {
        this.provider = Security.getProvider(name);
        return this;
    }

    public SSLContextBuilder setTrustStoreProvider(final Provider provider) {
        this.tsProvider = provider;
        return this;
    }

    public SSLContextBuilder setTrustStoreProvider(final String name) throws NoSuchProviderException {
        this.tsProvider = requireNonNullProvider(name);
        return this;
    }

    public SSLContextBuilder setKeyStoreProvider(final Provider provider) {
        this.ksProvider = provider;
        return this;
    }

    public SSLContextBuilder setKeyStoreProvider(final String name) throws NoSuchProviderException {
        this.ksProvider = requireNonNullProvider(name);
        return this;
    }

    public SSLContextBuilder setKeyStoreType(final String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    public SSLContextBuilder setKeyManagerFactoryAlgorithm(final String keyManagerFactoryAlgorithm) {
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
        return this;
    }

    public SSLContextBuilder setTrustManagerFactoryAlgorithm(final String trustManagerFactoryAlgorithm) {
        this.trustManagerFactoryAlgorithm = trustManagerFactoryAlgorithm;
        return this;
    }

    public SSLContextBuilder setSecureRandom(final SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(final KeyStore trustStore, final TrustStrategy trustStrategy)
            throws NoSuchAlgorithmException, KeyStoreException {

        final String alg = trustManagerFactoryAlgorithm == null
                ? TrustManagerFactory.getDefaultAlgorithm()
                : trustManagerFactoryAlgorithm;

        final TrustManagerFactory tmFactory = tsProvider == null
                ? TrustManagerFactory.getInstance(alg)
                : TrustManagerFactory.getInstance(alg, tsProvider);

        tmFactory.init(trustStore);
        final TrustManager[] tms = tmFactory.getTrustManagers();
        if (tms != null) {
            if (trustStrategy != null) {
                for (int i = 0; i < tms.length; i++) {
                    final TrustManager tm = tms[i];
                    if (tm instanceof X509TrustManager)
                        tms[i] = new TrustManagerDelegate((X509TrustManager) tm, trustStrategy);
                }
            }
            Collections.addAll(this.trustManagers, tms);
        }
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(final Path file)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, null);
    }

    public SSLContextBuilder loadTrustMaterial(final Path file, final char[] storePassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, storePassword, null);
    }

    public SSLContextBuilder loadTrustMaterial(final Path file,
                                               final char[] storePassword,
                                               final TrustStrategy trustStrategy,
                                               final OpenOption... openOptions)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(file, "Truststore file");
        return loadTrustMaterial(loadKeyStore(file, storePassword, openOptions), trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(final TrustStrategy trustStrategy) throws NoSuchAlgorithmException, KeyStoreException {
        return loadTrustMaterial(null, trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(final File file, final char[] storePassword, final TrustStrategy trustStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(file, "Truststore file");
        return loadTrustMaterial(file.toPath(), storePassword, trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(final File file, final char[] storePassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, storePassword, null);
    }

    public SSLContextBuilder loadTrustMaterial(final File file) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(file, null);
    }

    public SSLContextBuilder loadTrustMaterial(final URL url, final char[] storePassword, final TrustStrategy trustStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(url, "Truststore URL");
        return loadTrustMaterial(loadKeyStore(url, storePassword), trustStrategy);
    }

    public SSLContextBuilder loadTrustMaterial(final URL url, final char[] storePassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return loadTrustMaterial(url, storePassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(final KeyStore keyStore,
                                             final char[] keyPassword,
                                             final PrivateKeyStrategy aliasStrategy) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {

        final String alg = keyManagerFactoryAlgorithm == null
                ? KeyManagerFactory.getDefaultAlgorithm()
                : keyManagerFactoryAlgorithm;

        final KeyManagerFactory kmFactory = ksProvider == null
                ? KeyManagerFactory.getInstance(alg)
                : KeyManagerFactory.getInstance(alg, ksProvider);

        kmFactory.init(keyStore, keyPassword);
        final KeyManager[] kms = kmFactory.getKeyManagers();
        if (kms != null) {
            if (aliasStrategy != null) {
                for (int i = 0; i < kms.length; i++) {
                    final KeyManager km = kms[i];
                    if (km instanceof X509ExtendedKeyManager)
                        kms[i] = new KeyManagerDelegate((X509ExtendedKeyManager) km, aliasStrategy);
                }
            }
            Collections.addAll(keyManagers, kms);
        }
        return this;
    }

    public SSLContextBuilder loadKeyMaterial(final Path file,
                                             final char[] storePassword,
                                             final char[] keyPassword,
                                             final OpenOption... openOptions)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(file, storePassword, keyPassword, null, openOptions);
    }

    public SSLContextBuilder loadKeyMaterial(final Path file,
                                             final char[] storePassword,
                                             final char[] keyPassword,
                                             final PrivateKeyStrategy aliasStrategy,
                                             final OpenOption... openOptions)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(file, "Keystore file");
        return loadKeyMaterial(loadKeyStore(file, storePassword, openOptions), keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(final KeyStore keyStore, final char[] keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return loadKeyMaterial(keyStore, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(final File file,
                                             final char[] storePassword,
                                             final char[] keyPassword,
                                             final PrivateKeyStrategy aliasStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(file, "Keystore file");
        return loadKeyMaterial(file.toPath(), storePassword, keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(final File file, final char[] storePassword, final char[] keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(file, storePassword, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(final URL url,
                                             final char[] storePassword,
                                             final char[] keyPassword,
                                             final PrivateKeyStrategy aliasStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(url, "Keystore URL");
        return loadKeyMaterial(loadKeyStore(url, storePassword), keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(final URL url, final char[] storePassword, final char[] keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(url, storePassword, keyPassword, null);
    }

    protected void initSSLContext(final SSLContext sslContext,
                                  final Collection<KeyManager> keyManagers,
                                  final Collection<TrustManager> trustManagers,
                                  final SecureRandom secureRandom) throws KeyManagementException {
        sslContext.init(!keyManagers.isEmpty() ? keyManagers.toArray(EMPTY_KEY_MANAGER_ARRAY) : null,
                        !trustManagers.isEmpty() ? trustManagers.toArray(EMPTY_TRUST_MANAGER_ARRAY) : null,
                        secureRandom);
    }

    private KeyStore loadKeyStore(final Path file, final char[] password, final OpenOption... openOptions)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (final InputStream inputStream = Files.newInputStream(file, openOptions)) {
            keyStore.load(inputStream, password);
        }
        return keyStore;
    }

    private KeyStore loadKeyStore(final URL url, final char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (final InputStream inputStream = url.openStream()) {
            keyStore.load(inputStream, password);
        }
        return keyStore;
    }

    public SSLContext build() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext;
        final String protocolStr = this.protocol != null ? this.protocol : TLS;
        if (this.provider != null)
            sslContext = SSLContext.getInstance(protocolStr, this.provider);
        else
            sslContext = SSLContext.getInstance(protocolStr);

        initSSLContext(sslContext, keyManagers, trustManagers, secureRandom);
        return sslContext;
    }

    static class TrustManagerDelegate implements X509TrustManager {
        private final X509TrustManager trustManager;
        private final TrustStrategy trustStrategy;

        TrustManagerDelegate(final X509TrustManager trustManager, final TrustStrategy trustStrategy) {
            this.trustManager = trustManager;
            this.trustStrategy = trustStrategy;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            this.trustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            if (!this.trustStrategy.isTrusted(chain, authType))
                this.trustManager.checkServerTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.trustManager.getAcceptedIssuers();
        }
    }

    static class KeyManagerDelegate extends X509ExtendedKeyManager {
        private final X509ExtendedKeyManager keyManager;
        private final PrivateKeyStrategy aliasStrategy;

        KeyManagerDelegate(final X509ExtendedKeyManager keyManager, final PrivateKeyStrategy aliasStrategy) {
            this.keyManager = keyManager;
            this.aliasStrategy = aliasStrategy;
        }

        @Override
        public String[] getClientAliases(final String keyType, final Principal[] issuers) {
            return this.keyManager.getClientAliases(keyType, issuers);
        }

        public Map<String, PrivateKeyDetails> getClientAliasMap(final String[] keyTypes, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<>();
            for (final String keyType: keyTypes)
                putPrivateKeyDetails(validAliases, keyType, this.keyManager.getClientAliases(keyType, issuers));
            return validAliases;
        }

        public Map<String, PrivateKeyDetails> getServerAliasMap(final String keyType, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<>();
            putPrivateKeyDetails(validAliases, keyType, this.keyManager.getServerAliases(keyType, issuers));
            return validAliases;
        }

        private void putPrivateKeyDetails(final Map<String, PrivateKeyDetails> validAliases, final String keyType, final String[] aliases) {
            if (aliases != null) {
                for (final String alias: aliases)
                    validAliases.put(alias, new PrivateKeyDetails(keyType, this.keyManager.getCertificateChain(alias)));
            }
        }

        @Override
        public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket instanceof SSLSocket
                    ? ((SSLSocket) socket).getSSLParameters()
                    : null);
        }

        @Override
        public String[] getServerAliases(final String keyType, final Principal[] issuers) {
            return this.keyManager.getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket instanceof SSLSocket ? ((SSLSocket) socket).getSSLParameters() : null);
        }

        @Override
        public X509Certificate[] getCertificateChain(final String alias) {
            return this.keyManager.getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(final String alias) {
            return this.keyManager.getPrivateKey(alias);
        }

        @Override
        public String chooseEngineClientAlias(final String[] keyTypes, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, sslEngine.getSSLParameters());
        }

        @Override
        public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, sslEngine.getSSLParameters());
        }
    }

    private Provider requireNonNullProvider(final String name) throws NoSuchProviderException {
        final Provider provider = Security.getProvider(name);
        if (provider == null)
            throw new NoSuchProviderException(name);
        return provider;
    }

    @Override
    public String toString() {
        return "[provider=" + provider + ", protocol=" + protocol + ", keyStoreType=" + keyStoreType
                + ", keyManagerFactoryAlgorithm=" + keyManagerFactoryAlgorithm + ", keyManagers=" + keyManagers
                + ", trustManagerFactoryAlgorithm=" + trustManagerFactoryAlgorithm + ", trustManagers=" + trustManagers
                + ", secureRandom=" + secureRandom + "]";
    }
}
