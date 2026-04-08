package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;

import java.io.IOException;

/**
 * 客户端HTTP连接，可用于发送请求和接收响应。
 * @author liyibo
 * @date 2026-04-07 13:59
 */
public interface HttpClientConnection extends BHttpConnection {

    /**
     * 检查此连接是否处于一致状态。
     */
    boolean isConsistent();

    /**
     * 通过连接发送请求行和所有头部信息。
     */
    void sendRequestHeader(ClassicHttpRequest request) throws HttpException, IOException;

    /**
     * 提前终止请求，可能会导致连接处于不一致状态。
     */
    void terminateRequest(ClassicHttpRequest request) throws HttpException, IOException;

    /**
     * 通过连接发送请求实体。
     */
    void sendRequestEntity(ClassicHttpRequest request) throws HttpException, IOException;

    /**
     * 接收来自此连接的下一个可用响应的请求行和头部。
     * 调用方应检查HttpResponse对象，以确定是否还应尝试接收响应实体。
     */
    ClassicHttpResponse receiveResponseHeader() throws HttpException, IOException;

    /**
     * 接收此连接中可用的下一个响应实体，并将其附加到现有的HttpResponse对象上。
     */
    void receiveResponseEntity(ClassicHttpResponse response) throws HttpException, IOException;
}
