package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.BasicTokenIterator;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Iterator;

/**
 * ConnectionReuseStrategy策略接口的默认实现。
 *
 * 只有在：
 * 1、响应的消息边界明确。
 * 2、协议版本允许。
 * 3、双方都没有明确要求关闭连接
 * 才会复用连接，并不是只看有没有keep-alive头，HTTP/1.0和以下没有显式keep-alive头则不允许，但HTTP/1.1和以上版本默认倾向于复用。
 * @author liyibo
 * @date 2026-04-09 10:55
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultConnectionReuseStrategy implements ConnectionReuseStrategy {

    public static final DefaultConnectionReuseStrategy INSTANCE = new DefaultConnectionReuseStrategy();

    public DefaultConnectionReuseStrategy() {
        super();
    }

    @Override
    public boolean keepAlive(final HttpRequest request, final HttpResponse response, final HttpContext context) {
        Args.notNull(response, "HTTP response");

        // 检查request的Connection字段，如果对应值是close，说明不支持keepAlive
        if (request != null) {
            final Iterator<String> ti = new BasicTokenIterator(request.headerIterator(HttpHeaders.CONNECTION));
            while (ti.hasNext()) {
                final String token = ti.next();
                if (HeaderElements.CLOSE.equalsIgnoreCase(token))
                    return false;
            }
        }

        // If a HTTP 204 No Content response contains a Content-length with value > 0 or Transfer-Encoding header,
        // don't reuse the connection. This is to avoid getting out-of-sync if a misbehaved HTTP server
        // returns content as part of a HTTP 204 response.
        if (response.getCode() == HttpStatus.SC_NO_CONTENT) {
            final Header clh = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
            if (clh != null) {
                try {
                    final long contentLen = Long.parseLong(clh.getValue());
                    if (contentLen > 0)
                        return false;
                } catch (final NumberFormatException ex) {
                    // fall through
                }
            }
            if (response.containsHeader(HttpHeaders.TRANSFER_ENCODING))
                return false;
        }

        // Check for a self-terminating entity. If the end of the entity will
        // be indicated by closing the connection, there is no keep-alive.
        final Header teh = response.getFirstHeader(HttpHeaders.TRANSFER_ENCODING);
        if (teh != null) {
            if (!HeaderElements.CHUNKED_ENCODING.equalsIgnoreCase(teh.getValue()))
                return false;
        } else {
            final String method = request != null ? request.getMethod() : null;
            if (MessageSupport.canResponseHaveBody(method, response) && response.countHeaders(HttpHeaders.CONTENT_LENGTH) != 1)
                return false;
        }

        // Check for the "Connection" header. If that is absent, check for
        // the "Proxy-Connection" header. The latter is an unspecified and
        // broken but unfortunately common extension of HTTP.
        Iterator<Header> headerIterator = response.headerIterator(HttpHeaders.CONNECTION);
        if (!headerIterator.hasNext())
            headerIterator = response.headerIterator("Proxy-Connection");

        final ProtocolVersion ver = response.getVersion() != null ? response.getVersion() : context.getProtocolVersion();
        if (headerIterator.hasNext()) {
            if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                final Iterator<String> it = new BasicTokenIterator(headerIterator);
                while (it.hasNext()) {
                    final String token = it.next();
                    if (HeaderElements.CLOSE.equalsIgnoreCase(token))
                        return false;
                }
                return true;
            }
            final Iterator<String> it = new BasicTokenIterator(headerIterator);
            while (it.hasNext()) {
                final String token = it.next();
                if (HeaderElements.KEEP_ALIVE.equalsIgnoreCase(token))
                    return true;
            }
            return false;
        }
        return ver.greaterEquals(HttpVersion.HTTP_1_1);
    }
}
