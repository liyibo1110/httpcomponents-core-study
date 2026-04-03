package com.github.liyibo1110.hc.core5.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义了被注解类的在运行时的行为，目前只有一个是否为线程安全的标记。
 * @author liyibo
 * @date 2026-04-02 15:41
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Contract {
    ThreadingBehavior threading() default ThreadingBehavior.UNSAFE;
}
