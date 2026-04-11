package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.message.LineParser;
import com.github.liyibo1110.hc.core5.http.message.StatusLine;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;

/**
 * 一个从SessionInputBuffer实例获取输入的HTTP响应解析器。
 * @author liyibo
 * @date 2026-04-10 11:33
 */
public class DefaultHttpResponseParser extends AbstractMessageParser<ClassicHttpResponse> {

    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public DefaultHttpResponseParser(final LineParser lineParser,
                                     final HttpResponseFactory<ClassicHttpResponse> responseFactory,
                                     final Http1Config http1Config) {
        super(lineParser, http1Config);
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public DefaultHttpResponseParser(final Http1Config http1Config) {
        this(null, null, http1Config);
    }

    public DefaultHttpResponseParser() {
        this(Http1Config.DEFAULT);
    }

    @Override
    protected ClassicHttpResponse createMessage(final CharArrayBuffer buffer) throws IOException, HttpException {
        final StatusLine statusline = getLineParser().parseStatusLine(buffer);
        final ClassicHttpResponse response = this.responseFactory.newHttpResponse(statusline.getStatusCode(), statusline.getReasonPhrase());
        response.setVersion(statusline.getProtocolVersion());
        return response;
    }
}
