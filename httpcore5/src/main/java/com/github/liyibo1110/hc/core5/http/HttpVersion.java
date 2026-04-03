package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

/**
 * 表示HTTP版本，使用“主版本.次版本”的编号方案来标识协议版本。
 * HTTP消息的版本由消息第一行中的HTTP-Version字段指定。
 * @author liyibo
 * @date 2026-04-02 17:39
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class HttpVersion extends ProtocolVersion {
    private static final long serialVersionUID = -5856653513894415344L;

    /** protocol name */
    public static final String HTTP = "HTTP";

    public static final HttpVersion HTTP_0_9 = new HttpVersion(0, 9);
    public static final HttpVersion HTTP_1_0 = new HttpVersion(1, 0);
    public static final HttpVersion HTTP_1_1 = new HttpVersion(1, 1);
    public static final HttpVersion HTTP_2_0 = new HttpVersion(2, 0);
    public static final HttpVersion HTTP_2 = HTTP_2_0;
    public static final HttpVersion DEFAULT = HTTP_1_1;

    public static final HttpVersion[] ALL = {HTTP_0_9, HTTP_1_0, HTTP_1_1, HTTP_2_0};

    public HttpVersion(final int major, final int minor) {
        super(HTTP, major, minor);
    }

    /**
     * 根据版本值，获取特定的HttpVersion。
     */
    public static HttpVersion get(final int major, final int minor) {
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].equals(major, minor))
                return ALL[i];
        }
        // 没找到就自己构造
        return new HttpVersion(major, minor);
    }
}
