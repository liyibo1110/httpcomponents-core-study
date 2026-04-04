package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

/**
 * 根据HTTP消息的属性来确定所包含内容实体长度的策略。
 * @author liyibo
 * @date 2026-04-03 16:37
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface ContentLengthStrategy {

    /** 消息正文分块 */
    long CHUNKED = -1;

    /** 消息正文未明确划分。对于HTTP响应消息，此情况是合法的；对于HTTP请求消息，此情况是非法的。 */
    long UNDEFINED = -Long.MAX_VALUE;

    /**
     * 返回给定消息的长度（以字节为单位），返回值必须为非负数。
     * 如果消息采用分块编码，则返回CHUNKED。
     * 如果消息未明确划分，则返回UNDEFINED。
     */
    long determineLength(HttpMessage message) throws HttpException;
}
