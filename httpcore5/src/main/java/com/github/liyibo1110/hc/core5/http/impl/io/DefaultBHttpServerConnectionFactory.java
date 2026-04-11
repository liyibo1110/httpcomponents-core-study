package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ContentLengthStrategy;
import com.github.liyibo1110.hc.core5.http.config.CharCodingConfig;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.impl.CharCodingSupport;
import com.github.liyibo1110.hc.core5.http.io.HttpConnectionFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParserFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriterFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * 生成DefaultBHttpServerConnection对象的工厂。
 * @author liyibo
 * @date 2026-04-10 13:40
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultBHttpServerConnectionFactory implements HttpConnectionFactory<DefaultBHttpServerConnection> {

    private final String scheme;
    private final Http1Config http1Config;
    private final CharCodingConfig charCodingConfig;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory;
    private final HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory;

    public DefaultBHttpServerConnectionFactory(final String scheme,
                                               final Http1Config http1Config,
                                               final CharCodingConfig charCodingConfig,
                                               final ContentLengthStrategy incomingContentStrategy,
                                               final ContentLengthStrategy outgoingContentStrategy,
                                               final HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory,
                                               final HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory) {
        super();
        this.scheme = scheme;
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.requestParserFactory = requestParserFactory;
        this.responseWriterFactory = responseWriterFactory;
    }

    public DefaultBHttpServerConnectionFactory(final String scheme,
                                               final Http1Config http1Config,
                                               final CharCodingConfig charCodingConfig,
                                               final HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory,
                                               final HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory) {
        this(scheme, http1Config, charCodingConfig, null, null, requestParserFactory, responseWriterFactory);
    }

    public DefaultBHttpServerConnectionFactory(final String scheme, final Http1Config http1Config, final CharCodingConfig charCodingConfig) {
        this(scheme, http1Config, charCodingConfig, null, null, null, null);
    }

    @Override
    public DefaultBHttpServerConnection createConnection(final Socket socket) throws IOException {
        final DefaultBHttpServerConnection conn = new DefaultBHttpServerConnection(
                this.scheme,
                this.http1Config,
                CharCodingSupport.createDecoder(this.charCodingConfig),
                CharCodingSupport.createEncoder(this.charCodingConfig),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.requestParserFactory,
                this.responseWriterFactory);
        conn.bind(socket);
        return conn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String scheme;
        private Http1Config http1Config;
        private CharCodingConfig charCodingConfig;
        private ContentLengthStrategy incomingContentLengthStrategy;
        private ContentLengthStrategy outgoingContentLengthStrategy;
        private HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory;
        private HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory;

        private Builder() {}

        public Builder scheme(final String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder http1Config(final Http1Config http1Config) {
            this.http1Config = http1Config;
            return this;
        }

        public Builder charCodingConfig(final CharCodingConfig charCodingConfig) {
            this.charCodingConfig = charCodingConfig;
            return this;
        }

        public Builder incomingContentLengthStrategy(final ContentLengthStrategy incomingContentLengthStrategy) {
            this.incomingContentLengthStrategy = incomingContentLengthStrategy;
            return this;
        }

        public Builder outgoingContentLengthStrategy(final ContentLengthStrategy outgoingContentLengthStrategy) {
            this.outgoingContentLengthStrategy = outgoingContentLengthStrategy;
            return this;
        }

        public Builder requestParserFactory(final HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory) {
            this.requestParserFactory = requestParserFactory;
            return this;
        }

        public Builder responseWriterFactory(final HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory) {
            this.responseWriterFactory = responseWriterFactory;
            return this;
        }

        public DefaultBHttpServerConnectionFactory build() {
            return new DefaultBHttpServerConnectionFactory(
                    scheme,
                    http1Config,
                    charCodingConfig,
                    incomingContentLengthStrategy,
                    outgoingContentLengthStrategy,
                    requestParserFactory,
                    responseWriterFactory);
        }
    }
}
