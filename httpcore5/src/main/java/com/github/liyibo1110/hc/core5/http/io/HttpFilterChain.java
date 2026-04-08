package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

/**
 * 代表服务器端请求处理链中的单个元素（即只处理单个请求中的字段）。
 * @author liyibo
 * @date 2026-04-07 14:16
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface HttpFilterChain {

    interface ResponseTrigger {
        void sendInformation(ClassicHttpResponse response) throws HttpException, IOException;

        void submitResponse(ClassicHttpResponse response) throws HttpException, IOException;
    }

    /**
     * 进入请求处理链中的下一个环节。
     */
    void proceed(ClassicHttpRequest request, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException;
}
