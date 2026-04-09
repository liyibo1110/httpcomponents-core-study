package com.github.liyibo1110.hc.core5.http.config;

import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry对象的对应builder
 * @author liyibo
 * @date 2026-04-08 14:44
 */
public final class RegistryBuilder<I> {

    private final Map<String, I> items;

    public static <I> RegistryBuilder<I> create() {
        return new RegistryBuilder<>();
    }

    RegistryBuilder() {
        super();
        this.items = new HashMap<>();
    }

    public RegistryBuilder<I> register(final String id, final I item) {
        Args.notEmpty(id, "ID");
        Args.notNull(item, "Item");
        items.put(TextUtils.toLowerCase(id), item);
        return this;
    }

    public Registry<I> build() {
        return new Registry<>(items);
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
