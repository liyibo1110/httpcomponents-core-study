package com.github.liyibo1110.hc.core5.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被标记的类或方法说明目前还属于实验性产物。
 * @author liyibo
 * @date 2026-04-02 15:43
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Experimental {
}
