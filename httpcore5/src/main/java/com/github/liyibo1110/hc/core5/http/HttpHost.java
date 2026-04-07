package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.net.Host;
import com.github.liyibo1110.hc.core5.net.NamedEndpoint;
import com.github.liyibo1110.hc.core5.net.URIAuthority;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.LangUtils;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * 用于存储描述与主机建立HTTP连接所需所有详细信息的组件。
 * 其中包括远程主机名、端口和协议方案。
 * @author liyibo
 * @date 2026-04-03 16:13
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class HttpHost implements NamedEndpoint, Serializable {
    private static final long serialVersionUID = -7529410654042457626L;

    /** 默认schema是http */
    public static final URIScheme DEFAULT_SCHEME = URIScheme.HTTP;

    private final String schemeName;
    private final Host host;
    private final InetAddress address;

    public HttpHost(final String scheme, final InetAddress address, final String hostname, final int port) {
        Args.containsNoBlanks(hostname, "Host name");
        this.schemeName = scheme != null ? TextUtils.toLowerCase(scheme) : DEFAULT_SCHEME.id;
        this.host = new Host(hostname, port);
        this.address = address;
    }

    public HttpHost(final String scheme, final String hostname, final int port) {
        this(scheme, null, hostname, port);
    }

    public HttpHost(final String hostname, final int port) {
        this(null, hostname, port);
    }

    public HttpHost(final String scheme, final String hostname) {
        this(scheme, hostname, -1);
    }

    public static HttpHost create(final String s) throws URISyntaxException {
        Args.notEmpty(s, "HTTP Host");
        String text = s;
        String scheme = null;
        final int schemeIdx = text.indexOf("://");
        if (schemeIdx > 0) {
            scheme = text.substring(0, schemeIdx);
            if (TextUtils.containsBlanks(scheme))
                throw new URISyntaxException(s, "scheme contains blanks");
            text = text.substring(schemeIdx + 3);
        }
        final Host host = Host.create(text);
        return new HttpHost(scheme, host);
    }

    public static HttpHost create(final URI uri) {
        final String scheme = uri.getScheme();
        return new HttpHost(scheme != null ? scheme : URIScheme.HTTP.getId(), uri.getHost(), uri.getPort());
    }

    public HttpHost(final String hostname) {
        this(null, hostname, -1);
    }

    public HttpHost(final String scheme, final InetAddress address, final int port) {
        this(scheme, Args.notNull(address,"Inet address"), address.getHostName(), port);
    }

    public HttpHost(final InetAddress address, final int port) {
        this(null, address, port);
    }

    public HttpHost(final InetAddress address) {
        this(null, address, -1);
    }

    public HttpHost(final String scheme, final NamedEndpoint namedEndpoint) {
        this(scheme, Args.notNull(namedEndpoint, "Named endpoint").getHostName(), namedEndpoint.getPort());
    }

    @Deprecated
    public HttpHost(final URIAuthority authority) {
        this(null, authority);
    }

    @Override
    public String getHostName() {
        return this.host.getHostName();
    }

    @Override
    public int getPort() {
        return this.host.getPort();
    }

    public String getSchemeName() {
        return this.schemeName;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public String toURI() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.schemeName);
        buffer.append("://");
        buffer.append(this.host.toString());
        return buffer.toString();
    }

    public String toHostString() {
        return this.host.toString();
    }

    @Override
    public String toString() {
        return toURI();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof HttpHost) {
            final HttpHost that = (HttpHost) obj;
            return this.schemeName.equals(that.schemeName)
                    && this.host.equals(that.host)
                    && Objects.equals(this.address, that.address);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.schemeName);
        hash = LangUtils.hashCode(hash, this.host);
        hash = LangUtils.hashCode(hash, this.address);
        return hash;
    }
}
