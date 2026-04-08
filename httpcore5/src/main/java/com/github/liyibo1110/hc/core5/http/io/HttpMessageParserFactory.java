package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.MessageHeaders;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;

/**
 * 创建HttpMessageParser对象的工厂。
 * @author liyibo
 * @date 2026-04-07 14:56
 */
public interface HttpMessageParserFactory<T extends MessageHeaders> {

    HttpMessageParser<T> create(Http1Config http1Config);
}
