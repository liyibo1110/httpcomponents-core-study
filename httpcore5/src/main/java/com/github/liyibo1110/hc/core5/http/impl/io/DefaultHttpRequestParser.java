package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpRequestFactory;
import com.github.liyibo1110.hc.core5.http.MessageConstraintException;
import com.github.liyibo1110.hc.core5.http.RequestHeaderFieldsTooLargeException;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.io.SessionInputBuffer;
import com.github.liyibo1110.hc.core5.http.message.LineParser;
import com.github.liyibo1110.hc.core5.http.message.RequestLine;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一个从SessionInputBuffer实例获取输入的HTTP请求解析器。
 * @author liyibo
 * @date 2026-04-10 11:14
 */
public class DefaultHttpRequestParser extends AbstractMessageParser<ClassicHttpRequest> {

    private final HttpRequestFactory<ClassicHttpRequest> requestFactory;

    public DefaultHttpRequestParser(final LineParser lineParser,
                                    final HttpRequestFactory<ClassicHttpRequest> requestFactory,
                                    final Http1Config http1Config) {
        super(lineParser, http1Config);
        this.requestFactory = requestFactory != null ? requestFactory : DefaultClassicHttpRequestFactory.INSTANCE;
    }

    public DefaultHttpRequestParser(final Http1Config http1Config) {
        this(null, null, http1Config);
    }

    public DefaultHttpRequestParser() {
        this(Http1Config.DEFAULT);
    }

    @Override
    public ClassicHttpRequest parse(final SessionInputBuffer buffer, final InputStream inputStream)
            throws IOException, HttpException {
        try {
            return super.parse(buffer, inputStream);
        } catch (final MessageConstraintException ex) {
            throw new RequestHeaderFieldsTooLargeException(ex.getMessage(), ex);
        }
    }

    @Override
    protected ClassicHttpRequest createMessage(final CharArrayBuffer buffer) throws IOException, HttpException {
        final RequestLine requestLine = getLineParser().parseRequestLine(buffer);
        final ClassicHttpRequest request = this.requestFactory.newHttpRequest(requestLine.getMethod(), requestLine.getUri());
        request.setVersion(requestLine.getProtocolVersion());
        return request;
    }
}
