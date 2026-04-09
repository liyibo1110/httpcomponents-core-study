package com.github.liyibo1110.hc.core5.function;

/**
 * 函数式接口，类似Consumer，即只有单个入参，没有返回值。
 * @author liyibo
 * @date 2026-04-08 15:14
 */
@FunctionalInterface
public interface Callback<T> {
    void execute(T object);
}
