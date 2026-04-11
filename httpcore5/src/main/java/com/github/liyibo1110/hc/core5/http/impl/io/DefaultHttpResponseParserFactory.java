package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParser;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParserFactory;
import com.github.liyibo1110.hc.core5.http.message.LazyLaxLineParser;
import com.github.liyibo1110.hc.core5.http.message.LineParser;

/**
 * DefaultHttpResponseParser对象的工厂。
 * @author liyibo
 * @date 2026-04-10 11:37
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseParserFactory implements HttpMessageParserFactory<ClassicHttpResponse> {

    public static final DefaultHttpResponseParserFactory INSTANCE = new DefaultHttpResponseParserFactory();

    private final LineParser lineParser;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public DefaultHttpResponseParserFactory(final LineParser lineParser, final HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        super();
        this.lineParser = lineParser != null ? lineParser : LazyLaxLineParser.INSTANCE;
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public DefaultHttpResponseParserFactory() {
        this(null, null);
    }

    @Override
    public HttpMessageParser<ClassicHttpResponse> create(final Http1Config http1Config) {
        return new DefaultHttpResponseParser(this.lineParser, this.responseFactory, http1Config);
    }
}
