package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 维护一个以请求URI模式为键的对象映射。
 * 模式可以有三种格式：
 * 1、*
 * 2、*<uri>
 * 3、<uri>*
 *
 * 注意这个实现是RequestHandlerRegistry里面用的默认组件。
 *
 * @author liyibo
 * @date 2026-04-07 17:37
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class UriPatternMatcher<T> implements LookupRegistry<T> {

    private final Map<String, T> map;

    public UriPatternMatcher() {
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
        T obj = this.map.get(path); // 先尝试直接相等匹配
        if (obj == null) {
            // 直接相等匹配失败，再尝试pattern匹配
            String bestMatch = null;
            for (final String pattern : this.map.keySet()) {
                if (matchUriRequestPattern(pattern, path)) {
                    // 匹配中了，还要尝试其它的pattern，最终选出最合适的结果（即优先最长匹配到的内容）
                    if (bestMatch == null
                            || (bestMatch.length() < pattern.length())
                            || (bestMatch.length() == pattern.length() && pattern.endsWith("*"))) {
                        obj = this.map.get(pattern);
                        bestMatch = pattern;
                    }
                }
            }
        }
        return obj;
    }

    /**
     * 给定的path是否匹配给定的pattern。
     */
    protected boolean matchUriRequestPattern(final String pattern, final String path) {
        if (pattern.equals("*"))    // pattern是*，则直接通过
            return true;
        return (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1)))
                || (pattern.startsWith("*") && path.endsWith(pattern.substring(1)));
    }

    @Override
    public String toString() {
        return this.map.toString();
    }
}
