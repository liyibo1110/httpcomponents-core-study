package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * 用于解析HTTP消息HEAD部分中各行的接口。
 * 该接口提供了分别用于解析请求行、状态行或标头行的独立方法。该接口的实例应为无状态且线程安全的。
 * @author liyibo
 * @date 2026-04-06 13:41
 */
public interface LineParser {

    /**
     * CharArrayBuffer -> RequestLine
     */
    RequestLine parseRequestLine(CharArrayBuffer buffer) throws ParseException;

    /**
     * CharArrayBuffer -> StatusLine
     */
    StatusLine parseStatusLine(CharArrayBuffer buffer) throws ParseException;

    /**
     * CharArrayBuffer -> Header
     */
    Header parseHeader(CharArrayBuffer buffer) throws ParseException;
}
