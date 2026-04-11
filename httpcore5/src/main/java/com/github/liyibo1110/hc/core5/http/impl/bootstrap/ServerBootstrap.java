package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import com.github.liyibo1110.hc.core5.function.Callback;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.ExceptionListener;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.URIScheme;
import com.github.liyibo1110.hc.core5.http.config.CharCodingConfig;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.config.NamedElementChain;
import com.github.liyibo1110.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.impl.Http1StreamListener;
import com.github.liyibo1110.hc.core5.http.impl.HttpProcessors;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.impl.io.HttpService;
import com.github.liyibo1110.hc.core5.http.io.HttpConnectionFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpFilterHandler;
import com.github.liyibo1110.hc.core5.http.io.HttpRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.SocketConfig;
import com.github.liyibo1110.hc.core5.http.io.ssl.DefaultTlsSetupHandler;
import com.github.liyibo1110.hc.core5.http.io.support.BasicHttpServerExpectationDecorator;
import com.github.liyibo1110.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.support.HttpServerExpectationFilter;
import com.github.liyibo1110.hc.core5.http.io.support.HttpServerFilterChainElement;
import com.github.liyibo1110.hc.core5.http.io.support.HttpServerFilterChainRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.support.TerminalServerFilter;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;
import com.github.liyibo1110.hc.core5.http.protocol.LookupRegistry;
import com.github.liyibo1110.hc.core5.http.protocol.RequestHandlerRegistry;
import com.github.liyibo1110.hc.core5.http.protocol.UriPatternType;
import com.github.liyibo1110.hc.core5.net.InetAddressUtils;
import com.github.liyibo1110.hc.core5.util.Args;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * server的总入口
 * @author liyibo
 * @date 2026-04-10 17:33
 */
public class ServerBootstrap {
    private final List<HandlerEntry<HttpRequestHandler>> handlerList;
    private final List<FilterEntry<HttpFilterHandler>> filters;
    private String canonicalHostName;
    private LookupRegistry<HttpRequestHandler> lookupRegistry;
    private int listenerPort;
    private InetAddress localAddress;
    private SocketConfig socketConfig;
    private Http1Config http1Config;
    private CharCodingConfig charCodingConfig;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private HttpResponseFactory<ClassicHttpResponse> responseFactory;
    private ServerSocketFactory serverSocketFactory;
    private SSLContext sslContext;
    private Callback<SSLParameters> sslSetupHandler;
    private HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private ExceptionListener exceptionListener;
    private Http1StreamListener streamListener;

    private ServerBootstrap() {
        this.handlerList = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    public static ServerBootstrap bootstrap() {
        return new ServerBootstrap();
    }

    public final ServerBootstrap setCanonicalHostName(final String canonicalHostName) {
        this.canonicalHostName = canonicalHostName;
        return this;
    }

    public final ServerBootstrap setListenerPort(final int listenerPort) {
        this.listenerPort = listenerPort;
        return this;
    }

    public final ServerBootstrap setLocalAddress(final InetAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public final ServerBootstrap setSocketConfig(final SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    public final ServerBootstrap setHttp1Config(final Http1Config http1Config) {
        this.http1Config = http1Config;
        return this;
    }

    public final ServerBootstrap setCharCodingConfig(final CharCodingConfig charCodingConfig) {
        this.charCodingConfig = charCodingConfig;
        return this;
    }

    public final ServerBootstrap setHttpProcessor(final HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final ServerBootstrap setConnectionReuseStrategy(final ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final ServerBootstrap setResponseFactory(final HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.responseFactory = responseFactory;
        return this;
    }

    public final ServerBootstrap setLookupRegistry(final LookupRegistry<HttpRequestHandler> lookupRegistry) {
        this.lookupRegistry = lookupRegistry;
        return this;
    }

    public final ServerBootstrap register(final String uriPattern, final HttpRequestHandler requestHandler) {
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(requestHandler, "Supplier");
        handlerList.add(new HandlerEntry<>(null, uriPattern, requestHandler));
        return this;
    }

    public final ServerBootstrap registerVirtual(final String hostname, final String uriPattern, final HttpRequestHandler requestHandler) {
        Args.notBlank(hostname, "Hostname");
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(requestHandler, "Supplier");
        handlerList.add(new HandlerEntry<>(hostname, uriPattern, requestHandler));
        return this;
    }

    public final ServerBootstrap setConnectionFactory(final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public final ServerBootstrap setServerSocketFactory(final ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
        return this;
    }

    public final ServerBootstrap setSslContext(final SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public final ServerBootstrap setSslSetupHandler(final Callback<SSLParameters> sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    public final ServerBootstrap setExceptionListener(final ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
        return this;
    }

    public final ServerBootstrap setStreamListener(final Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final ServerBootstrap addFilterBefore(final String existing, final String name, final HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        filters.add(new FilterEntry<>(FilterEntry.Position.BEFORE, name, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap addFilterAfter(final String existing, final String name, final HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        filters.add(new FilterEntry<>(FilterEntry.Position.AFTER, name, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap replaceFilter(final String existing, final HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notNull(filterHandler, "Filter handler");
        filters.add(new FilterEntry<>(FilterEntry.Position.REPLACE, existing, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap addFilterFirst(final String name, final HttpFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        filters.add(new FilterEntry<>(FilterEntry.Position.FIRST, name, filterHandler, null));
        return this;
    }

    public final ServerBootstrap addFilterLast(final String name, final HttpFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        filters.add(new FilterEntry<>(FilterEntry.Position.LAST, name, filterHandler, null));
        return this;
    }

    /**
     * 核心方法，生成一个HttpServer对象。
     *
     * @return
     */
    public HttpServer create() {
        final RequestHandlerRegistry<HttpRequestHandler> handlerRegistry = new RequestHandlerRegistry<>(
                canonicalHostName != null ? canonicalHostName : InetAddressUtils.getCanonicalLocalHostName(),
                () -> lookupRegistry != null ? lookupRegistry : UriPatternType.newMatcher(UriPatternType.URI_PATTERN));
        for (final HandlerEntry<HttpRequestHandler> entry : handlerList)
            handlerRegistry.register(entry.hostname, entry.uriPattern, entry.handler);

        final HttpServerRequestHandler requestHandler;
        if (!filters.isEmpty()) {
            final NamedElementChain<HttpFilterHandler> filterChainDefinition = new NamedElementChain<>();
            filterChainDefinition.addLast(new TerminalServerFilter(handlerRegistry,
                            this.responseFactory != null ? this.responseFactory : DefaultClassicHttpResponseFactory.INSTANCE),
                    StandardFilter.MAIN_HANDLER.name());
            filterChainDefinition.addFirst(new HttpServerExpectationFilter(), StandardFilter.EXPECT_CONTINUE.name());

            for (final FilterEntry<HttpFilterHandler> entry : filters) {
                switch (entry.position) {
                    case AFTER:
                        filterChainDefinition.addAfter(entry.existing, entry.filterHandler, entry.name);
                        break;
                    case BEFORE:
                        filterChainDefinition.addBefore(entry.existing, entry.filterHandler, entry.name);
                        break;
                    case REPLACE:
                        filterChainDefinition.replace(entry.existing, entry.filterHandler);
                        break;
                    case FIRST:
                        filterChainDefinition.addFirst(entry.filterHandler, entry.name);
                        break;
                    case LAST:
                        // Don't add last, after TerminalServerFilter, as that does not delegate to the chain
                        // Instead, add the filter just before it, making it effectively the last filter
                        filterChainDefinition.addBefore(StandardFilter.MAIN_HANDLER.name(), entry.filterHandler, entry.name);
                        break;
                }
            }

            NamedElementChain<HttpFilterHandler>.Node current = filterChainDefinition.getLast();
            HttpServerFilterChainElement filterChain = null;
            while (current != null) {
                filterChain = new HttpServerFilterChainElement(current.getValue(), filterChain);
                current = current.getPrevious();
            }
            requestHandler = new HttpServerFilterChainRequestHandler(filterChain);
        } else {
            requestHandler = new BasicHttpServerExpectationDecorator(new BasicHttpServerRequestHandler(
                    handlerRegistry,
                    this.responseFactory != null ? this.responseFactory : DefaultClassicHttpResponseFactory.INSTANCE));
        }

        final HttpService httpService = new HttpService(
                this.httpProcessor != null ? this.httpProcessor : HttpProcessors.server(),
                requestHandler,
                this.connStrategy != null ? this.connStrategy : DefaultConnectionReuseStrategy.INSTANCE,
                this.streamListener);

        ServerSocketFactory serverSocketFactoryCopy = this.serverSocketFactory;
        if (serverSocketFactoryCopy == null) {
            if (this.sslContext != null)
                serverSocketFactoryCopy = this.sslContext.getServerSocketFactory();
            else
                serverSocketFactoryCopy = ServerSocketFactory.getDefault();
        }

        HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactoryCopy = this.connectionFactory;
        if (connectionFactoryCopy == null) {
            final String scheme = serverSocketFactoryCopy instanceof SSLServerSocketFactory ? URIScheme.HTTPS.id : URIScheme.HTTP.id;
            connectionFactoryCopy = new DefaultBHttpServerConnectionFactory(scheme, this.http1Config, this.charCodingConfig);
        }

        return new HttpServer(
                Math.max(this.listenerPort, 0),
                httpService,
                this.localAddress,
                this.socketConfig != null ? this.socketConfig : SocketConfig.DEFAULT,
                serverSocketFactoryCopy,
                connectionFactoryCopy,
                sslSetupHandler != null ? sslSetupHandler : new DefaultTlsSetupHandler(),
                this.exceptionListener != null ? this.exceptionListener : ExceptionListener.NO_OP);
    }
}