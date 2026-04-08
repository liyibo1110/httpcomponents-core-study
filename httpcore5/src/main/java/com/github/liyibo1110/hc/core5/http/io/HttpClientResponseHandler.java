package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;

import java.io.IOException;

/**
 * 封装了从ClassicHttpResponse生成响应对象过程的处理程序。
 * @author liyibo
 * @date 2026-04-07 14:10
 */
@FunctionalInterface
public interface HttpClientResponseHandler<T> {

    /**
     * 处理ClassicHttpResponse对象，并返回与该响应对应的一些值。
     */
    T handleResponse(ClassicHttpResponse response) throws HttpException, IOException;
}
