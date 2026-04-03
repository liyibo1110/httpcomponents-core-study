package com.github.liyibo1110.hc.core5.http;

/**
 * HTTP的Header字段
 * @author liyibo
 * @date 2026-04-02 17:09
 */
public interface Header extends NameValuePair {

    /**
     * 是否为敏感header，某些编码方案（入HPACK）会对敏感header的编码表示形式增加限制。
     */
    boolean isSensitive();
}
