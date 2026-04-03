package com.github.liyibo1110.hc.core5.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被注解的类或字段或方法说明：实现了现已过时的HTTP协议或相关协议的要求。
 * @author liyibo
 * @date 2026-04-02 15:44
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Obsolete {

}
