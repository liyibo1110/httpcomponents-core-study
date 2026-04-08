package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 维护一个以请求URI正则表达式为键的对象映射。
 * 该映射中保留了插入顺序，因此查找时会依次测试每个正则表达式，直到找到匹配项为止。该类可用于解析与特定请求URI匹配的对象。
 * @author liyibo
 * @date 2026-04-07 17:49
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class UriRegexMatcher<T> implements LookupRegistry<T> {

    /** key是正则表达式，value是数据元素 */
    private final Map<String, T> objectMap;

    /** key是正则表达式，value是java.util.regex.Pattern */
    private final Map<String, Pattern> patternMap;

    public UriRegexMatcher() {
        super();
        this.objectMap = new LinkedHashMap<>();
        this.patternMap = new LinkedHashMap<>();
    }

    @Override
    public synchronized void register(final String regex, final T obj) {
        Args.notNull(regex, "URI request regex");
        this.objectMap.put(regex, obj);
        this.patternMap.put(regex, Pattern.compile(regex));
    }

    @Override
    public synchronized void unregister(final String regex) {
        if (regex == null)
            return;
        this.objectMap.remove(regex);
        this.patternMap.remove(regex);
    }

    @Override
    public synchronized T lookup(final String path) {
        Args.notNull(path, "Request path");
        final T obj = this.objectMap.get(path); // 先尝试直接匹配
        if(obj == null) {
            // 直接匹配不到，再尝试遍历正则匹配
            for (final Map.Entry<String, Pattern> entry : this.patternMap.entrySet()) {
                if(entry.getValue().matcher(path).matches())
                    return objectMap.get(entry.getKey());
            }
        }
        return obj;
    }

    @Override
    public String toString() {
        return this.objectMap.toString();
    }
}
