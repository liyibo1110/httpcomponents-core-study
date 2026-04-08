package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.http.EndpointDetails;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import javax.net.ssl.SSLSession;
import java.util.Objects;

/**
 * HttpContext的实现，为可由用户设置的属性提供了便捷的设置器，并为可读取的属性提供了获取器。
 * @author liyibo
 * @date 2026-04-07 16:11
 */
public class HttpCoreContext implements HttpContext {

    /** 表示实际连接端点详细信息的EndpointDetails对象的属性名称。 */
    public static final String CONNECTION_ENDPOINT  = HttpContext.RESERVED_PREFIX + "connection-endpoint";

    /** SSLSession对象的属性名称，该对象表示实际连接端点的详细信息。 */
    public static final String SSL_SESSION = HttpContext.RESERVED_PREFIX + "ssl-session";

    /** HttpRequest对象中表示实际HTTP请求的属性名称 */
    public static final String HTTP_REQUEST = HttpContext.RESERVED_PREFIX + "request";

    /** HttpResponse对象的属性名称，该对象表示实际的HTTP响应。 */
    public static final String HTTP_RESPONSE = HttpContext.RESERVED_PREFIX + "response";

    public static HttpCoreContext create() {
        return new HttpCoreContext();
    }

    public static HttpCoreContext adapt(final HttpContext context) {
        if (context == null)
            return new HttpCoreContext();
        if (context instanceof HttpCoreContext)
            return (HttpCoreContext) context;
        return new HttpCoreContext(context);
    }

    private final HttpContext context;

    public HttpCoreContext(final HttpContext context) {
        super();
        this.context = Objects.requireNonNull(context);
    }

    public HttpCoreContext() {
        super();
        this.context = new BasicHttpContext();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.context.getProtocolVersion();
    }

    @Override
    public void setProtocolVersion(final ProtocolVersion version) {
        this.context.setProtocolVersion(version);
    }

    @Override
    public Object getAttribute(final String id) {
        return context.getAttribute(id);
    }

    @Override
    public Object setAttribute(final String id, final Object obj) {
        return context.setAttribute(id, obj);
    }

    @Override
    public Object removeAttribute(final String id) {
        return context.removeAttribute(id);
    }

    public <T> T getAttribute(final String attributeName, final Class<T> clazz) {
        Args.notNull(clazz, "Attribute class");
        final Object obj = getAttribute(attributeName);
        if (obj == null)
            return null;
        return clazz.cast(obj);
    }

    public SSLSession getSSLSession() {
        return getAttribute(SSL_SESSION, SSLSession.class);
    }

    public EndpointDetails getEndpointDetails() {
        return getAttribute(CONNECTION_ENDPOINT, EndpointDetails.class);
    }

    public HttpRequest getRequest() {
        return getAttribute(HTTP_REQUEST, HttpRequest.class);
    }

    public HttpResponse getResponse() {
        return getAttribute(HTTP_RESPONSE, HttpResponse.class);
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
