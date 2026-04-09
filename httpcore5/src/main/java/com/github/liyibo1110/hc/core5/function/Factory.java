package com.github.liyibo1110.hc.core5.function;

/**
 * 函数式接口，类似Function，即有单个入参以及返回值。
 * @author liyibo
 * @date 2026-04-08 15:15
 */
@FunctionalInterface
public interface Factory<P, T> {
    T create(P parameter);
}
