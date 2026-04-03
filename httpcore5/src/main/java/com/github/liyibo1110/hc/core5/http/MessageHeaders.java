package com.github.liyibo1110.hc.core5.http;

import java.util.Iterator;

/**
 * 由多个Header组成的Header
 * @author liyibo
 * @date 2026-04-02 17:10
 */
public interface MessageHeaders {

    /**
     * 检查Message中是否包含某个header。
     */
    boolean containsHeader(String name);

    /**
     * 检查Message中是否包含某个header，以及包含多少次。
     */
    int countHeaders(String name);

    /**
     * 返回Message中名称与指定名称匹配的第一个header。
     * 如果Message中存在多个匹配的header，则返回getHeaders(String)的第一个元素。
     * 如果消息中不存在匹配的标头，则返回null。
     */
    Header getFirstHeader(String name);

    /**
     * 返回Message中名称与指定名称匹配的第一个header。
     * name不区分大小写。
     */
    Header getHeader(String name) throws ProtocolException;

    /**
     * 返回Message的所有header，顺序就是在连接中发送时的排列顺序。
     */
    Header[] getHeaders();

    /**
     * 返回Message中所有名称匹配的header，顺序就是在连接中发送时的排列顺序。
     */
    Header[] getHeaders(String name);

    Header getLastHeader(String name);

    Iterator<Header> headerIterator();

    Iterator<Header> headerIterator(String name);
}
