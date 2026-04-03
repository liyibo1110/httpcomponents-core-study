package com.github.liyibo1110.hc.core5.http;

/**
 * 表示HTTP header中的一个元素，该元素由一个名称/值对以及若干可选的名称/值参数组成。
 * 注意这个表示的是，header某个value里面的可再拆的element。
 *
 * 例如：Content-Type: text/html; charset=UTF-8
 * header name = Content-Type
 * header value = text/html; charset=UTF-8
 *
 * header elements：
 * name = text/html
 * value = null
 * parameters：
 *   charset = UTF-8
 *
 * 又例如：Accept: text/html; q=0.9, application/json; q=0.8
 * element1：
 *  name = text/html
 *  value = null
 *  parameters：
 *      q=0.9
 * element2：
 *  name = application/json
 *  value = null
 *  parameters:
 *      q=0.8
 *
 * @author liyibo
 * @date 2026-04-03 11:44
 */
public interface HeaderElement {

    String getName();

    String getValue();

    NameValuePair[] getParameters();

    NameValuePair getParameterByName(String name);

    int getParameterCount();

    NameValuePair getParameter(int index);
}
