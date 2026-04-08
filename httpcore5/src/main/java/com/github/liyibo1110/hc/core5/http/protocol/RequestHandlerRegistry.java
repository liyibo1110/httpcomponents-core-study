package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestMapper;
import com.github.liyibo1110.hc.core5.http.MisdirectedRequestException;
import com.github.liyibo1110.hc.core5.net.URIAuthority;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * 通用的request handler注册表，其中的handler可通过请求消息的属性进行解析。
 *
 * 本质是个两层路由表：
 * 1、按host分桶。
 * 2、再按URI path pattern分流。
 * 传入HttpRequest，返回T。
 *
 * RequestHandlerRegistry<T>
 * ├── primary : LookupRegistry<T>
 * │     ├── "/api/*" -> handlerA
 * │     ├── "/admin/*" -> handlerB
 * │     └── "*" -> defaultHandler
 * └── virtualMap : Map<hostname, LookupRegistry<T>>
 *       ├── "www.a.com"
 *       │     ├── "/api/*" -> handlerC
 *       │     └── "/static/*" -> handlerD
 *       └── "admin.a.com"
 *             ├── "/dashboard/*" -> handlerE
 *             └── "*" -> handlerF
 * @author liyibo
 * @date 2026-04-07 17:57
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class RequestHandlerRegistry<T> implements HttpRequestMapper<T> {

    private final static String LOCALHOST = "localhost";
    private final static String IP_127_0_0_1 = "127.0.0.1";

    private final String canonicalHostName;

    /** 第二层分流用 */
    private final Supplier<LookupRegistry<T>> registrySupplier;

    /** 第一层分流用，默认host使用的注册表 */
    private final LookupRegistry<T> primary;

    /** 第一层分流用，key是hostname */
    private final ConcurrentMap<String, LookupRegistry<T>> virtualMap;

    public RequestHandlerRegistry(final String canonicalHostName, final Supplier<LookupRegistry<T>> registrySupplier) {
        this.canonicalHostName = TextUtils.toLowerCase(Args.notNull(canonicalHostName, "Canonical hostname"));
        this.registrySupplier = registrySupplier != null ? registrySupplier : UriPatternMatcher::new;
        this.primary = this.registrySupplier.get();
        this.virtualMap = new ConcurrentHashMap<>();
    }

    public RequestHandlerRegistry(final String canonicalHostName, final UriPatternType patternType) {
        this(canonicalHostName, () -> UriPatternType.newMatcher(patternType));
    }

    public RequestHandlerRegistry(final UriPatternType patternType) {
        this(LOCALHOST, patternType);
    }

    public RequestHandlerRegistry() {
        this(LOCALHOST, UriPatternType.URI_PATTERN);
    }

    private LookupRegistry<T> getPatternMatcher(final String hostname) {
        if (hostname == null
                || hostname.equals(canonicalHostName)
                || hostname.equals(LOCALHOST)
                || hostname.equals(IP_127_0_0_1)) {
            return primary;
        }
        return virtualMap.get(hostname);
    }

    @Override
    public T resolve(final HttpRequest request, final HttpContext context) throws MisdirectedRequestException {
        final URIAuthority authority = request.getAuthority();
        final String key = authority != null ? TextUtils.toLowerCase(authority.getHostName()) : null;
        final LookupRegistry<T> patternMatcher = getPatternMatcher(key);
        if (patternMatcher == null)
            throw new MisdirectedRequestException("Not authoritative");

        String path = request.getPath();
        final int i = path.indexOf('?');
        if (i != -1)
            path = path.substring(0, i);
        return patternMatcher.lookup(path);

    }

    public void register(final String hostname, final String uriPattern, final T object) {
        Args.notBlank(uriPattern, "URI pattern");
        if (object == null)
            return;
        final String key = TextUtils.toLowerCase(hostname);
        if (hostname == null || hostname.equals(canonicalHostName) || hostname.equals(LOCALHOST)) {
            primary.register(uriPattern, object);
        } else {
            LookupRegistry<T> patternMatcher = virtualMap.get(key);
            if (patternMatcher == null) {
                final LookupRegistry<T> newPatternMatcher = registrySupplier.get();
                patternMatcher = virtualMap.putIfAbsent(key, newPatternMatcher);
                if (patternMatcher == null)
                    patternMatcher = newPatternMatcher;
            }
            patternMatcher.register(uriPattern, object);
        }
    }
}
