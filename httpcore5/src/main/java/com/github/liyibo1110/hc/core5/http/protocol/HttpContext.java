package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.http.ProtocolVersion;

/**
 * HttpContext表示HTTP进程的执行状态。它是一种结构，可用于将属性名称映射到属性值。
 *
 * HttpContext的主要目的是促进各种逻辑相关组件之间的信息共享。HttpContext可用于存储一条消息或几条连续消息的处理状态。
 * 如果在连续的消息之间复用相同的上下文，则多个逻辑相关的消息可以参与一个逻辑会话。
 *
 * 重要提示：请注意即使HttpContext的实现是线程安全的，也不应由多个线程并发使用，因为该上下文可能包含非线程安全的属性。
 * @author liyibo
 * @date 2026-04-03 14:45
 */
public interface HttpContext {
    String RESERVED_PREFIX  = "http.";

    ProtocolVersion getProtocolVersion();

    void setProtocolVersion(ProtocolVersion version);

    Object getAttribute(String id);

    Object setAttribute(String id, Object obj);

    Object removeAttribute(String id);
}
