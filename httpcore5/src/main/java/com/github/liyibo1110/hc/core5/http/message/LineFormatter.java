package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * 用于格式化HTTP消息HEAD部分元素的接口。
 * 该接口提供了分别用于格式化请求行、状态行或标头行的独立方法。格式化方法应返回一行格式化后的内容，且该内容不应包含行分隔符（如CR-LF）。
 * 该接口的实例应为无状态且线程安全的。
 * @author liyibo
 * @date 2026-04-06 13:43
 */
public interface LineFormatter {

    /**
     * 格式化给定的RequestLine
     */
    void formatRequestLine(CharArrayBuffer buffer, RequestLine reqline);


    /**
     * 格式化给定的StatusLine
     */
    void formatStatusLine(CharArrayBuffer buffer, StatusLine statline);


    /**
     * 格式化给定的Header
     */
    void formatHeader(CharArrayBuffer buffer, Header header);
}
