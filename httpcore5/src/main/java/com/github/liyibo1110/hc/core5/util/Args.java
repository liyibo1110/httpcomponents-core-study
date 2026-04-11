package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.http.EntityDetails;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 处理和验证参数相关功能的工具类。
 * @author liyibo
 * @date 2026-04-02 13:48
 */
public final class Args {

    /**
     * 和Asserts的方法一样，只是异常变成了IllegalArgumentException
     */
    public static void check(final boolean expression, final String message) {
        if (!expression)
            throw new IllegalArgumentException(message);
    }

    public static void check(final boolean expression, final String message, final Object... args) {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, args));
    }

    public static void check(final boolean expression, final String message, final Object arg) {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, arg));
    }

    @Deprecated
    public static long checkContentLength(final EntityDetails entityDetails) {
        // -1 is a special value,
        // 0 is allowed as well,
        // but never more than Integer.MAX_VALUE.
        return checkRange(entityDetails.getContentLength(), -1, Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory)");
    }

    /**
     * 检查value是否在给定范围中（闭区间）
     */
    public static int checkRange(final int value, final int lowInclusive, final int highInclusive, final String message) {
        if (value < lowInclusive || value > highInclusive)
            throw illegalArgumentException("%s: %d is out of range [%d, %d]", message, value, lowInclusive, highInclusive);
        return value;
    }

    public static long checkRange(final long value, final long lowInclusive, final long highInclusive, final String message) {
        if (value < lowInclusive || value > highInclusive)
            throw illegalArgumentException("%s: %d is out of range [%d, %d]", message, value, lowInclusive, highInclusive);
        return value;
    }

    /**
     * 验证argument不能包含空格
     */
    public static <T extends CharSequence> T containsNoBlanks(final T argument, final String name) {
        notNull(argument, name);
        if (isEmpty(argument))
            throw illegalArgumentExceptionNotEmpty(name);
        if (TextUtils.containsBlanks(argument))
            throw new IllegalArgumentException(name + " must not contain blanks");
        return argument;
    }

    /**
     * 生成IllegalArgumentException对象
     */
    private static IllegalArgumentException illegalArgumentException(final String format, final Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    private static IllegalArgumentException illegalArgumentExceptionNotEmpty(final String name) {
        return new IllegalArgumentException(name + " must not be empty");
    }

    /**
     * 生成NullPointerException对象
     */
    private static NullPointerException NullPointerException(final String name) {
        return new NullPointerException(name + " must not be null");
    }

    public static <T extends CharSequence> T notBlank(final T argument, final String name) {
        notNull(argument, name);
        if (TextUtils.isBlank(argument))
            throw new IllegalArgumentException(name + " must not be blank");
        return argument;
    }

    public static <T extends CharSequence> T notEmpty(final T argument, final String name) {
        notNull(argument, name);
        if (isEmpty(argument))
            throw illegalArgumentExceptionNotEmpty(name);
        return argument;
    }

    public static <E, T extends Collection<E>> T notEmpty(final T argument, final String name) {
        notNull(argument, name);
        if (isEmpty(argument))
            throw illegalArgumentExceptionNotEmpty(name);
        return argument;
    }

    public static <T> T notEmpty(final T argument, final String name) {
        notNull(argument, name);
        if (isEmpty(argument))
            throw illegalArgumentExceptionNotEmpty(name);
        return argument;
    }

    public static int notNegative(final int n, final String name) {
        if (n < 0)
            throw illegalArgumentException("%s must not be negative: %d", name, n);
        return n;
    }

    public static long notNegative(final long n, final String name) {
        if (n < 0)
            throw illegalArgumentException("%s must not be negative: %d", name, n);
        return n;
    }

    public static <T> T notNull(final T argument, final String name) {
        return Objects.requireNonNull(argument, name);
    }

    /**
     * 判断各种类型对象的empty
     */
    public static boolean isEmpty(final Object object) {
        if (object == null)
            return true;
        if (object instanceof CharSequence)
            return ((CharSequence) object).length() == 0;
        if (object.getClass().isArray())
            return Array.getLength(object) == 0;
        if (object instanceof Collection<?>)
            return ((Collection<?>) object).isEmpty();
        if (object instanceof Map<?, ?>)
            return ((Map<?, ?>) object).isEmpty();
        return false;
    }

    public static int positive(final int n, final String name) {
        if (n <= 0)
            throw illegalArgumentException("%s must not be negative or zero: %d", name, n);
        return n;
    }

    public static long positive(final long n, final String name) {
        if (n <= 0)
            throw illegalArgumentException("%s must not be negative or zero: %d", name, n);
        return n;
    }

    public static <T extends TimeValue> T positive(final T timeValue, final String name) {
        positive(timeValue.getDuration(), name);
        return timeValue;
    }

    private Args() {}
}
