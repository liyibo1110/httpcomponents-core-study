package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ContentLengthStrategy;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpMessage;
import com.github.liyibo1110.hc.core5.http.NotImplementedException;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.util.Args;

/**
 * ContentLengthStrategy策略接口的默认实现类。
 * @author liyibo
 * @date 2026-04-09 11:58
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultContentLengthStrategy implements ContentLengthStrategy {

    public static final DefaultContentLengthStrategy INSTANCE = new DefaultContentLengthStrategy();

    public DefaultContentLengthStrategy() {}

    @Override
    public long determineLength(final HttpMessage message) throws HttpException {
        Args.notNull(message, "HTTP message");
        // Although Transfer-Encoding is specified as a list, in practice
        // it is either missing or has the single value "chunked". So we
        // treat it as a single-valued header here.
        final Header transferEncodingHeader = message.getFirstHeader(HttpHeaders.TRANSFER_ENCODING);
        if (transferEncodingHeader != null) {
            final String headerValue = transferEncodingHeader.getValue();
            if (HeaderElements.CHUNKED_ENCODING.equalsIgnoreCase(headerValue))
                return CHUNKED;
            throw new NotImplementedException("Unsupported transfer encoding: " + headerValue);
        }
        if (message.countHeaders(HttpHeaders.CONTENT_LENGTH) > 1)
            throw new ProtocolException("Multiple Content-Length headers");

        final Header contentLengthHeader = message.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if (contentLengthHeader != null) {
            final String s = contentLengthHeader.getValue();
            try {
                final long len = Long.parseLong(s);
                if (len < 0)
                    throw new ProtocolException("Negative content length: " + s);
                return len;
            } catch (final NumberFormatException e) {
                throw new ProtocolException("Invalid content length: " + s);
            }
        }
        return UNDEFINED;
    }
}
