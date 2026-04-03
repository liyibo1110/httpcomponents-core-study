package com.github.liyibo1110.hc.core5.http;

import java.util.Set;

/**
 * Message中传输的实体的详细信息。
 * @author liyibo
 * @date 2026-04-03 10:10
 */
public interface EntityDetails {
    /**
     * 返回entity的长度。
     */
    long getContentLength();

    /**
     * 返回entity的content type名称。
     */
    String getContentType();

    /**
     * 返回entity的content encoding名称。
     */
    String getContentEncoding();

    /**
     * 返回该实体的分块传输提示。
     * 对实体进行封装的行为取决于具体实现，但应符合其主要用途。
     */
    boolean isChunked();

    /**
     * 尾随标头的初步声明。
     */
    Set<String> getTrailerNames();
}
