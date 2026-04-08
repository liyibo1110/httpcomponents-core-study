package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.HttpConnection;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;

/**
 * 信息类(1xx)的HTTP响应回调
 * @author liyibo
 * @date 2026-04-07 15:16
 */
public interface HttpResponseInformationCallback {

    void execute(HttpResponse response, HttpConnection connection, HttpContext context) throws HttpException;
}
