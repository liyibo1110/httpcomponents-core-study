package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.http.NameValuePair;

/**
 * 用于将header的value解析为elements的接口。
 * 该接口的实例应为无状态且线程安全。
 * @author liyibo
 * @date 2026-04-05 15:31
 */
public interface HeaderValueParser {

    /**
     * value -> HeaderElement[]。
     * @param buffer 用于存储待解析的value的buffer。
     * @param cursor 包含当前位置以及buffer中用于解析操作的范围的ParserCursor实例。
     */
    HeaderElement[] parseElements(CharSequence buffer, ParserCursor cursor);

    /**
     * 解析单个的HeaderElement，由以分号分隔的name=value定义列表组成。
     */
    HeaderElement parseHeaderElement(CharSequence buffer, ParserCursor cursor);

    /**
     * 解析一组NameValuePair。
     */
    NameValuePair[] parseParameters(CharSequence buffer, ParserCursor cursor);

    /**
     * 解析一个NameValuePair。
     */
    NameValuePair parseNameValuePair(CharSequence buffer, ParserCursor cursor);
}
