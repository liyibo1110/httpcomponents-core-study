package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.net.URIAuthority;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 请求消息的第一行，包含了：
 * 1、请求方法。
 * 2、资源URI。
 * 3、请求协议版本。
 * @author liyibo
 * @date 2026-04-02 17:31
 */
public interface HttpRequest extends HttpMessage {

    String getMethod();

    String getPath();

    void setPath(String path);

    String getScheme();

    void setScheme(String scheme);

    URIAuthority getAuthority();

    void setAuthority(URIAuthority authority);

    String getRequestUri();

    URI getUri() throws URISyntaxException;

    void setUri(final URI requestUri);
}
