package com.github.liyibo1110.hc.core5.function;

/**
 * 函数式接口，类似Function，只是语义不同，强调input -> output。
 * @author liyibo
 * @date 2026-04-08 15:20
 */
@FunctionalInterface
public interface Resolver<I, O> {
    O resolve(I object);
}
