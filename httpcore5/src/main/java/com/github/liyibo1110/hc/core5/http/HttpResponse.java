package com.github.liyibo1110.hc.core5.http;

import java.util.Locale;

/**
 * 服务端接收并解析请求信息后，返回的HTTP响应消息。
 * @author liyibo
 * @date 2026-04-02 17:34
 */
public interface HttpResponse extends HttpMessage {

    int getCode();

    void setCode(int code);

    String getReasonPhrase();

    void setReasonPhrase(String reason);

    Locale getLocale();

    void setLocale(Locale loc);
}
