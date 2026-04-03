package com.github.liyibo1110.hc.core5.util;

/**
 * @author liyibo
 * @date 2026-04-02 13:24
 */
public final class Asserts {

    private Asserts() {}

    /**
     * expression为false，则抛IllegalStateException
     */
    public static void check(final boolean expression, final String message) {
        if (!expression)
            throw new IllegalStateException(message);
    }

    public static void check(final boolean expression, final String message, final Object... args) {
        if (!expression)
            throw new IllegalStateException(String.format(message, args));
    }

    public static void check(final boolean expression, final String message, final Object arg) {
        if (!expression)
            throw new IllegalStateException(String.format(message, arg));
    }

    /**
     * object为null，则抛IllegalStateException
     */
    public static void notNull(final Object object, final String name) {
        if (object == null)
            throw new IllegalStateException(name + " is null");
    }

    /**
     * 字符串为empty，则抛IllegalStateException
     */
    public static void notEmpty(final CharSequence s, final String name) {
        if (TextUtils.isEmpty(s))
            throw new IllegalStateException(name + " is empty");
    }

    /**
     * 字符串为blank，则抛IllegalStateException
     */
    public static void notBlank(final CharSequence s, final String name) {
        if (TextUtils.isBlank(s))
            throw new IllegalStateException(name + " is blank");
    }
}
