package com.github.liyibo1110.hc.core5.http.config;

/**
 * 通过小写字符串查找特定元素
 * @author liyibo
 * @date 2026-04-08 14:20
 */
public interface Lookup<I> {

    I lookup(String name);
}
