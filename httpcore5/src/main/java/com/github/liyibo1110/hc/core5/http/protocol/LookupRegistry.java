package com.github.liyibo1110.hc.core5.http.protocol;

/**
 * 用来查找元素的注册表。
 * @author liyibo
 * @date 2026-04-07 17:32
 */
public interface LookupRegistry<T> {

    /**
     * 将给定的对象注册为与给定pattern匹配的URI
     */
    void register(String pattern, T obj);

    /**
     * 查找与给定请求路径匹配的对象
     */
    T lookup(String value);

    void unregister(String pattern);
}
