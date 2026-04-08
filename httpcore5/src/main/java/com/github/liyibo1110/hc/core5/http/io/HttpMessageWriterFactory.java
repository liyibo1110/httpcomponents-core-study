package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.MessageHeaders;

/**
 * 创建HttpMessageWriter对象的工厂。
 * @author liyibo
 * @date 2026-04-07 15:14
 */
public interface HttpMessageWriterFactory<T extends MessageHeaders> {

    HttpMessageWriter<T> create();
}
