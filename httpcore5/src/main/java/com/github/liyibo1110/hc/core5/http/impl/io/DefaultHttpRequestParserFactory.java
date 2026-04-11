package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestFactory;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParser;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParserFactory;
import com.github.liyibo1110.hc.core5.http.message.LazyLineParser;
import com.github.liyibo1110.hc.core5.http.message.LineParser;

/**
 * DefaultHttpRequestParser对象的工厂。
 * @author liyibo
 * @date 2026-04-10 11:30
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpRequestParserFactory implements HttpMessageParserFactory<ClassicHttpRequest> {

    public static final DefaultHttpRequestParserFactory INSTANCE = new DefaultHttpRequestParserFactory();

    private final LineParser lineParser;
    private final HttpRequestFactory<ClassicHttpRequest> requestFactory;

    public DefaultHttpRequestParserFactory(final LineParser lineParser, final HttpRequestFactory<ClassicHttpRequest> requestFactory) {
        super();
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
        this.requestFactory = requestFactory != null ? requestFactory : DefaultClassicHttpRequestFactory.INSTANCE;
    }

    public DefaultHttpRequestParserFactory() {
        this(null, null);
    }

    @Override
    public HttpMessageParser<ClassicHttpRequest> create(final Http1Config http1Config) {
        return new DefaultHttpRequestParser(this.lineParser, this.requestFactory, http1Config);
    }
}
