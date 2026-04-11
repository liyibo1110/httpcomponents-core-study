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
import com.github.liyibo1110.hc.core5.http.io.ResponseOutOfOrderStrategy;

import java.io.IOException;
import java.net.Socket;

/**
 * 生成DefaultBHttpClientConnection对象的工厂。
 * @author liyibo
 * @date 2026-04-10 13:38
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultBHttpClientConnectionFactory implements HttpConnectionFactory<DefaultBHttpClientConnection> {

    private final Http1Config http1Config;
    private final CharCodingConfig charCodingConfig;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final ResponseOutOfOrderStrategy responseOutOfOrderStrategy;
    private final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory;
    private final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory;

    private DefaultBHttpClientConnectionFactory(final Http1Config http1Config,
                                                final CharCodingConfig charCodingConfig,
                                                final ContentLengthStrategy incomingContentStrategy,
                                                final ContentLengthStrategy outgoingContentStrategy,
                                                final ResponseOutOfOrderStrategy responseOutOfOrderStrategy,
                                                final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory,
                                                final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.responseOutOfOrderStrategy = responseOutOfOrderStrategy;
        this.requestWriterFactory = requestWriterFactory;
        this.responseParserFactory = responseParserFactory;
    }

    public DefaultBHttpClientConnectionFactory(final Http1Config http1Config,
                                               final CharCodingConfig charCodingConfig,
                                               final ContentLengthStrategy incomingContentStrategy,
                                               final ContentLengthStrategy outgoingContentStrategy,
                                               final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory,
                                               final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this(http1Config, charCodingConfig, incomingContentStrategy, outgoingContentStrategy, null,
             requestWriterFactory, responseParserFactory);
    }

    public DefaultBHttpClientConnectionFactory(final Http1Config http1Config,
                                               final CharCodingConfig charCodingConfig,
                                               final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory,
                                               final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this(http1Config, charCodingConfig, null, null, requestWriterFactory, responseParserFactory);
    }

    public DefaultBHttpClientConnectionFactory(final Http1Config http1Config, final CharCodingConfig charCodingConfig) {
        this(http1Config, charCodingConfig, null, null, null, null);
    }

    public DefaultBHttpClientConnectionFactory() {
        this(null, null, null, null, null, null);
    }

    @Override
    public DefaultBHttpClientConnection createConnection(final Socket socket) throws IOException {
        final DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
                this.http1Config,
                CharCodingSupport.createDecoder(this.charCodingConfig),
                CharCodingSupport.createEncoder(this.charCodingConfig),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.responseOutOfOrderStrategy,
                this.requestWriterFactory,
                this.responseParserFactory);
        conn.bind(socket);
        return conn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Http1Config http1Config;
        private CharCodingConfig charCodingConfig;
        private ContentLengthStrategy incomingContentLengthStrategy;
        private ContentLengthStrategy outgoingContentLengthStrategy;
        private ResponseOutOfOrderStrategy responseOutOfOrderStrategy;
        private HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory;
        private HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory;

        private Builder() {}

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

        public Builder responseOutOfOrderStrategy(final ResponseOutOfOrderStrategy responseOutOfOrderStrategy) {
            this.responseOutOfOrderStrategy = responseOutOfOrderStrategy;
            return this;
        }

        public Builder requestWriterFactory(final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory) {
            this.requestWriterFactory = requestWriterFactory;
            return this;
        }

        public Builder responseParserFactory(final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
            this.responseParserFactory = responseParserFactory;
            return this;
        }

        public DefaultBHttpClientConnectionFactory build() {
            return new DefaultBHttpClientConnectionFactory(
                    http1Config,
                    charCodingConfig,
                    incomingContentLengthStrategy,
                    outgoingContentLengthStrategy,
                    responseOutOfOrderStrategy,
                    requestWriterFactory,
                    responseParserFactory);
        }
    }
}
