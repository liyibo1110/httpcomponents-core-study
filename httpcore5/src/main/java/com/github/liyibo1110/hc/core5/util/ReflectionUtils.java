package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.annotation.Internal;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author liyibo
 * @date 2026-04-02 13:41
 */
@Internal
public final class ReflectionUtils {

    /**
     * 调用某对象的setter方法
     */
    public static void callSetter(final Object object, final String setterName, final Class<?> type, final Object value) {
        try {
            final Class<?> clazz = object.getClass();
            final Method method = clazz.getMethod("set" + setterName, type);
            setAccessible(method);
            method.invoke(object, value);
        } catch (final Exception ignore) {

        }
    }

    /**
     * 调用某对象的getter方法
     */
    public static <T> T callGetter(final Object object, final String getterName, final Class<T> resultType) {
        try {
            final Class<?> clazz = object.getClass();
            final Method method = clazz.getMethod("get" + getterName);
            setAccessible(method);
            return resultType.cast(method.invoke(object));
        } catch (final Exception ignore) {
            return null;
        }
    }

    private static void setAccessible(final Method method) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            method.setAccessible(true);
            return null;
        });
    }

    /**
     * 获取java主版本号
     */
    public static int determineJRELevel() {
        final String s = System.getProperty("java.version");
        final String[] parts = s.split("\\.");
        if (parts.length > 0) {
            try {
                final int majorVersion = Integer.parseInt(parts[0]);
                if (majorVersion > 1)
                    return majorVersion;
                else if (majorVersion == 1 && parts.length > 1)
                    return Integer.parseInt(parts[1]);
            } catch (final NumberFormatException ignore) {

            }
        }
        return 7;
    }
}
