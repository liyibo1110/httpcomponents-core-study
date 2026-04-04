package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 参照HttpRequestInterceptor
 * @author liyibo
 * @date 2026-04-03 14:54
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpResponseInterceptor {

    /**
     * 处理响应。
     * 在服务器端，此步骤在响应发送给客户端之前执行。
     * 在客户端，此步骤在对消息正文进行评估之前对传入的消息进行处理。
     */
    void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException;
}
