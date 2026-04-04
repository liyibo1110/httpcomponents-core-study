package com.github.liyibo1110.hc.core5.http;

/**
 * HttpEntity的容器。
 * @author liyibo
 * @date 2026-04-03 15:11
 */
public interface HttpEntityContainer {

    HttpEntity getEntity();

    void setEntity(HttpEntity entity);
}
