package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * 已格式化的header，例如在接收header时，可以保留其原始格式，这样发送header时就无需再进行格式化处理
 * @author liyibo
 * @date 2026-04-03 15:12
 */
public interface FormattedHeader extends Header {

    /**
     * 获取FormattedHeader的buffer，返回的buffer禁止修改。
     */
    CharArrayBuffer getBuffer();

    /**
     * 获取header value的首部。通过直接访问缓冲区中的值，可以避免创建临时字符串。
     */
    int getValuePos();
}
