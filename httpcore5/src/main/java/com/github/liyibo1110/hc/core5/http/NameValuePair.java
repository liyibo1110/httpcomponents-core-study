package com.github.liyibo1110.hc.core5.http;

/**
 * 用在HttpMessage里面的name-value值
 * @author liyibo
 * @date 2026-04-02 17:00
 */
public interface NameValuePair {

    /**
     * name不能为null。
     */
    String getName();

    /**
     * value可以为null。
     */
    String getValue();
}
