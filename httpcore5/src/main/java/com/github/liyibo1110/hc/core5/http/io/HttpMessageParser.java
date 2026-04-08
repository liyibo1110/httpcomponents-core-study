package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.MessageHeaders;

import java.io.IOException;
import java.io.InputStream;

/**
 * 消息解析器，用于从输入流中构建HTTP消息头。
 * @author liyibo
 * @date 2026-04-07 14:48
 */
public interface HttpMessageParser<T extends MessageHeaders> {

    /**
     * 根据给定的输入流生成一个MessageHeaders实例
     */
    T parse(SessionInputBuffer buffer, InputStream inputStream) throws IOException, HttpException;
}
