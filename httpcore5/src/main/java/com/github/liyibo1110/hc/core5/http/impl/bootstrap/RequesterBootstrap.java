package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import com.github.liyibo1110.hc.core5.annotation.Experimental;
import com.github.liyibo1110.hc.core5.function.Callback;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.HttpHost;
import com.github.liyibo1110.hc.core5.http.config.CharCodingConfig;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.impl.DefaultAddressResolver;
import com.github.liyibo1110.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.impl.Http1StreamListener;
import com.github.liyibo1110.hc.core5.http.impl.HttpProcessors;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultBHttpClientConnectionFactory;
import com.github.liyibo1110.hc.core5.http.impl.io.HttpRequestExecutor;
import com.github.liyibo1110.hc.core5.http.io.HttpClientConnection;
import com.github.liyibo1110.hc.core5.http.io.HttpConnectionFactory;
import com.github.liyibo1110.hc.core5.http.io.SocketConfig;
import com.github.liyibo1110.hc.core5.http.io.ssl.DefaultTlsSetupHandler;
import com.github.liyibo1110.hc.core5.http.io.ssl.SSLSessionVerifier;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;
import com.github.liyibo1110.hc.core5.pool.ConnPoolListener;
import com.github.liyibo1110.hc.core5.pool.DefaultDisposalCallback;
import com.github.liyibo1110.hc.core5.pool.LaxConnPool;
import com.github.liyibo1110.hc.core5.pool.ManagedConnPool;
import com.github.liyibo1110.hc.core5.pool.PoolConcurrencyPolicy;
import com.github.liyibo1110.hc.core5.pool.PoolReusePolicy;
import com.github.liyibo1110.hc.core5.pool.StrictConnPool;
import com.github.liyibo1110.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

/**
 * ServerBootstrap组件对应的client side工厂，用来生成HttpRequester对象。
 * @author liyibo
 * @date 2026-04-13 17:41
 */
public class RequesterBootstrap {

    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connReuseStrategy;
    private SocketConfig socketConfig;
    private HttpConnectionFactory<? extends HttpClientConnection> connectFactory;
    private SSLSocketFactory sslSocketFactory;
    private Callback<SSLParameters> sslSetupHandler;
    private SSLSessionVerifier sslSessionVerifier;
    private int defaultMaxPerRoute;
    private int maxTotal;
    private Timeout timeToLive;
    private PoolReusePolicy poolReusePolicy;
    private PoolConcurrencyPolicy poolConcurrencyPolicy;
    private Http1StreamListener streamListener;
    private ConnPoolListener<HttpHost> connPoolListener;

    private RequesterBootstrap() {}

    public static RequesterBootstrap bootstrap() {
        return new RequesterBootstrap();
    }

    public final RequesterBootstrap setHttpProcessor(final HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final RequesterBootstrap setConnectionReuseStrategy(final ConnectionReuseStrategy connStrategy) {
        this.connReuseStrategy = connStrategy;
        return this;
    }

    public final RequesterBootstrap setSocketConfig(final SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    public final RequesterBootstrap setConnectionFactory(final HttpConnectionFactory<? extends HttpClientConnection> connectFactory) {
        this.connectFactory = connectFactory;
        return this;
    }

    public final RequesterBootstrap setSslContext(final SSLContext sslContext) {
        this.sslSocketFactory = sslContext != null ? sslContext.getSocketFactory() : null;
        return this;
    }

    public final RequesterBootstrap setSslSocketFactory(final SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public final RequesterBootstrap setSslSetupHandler(final Callback<SSLParameters> sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    public final RequesterBootstrap setSslSessionVerifier(final SSLSessionVerifier sslSessionVerifier) {
        this.sslSessionVerifier = sslSessionVerifier;
        return this;
    }

    public final RequesterBootstrap setDefaultMaxPerRoute(final int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public final RequesterBootstrap setMaxTotal(final int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public final RequesterBootstrap setTimeToLive(final Timeout timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public final RequesterBootstrap setPoolReusePolicy(final PoolReusePolicy poolReusePolicy) {
        this.poolReusePolicy = poolReusePolicy;
        return this;
    }

    @Experimental
    public final RequesterBootstrap setPoolConcurrencyPolicy(final PoolConcurrencyPolicy poolConcurrencyPolicy) {
        this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        return this;
    }

    public final RequesterBootstrap setStreamListener(final Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final RequesterBootstrap setConnPoolListener(final ConnPoolListener<HttpHost> connPoolListener) {
        this.connPoolListener = connPoolListener;
        return this;
    }

    public HttpRequester create() {
        final HttpRequestExecutor requestExecutor = new HttpRequestExecutor(
                HttpRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE,
                connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE,
                streamListener);
        final ManagedConnPool<HttpHost, HttpClientConnection> connPool;
        switch (poolConcurrencyPolicy != null ? poolConcurrencyPolicy : PoolConcurrencyPolicy.STRICT) {
            case LAX:
                connPool = new LaxConnPool<>(
                        defaultMaxPerRoute > 0 ? defaultMaxPerRoute : 20,
                        timeToLive,
                        poolReusePolicy,
                        new DefaultDisposalCallback<>(),
                        connPoolListener);
                break;
            case STRICT:
            default:
                connPool = new StrictConnPool<>(
                        defaultMaxPerRoute > 0 ? defaultMaxPerRoute : 20,
                        maxTotal > 0 ? maxTotal : 50,
                        timeToLive,
                        poolReusePolicy,
                        new DefaultDisposalCallback<>(),
                        connPoolListener);
                break;
        }
        return new HttpRequester(
                requestExecutor,
                httpProcessor != null ? httpProcessor : HttpProcessors.client(), connPool,
                socketConfig != null ? socketConfig : SocketConfig.DEFAULT,
                connectFactory != null ? connectFactory : new DefaultBHttpClientConnectionFactory(Http1Config.DEFAULT, CharCodingConfig.DEFAULT),
                sslSocketFactory,
                sslSetupHandler != null ? sslSetupHandler : new DefaultTlsSetupHandler(),
                sslSessionVerifier,
                DefaultAddressResolver.INSTANCE);
    }
}
