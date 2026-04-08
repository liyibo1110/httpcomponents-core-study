package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.MessageHeaders;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 消息写入器用来将HTTP header序列化到输出流中
 * @author liyibo
 * @date 2026-04-07 15:12
 */
public interface HttpMessageWriter<T extends MessageHeaders> {

    /**
     * 将MessageHeaders实例序列化到指定的输出流中
     */
    void write(T message, SessionOutputBuffer buffer, OutputStream outputStream) throws IOException, HttpException;
}
