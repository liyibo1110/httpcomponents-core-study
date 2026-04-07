package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * BasicLineParser的扩展，它会延迟解析header value。只有通过Header.getValue()访问时，才会解析。
 * 与BasicLineParser和LazyLineParser不同，此解析器不会拒绝那些在header字段名称和冒号之间包含空格的标头。
 * 该解析器应用于客户端服务器解析响应消息，或由中间代理（proxy）同时解析请求和响应消息。
 * @author liyibo
 * @date 2026-04-07 13:44
 */
public class LazyLaxLineParser extends BasicLineParser {
    public final static LazyLaxLineParser INSTANCE = new LazyLaxLineParser();

    @Override
    public Header parseHeader(final CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        return new BufferedHeader(buffer, false);
    }
}
