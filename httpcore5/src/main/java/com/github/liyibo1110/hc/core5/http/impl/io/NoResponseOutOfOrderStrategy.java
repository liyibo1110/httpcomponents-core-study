package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.io.HttpClientConnection;
import com.github.liyibo1110.hc.core5.http.io.ResponseOutOfOrderStrategy;

import java.io.InputStream;

/**
 * 这是一个不检查早期响应的ResponseOutOfOrderStrategy实现。
 * 检测早期响应需要进行1毫秒的阻塞读取，这会给大型上传带来巨大的性能开销。
 * @author liyibo
 * @date 2026-04-10 11:45
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class NoResponseOutOfOrderStrategy implements ResponseOutOfOrderStrategy {

    public static final NoResponseOutOfOrderStrategy INSTANCE = new NoResponseOutOfOrderStrategy();

    @Override
    public boolean isEarlyResponseDetected(final ClassicHttpRequest request,
                                           final HttpClientConnection connection,
                                           final InputStream inputStream,
                                           final long totalBytesSent,
                                           final long nextWriteSize) {
        return false;
    }
}
