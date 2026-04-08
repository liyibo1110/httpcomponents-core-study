package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpException;

import java.io.IOException;

/**
 * 服务器端的HTTP连接，可用于接收请求和发送响应。
 * @author liyibo
 * @date 2026-04-07 14:04
 */
public interface HttpServerConnection extends BHttpConnection {

    ClassicHttpRequest receiveRequestHeader() throws HttpException, IOException;

    void receiveRequestEntity(ClassicHttpRequest request) throws HttpException, IOException;

    void sendResponseHeader(ClassicHttpResponse response) throws HttpException, IOException;

    void sendResponseEntity(ClassicHttpResponse response) throws HttpException, IOException;
}
