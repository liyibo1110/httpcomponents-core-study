package com.github.liyibo1110.hc.core5.function;

/**
 * 函数式接口，等同于JDK的Supplier
 * @author liyibo
 * @date 2026-04-08 15:16
 */
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
