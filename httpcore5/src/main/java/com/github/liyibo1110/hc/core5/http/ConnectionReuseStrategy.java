package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

/**
 * 用于决定连接是否可用于后续请求以及是否应保持连接存活的接口。
 * 该接口的实现必须是线程安全的。由于该接口的方法可能由多个线程执行，因此必须对共享数据的访问进行同步。
 * @author liyibo
 * @date 2026-04-03 16:09
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface ConnectionReuseStrategy {

    /**
     * 决定请求完成后是否可以保持连接打开。如果此方法返回false，调用方必须关闭连接以正确遵守HTTP协议。
     * 如果返回true，调用方应尝试保持连接打开，以便在其他请求中重复使用。
     *
     * 可以使用HttpContext来检索与保持连接策略相关的其他对象：实际的HTTP连接、原始HTTP请求、已知的目标主机、连接已被重用的次数等。
     *
     * 如果连接已关闭，则返回false。连接重用策略不得触发过期连接检查。
     */
    boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context);
}
