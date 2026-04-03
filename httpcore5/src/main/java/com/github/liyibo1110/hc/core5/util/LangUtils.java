package com.github.liyibo1110.hc.core5.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * 用来生成一致的equals和hashCode方法的工具类。
 * @author liyibo
 * @date 2026-04-02 13:38
 */
public final class LangUtils {
    public static final int HASH_SEED = 17;
    public static final int HASH_OFFSET = 37;

    private LangUtils() {}

    public static int hashCode(final int seed, final int hashcode) {
        return seed * HASH_OFFSET + hashcode;
    }

    public static int hashCode(final int seed, final boolean b) {
        return hashCode(seed, b ? 1 : 0);
    }

    public static int hashCode(final int seed, final Object obj) {
        return hashCode(seed, obj != null ? obj.hashCode() : 0);
    }

    @Deprecated
    public static boolean equals(final Object obj1, final Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    @Deprecated
    public static boolean equals(final Object[] a1, final Object[] a2) {
        return Arrays.equals(a1, a2);
    }
}
