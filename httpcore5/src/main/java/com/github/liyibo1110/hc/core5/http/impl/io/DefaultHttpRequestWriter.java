package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.LineFormatter;
import com.github.liyibo1110.hc.core5.http.message.RequestLine;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;

/**
 * 一种将输出序列化为SessionOutputBuffer实例的HTTP请求写入器
 * @author liyibo
 * @date 2026-04-10 11:23
 */
public class DefaultHttpRequestWriter extends AbstractMessageWriter<ClassicHttpRequest> {

    public DefaultHttpRequestWriter(final LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpRequestWriter() {
        this(null);
    }

    @Override
    protected void writeHeadLine(final ClassicHttpRequest message, final CharArrayBuffer lineBuf) throws IOException {
        final ProtocolVersion transportVersion = message.getVersion();
        getLineFormatter().formatRequestLine(lineBuf, new RequestLine(
                message.getMethod(),
                message.getRequestUri(),
                transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1));
    }
}
