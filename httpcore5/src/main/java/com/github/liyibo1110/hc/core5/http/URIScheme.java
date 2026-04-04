package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.util.Args;

/**
 * URI schemes的枚举（即协议名：http和https）
 * @author liyibo
 * @date 2026-04-03 14:37
 */
public enum URIScheme {
    HTTP("http"),
    HTTPS("https");

    public final String id;

    URIScheme(final String id) {
        this.id = Args.notBlank(id, "id");
    }

    public String getId() {
        return id;
    }

    public boolean same(final String scheme) {
        return id.equalsIgnoreCase(scheme);
    }

    @Override
    public String toString() {
        return id;
    }
}
