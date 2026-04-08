package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpContext的默认实现。
 * 请注意，如果父context不支持多线程，则该类的实例可能不支持多线程。
 * @author liyibo
 * @date 2026-04-07 16:14
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class BasicHttpContext implements HttpContext {

    private final HttpContext parentContext;

    private final Map<String, Object> map;

    private ProtocolVersion version;

    public BasicHttpContext() {
        this(null);
    }

    public BasicHttpContext(final HttpContext parentContext) {
        super();
        this.map = new ConcurrentHashMap<>();
        this.parentContext = parentContext;
    }

    @Override
    public Object getAttribute(final String id) {
        Args.notNull(id, "Id");
        Object obj = this.map.get(id);
        // 自己没有，再尝试从父context找
        if (obj == null && this.parentContext != null)
            obj = this.parentContext.getAttribute(id);
        return obj;
    }

    @Override
    public Object setAttribute(final String id, final Object obj) {
        Args.notNull(id, "Id");
        if (obj != null)
            return this.map.put(id, obj);
        return this.map.remove(id);
    }

    @Override
    public Object removeAttribute(final String id) {
        Args.notNull(id, "Id");
        return this.map.remove(id);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version != null ? this.version : HttpVersion.DEFAULT;
    }

    @Override
    public void setProtocolVersion(final ProtocolVersion version) {
        this.version = version;
    }

    public void clear() {
        this.map.clear();
    }

    @Override
    public String toString() {
        return this.map.toString();
    }
}
