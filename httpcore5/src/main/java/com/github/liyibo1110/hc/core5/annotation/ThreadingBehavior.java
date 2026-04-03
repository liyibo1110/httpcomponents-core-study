package com.github.liyibo1110.hc.core5.annotation;

/**
 * 定义了运行时强制执行的线程行为类型。
 * @author liyibo
 * @date 2026-04-02 15:37
 */
public enum ThreadingBehavior {

    /**
     * 所属类具有不可变性，从而线程安全。
     */
    IMMUTABLE,

    /**
     * 如果类在构造时注入的依赖项是不可变的，则类实例则也是不可变的。
     * 如果依赖项是线程安全的，则类实例则为线程安全的。
     */
    IMMUTABLE_CONDITIONAL,

    /**
     * 所属类里没有任何状态字段，因此也是线程安全的。
     */
    STATELESS,

    /**
     * 所属类具备线程安全特性。
     */
    SAFE,

    /**
     * 如果类在构造时注入的依赖项是线程安全的，那么类实例则为线程安全的。
     */
    SAFE_CONDITIONAL,

    /**
     * 所属类不具备线程安全特性。
     */
    UNSAFE
}
