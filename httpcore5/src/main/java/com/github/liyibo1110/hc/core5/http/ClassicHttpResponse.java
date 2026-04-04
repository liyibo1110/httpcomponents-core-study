package com.github.liyibo1110.hc.core5.http;

import java.io.Closeable;

/**
 * 额外包含了HttpEntityContainer接口。
 * @author liyibo
 * @date 2026-04-03 16:35
 */
public interface ClassicHttpResponse extends HttpResponse, HttpEntityContainer, Closeable {
    // 空的
}
