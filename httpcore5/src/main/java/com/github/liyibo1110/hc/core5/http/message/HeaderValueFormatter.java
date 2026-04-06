package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * 用于格式化head value中各元素的接口。
 * 该接口的实例应为无状态且线程安全的。
 * @author liyibo
 * @date 2026-04-06 13:45
 */
public interface HeaderValueFormatter {

    /**
     * 格式化给定的一组HeaderElement
     */
    void formatElements(CharArrayBuffer buffer, HeaderElement[] elems, boolean quote);

    /**
     * 格式化给定的一个HeaderElement
     */
    void formatHeaderElement(CharArrayBuffer buffer, HeaderElement elem, boolean quote);

    /**
     * 格式化给定的一组NameValuePair
     */
    void formatParameters(CharArrayBuffer buffer, NameValuePair[] nvps, boolean quote);

    /**
     * 格式化给定的一个NameValuePair
     */
    void formatNameValuePair(CharArrayBuffer buffer, NameValuePair nvp, boolean quote);
}
