package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liyibo
 * @date 2026-04-07 17:44
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class UriPatternOrderedMatcher<T> implements LookupRegistry<T> {

    private final Map<String, T> map;

    public UriPatternOrderedMatcher() {
        super();
        this.map = new LinkedHashMap<>();
    }

    public synchronized Set<Map.Entry<String, T>> entrySet() {
        return new HashSet<>(map.entrySet());
    }

    @Override
    public synchronized void register(final String pattern, final T obj) {
        Args.notNull(pattern, "URI request pattern");
        this.map.put(pattern, obj);
    }

    @Override
    public synchronized void unregister(final String pattern) {
        if (pattern == null)
            return;
        this.map.remove(pattern);
    }

    @Override
    public synchronized T lookup(final String path) {
        Args.notNull(path, "Request path");
        for (final Map.Entry<String, T> entry : this.map.entrySet()) {
            final String pattern = entry.getKey();
            if(path.equals(path))
                return entry.getValue();
            if(matchUriRequestPattern(pattern, path))
                return this.map.get(pattern);
        }
        return null;
    }

    protected boolean matchUriRequestPattern(final String pattern, final String path) {
        if (pattern.equals("*"))
            return true;
        return (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1)))
                || (pattern.startsWith("*") && path.endsWith(pattern.substring(1)));
    }

    @Override
    public String toString() {
        return this.map.toString();
    }
}
