package com.github.liyibo1110.hc.core5.http.config;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup接口的通用实现
 * @author liyibo
 * @date 2026-04-08 14:42
 */
@Contract(threading = ThreadingBehavior.SAFE)
public final class Registry<I> implements Lookup<I> {

    /** 底层存储容器 */
    private final Map<String, I> map;

    Registry(final Map<String, I> map) {
        super();
        this.map = new ConcurrentHashMap<>(map);
    }

    @Override
    public I lookup(final String key) {
        if (key == null)
            return null;
        return map.get(TextUtils.toLowerCase(key));
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
