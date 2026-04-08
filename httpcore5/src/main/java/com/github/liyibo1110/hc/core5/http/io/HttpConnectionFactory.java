package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.HttpConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * HttpConnection对象的工厂接口。
 * @author liyibo
 * @date 2026-04-07 14:15
 */
public interface HttpConnectionFactory<T extends HttpConnection> {

    T createConnection(Socket socket) throws IOException;
}
