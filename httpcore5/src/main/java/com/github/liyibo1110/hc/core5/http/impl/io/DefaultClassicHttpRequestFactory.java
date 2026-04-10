package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestFactory;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpRequest;

import java.net.URI;

/**
 * 创建BasicClassicHttpRequest对象的工厂。
 * @author liyibo
 * @date 2026-04-09 15:22
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultClassicHttpRequestFactory implements HttpRequestFactory<ClassicHttpRequest> {

    public static final DefaultClassicHttpRequestFactory INSTANCE = new DefaultClassicHttpRequestFactory();

    @Override
    public ClassicHttpRequest newHttpRequest(final String method, final URI uri) {
        return new BasicClassicHttpRequest(method, uri);
    }

    @Override
    public ClassicHttpRequest newHttpRequest(final String method, final String uri) {
        return new BasicClassicHttpRequest(method, uri);
    }
}
