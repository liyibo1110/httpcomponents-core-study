package com.github.liyibo1110.hc.core5.http;

/**
 * 额外包含了HttpEntityContainer接口。
 * @author liyibo
 * @date 2026-04-03 16:34
 */
public interface ClassicHttpRequest extends HttpRequest, HttpEntityContainer {
    //  只是整合了HttpRequest和HttpEntityContainer接口
}
