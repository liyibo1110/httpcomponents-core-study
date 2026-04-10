package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.function.Resolver;
import com.github.liyibo1110.hc.core5.http.HttpHost;
import com.github.liyibo1110.hc.core5.http.URIScheme;

import java.net.InetSocketAddress;

/**
 * HttpHost -> InetSocketAddress的转换器。
 * @author liyibo
 * @date 2026-04-09 13:15
 */
public final class DefaultAddressResolver implements Resolver<HttpHost, InetSocketAddress> {

    public static final DefaultAddressResolver INSTANCE = new DefaultAddressResolver();

    @Override
    public InetSocketAddress resolve(final HttpHost host) {
        if (host == null)
            return null;
        int port = host.getPort();
        if(port < 0) {  // 只能用http默认端口
            final String scheme = host.getSchemeName();
            if (URIScheme.HTTP.same(scheme))
                port = 80;
            else if (URIScheme.HTTPS.same(scheme))
                port = 443;
        }
        return new InetSocketAddress(host.getHostName(), port);
    }
}
