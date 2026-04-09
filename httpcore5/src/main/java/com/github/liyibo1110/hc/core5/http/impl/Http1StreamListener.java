package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HttpConnection;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpResponse;

/**
 * HTTP/1.1协议的stream event监听器组件
 * @author liyibo
 * @date 2026-04-09 10:21
 */
@Contract(threading = ThreadingBehavior.STATELESS)
@Internal
public interface Http1StreamListener {

    void onRequestHead(HttpConnection connection, HttpRequest request);

    void onResponseHead(HttpConnection connection, HttpResponse response);

    void onExchangeComplete(HttpConnection connection, boolean keepAlive);
}
