package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpResponseInterceptor;

/**
 * HTTP协议处理器是一组实现“责任链”模式的协议拦截器集合，其中每个单独的协议拦截器负责处理HTTP协议中的特定方面。
 *
 * 通常情况下，只要拦截器不依赖于执行上下文的特定状态，其执行顺序就不应产生影响。
 * 如果协议拦截器之间存在相互依赖关系，因此必须按特定顺序执行，则应按照其预期执行顺序将其添加到协议处理器中。
 *
 * 协议拦截器必须实现为线程安全的。与Servlet类似，协议拦截器不应使用实例变量，除非对这些变量的访问已进行同步。
 * @author liyibo
 * @date 2026-04-07 15:21
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpProcessor extends HttpRequestInterceptor, HttpResponseInterceptor {
    // 只是整合了HttpRequestInterceptor和HttpResponseInterceptor接口
}
