package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * HTTP协议拦截器是一种实现HTTP协议特定方面的例程。
 * 通常协议拦截器会对传入Message中的某个特定header或一组相关header进行处理，或者在传出Message中添加某个特定header或一组相关header。
 *
 * 协议拦截器还可以操作Message中包含的entity。通常，这是通过使用“装饰器”模式来实现的，即使用一个包装实体类来装饰原始实体。
 * 协议拦截器必须实现为线程安全的。与Servlet类似，协议拦截器不应使用实例变量，除非对这些变量的访问已进行同步。
 * @author liyibo
 * @date 2026-04-03 14:43
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpRequestInterceptor {

    /**
     * 处理请求。
     * 在客户端，此步骤在请求发送至服务器之前执行。
     * 在服务器端，此步骤在对传入消息的正文进行评估之前执行。
     */
    void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException;
}
