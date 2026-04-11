package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.message.LineFormatter;
import com.github.liyibo1110.hc.core5.http.message.StatusLine;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;

/**
 * 一种HTTP响应写入器，它将输出序列化为SessionOutputBuffer的实例。
 * @author liyibo
 * @date 2026-04-10 11:35
 */
public class DefaultHttpResponseWriter extends AbstractMessageWriter<ClassicHttpResponse> {

    public DefaultHttpResponseWriter(final LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpResponseWriter() {
        super(null);
    }

    @Override
    protected void writeHeadLine(final ClassicHttpResponse message, final CharArrayBuffer lineBuf) throws IOException {
        final ProtocolVersion transportVersion = message.getVersion();
        getLineFormatter().formatStatusLine(lineBuf, new StatusLine(
                transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1,
                message.getCode(),
                message.getReasonPhrase()));
    }
}
