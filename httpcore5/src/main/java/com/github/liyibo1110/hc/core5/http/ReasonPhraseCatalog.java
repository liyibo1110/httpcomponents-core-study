package com.github.liyibo1110.hc.core5.http;

import java.util.Locale;

/**
 * 用于获取HTTP状态码reason说明的接口。
 * @author liyibo
 * @date 2026-04-03 15:08
 */
public interface ReasonPhraseCatalog {

    /**
     * 获取状态码的说明短语。
     * 可选的上下文参数支持能够检测reason短语语言的目录。
     */
    String getReason(int status, Locale loc);
}
