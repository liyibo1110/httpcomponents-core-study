package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.util.Args;

import java.util.Locale;

/**
 * HTTP method
 * @author liyibo
 * @date 2026-04-03 11:10
 */
public enum Method {

    GET(true, true),
    HEAD(true, true),
    POST(false, false),
    PUT(false, true),
    DELETE(false, true),
    CONNECT(false, false),
    TRACE(true, true),
    OPTIONS(true, true),
    PATCH(false, false);

    private final boolean safe;
    private final boolean idempotent;

    Method(final boolean safe, final boolean idempotent) {
        this.safe = safe;
        this.idempotent = idempotent;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public static boolean isSafe(final String value) {
        if (value == null)
            return false;
        try {
            return normalizedValueOf(value).safe;
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isIdempotent(final String value) {
        if (value == null)
            return false;
        try {
            return normalizedValueOf(value).idempotent;
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * 返回method名称的标准化值。
     */
    public static Method normalizedValueOf(final String method) {
        return valueOf(Args.notNull(method, "method").toLowerCase(Locale.ROOT));
    }

    public boolean isSame(final String value) {
        if(value == null)
            return false;
        return name().equals(value);
    }
}
