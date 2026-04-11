package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.io.HttpClientConnection;
import com.github.liyibo1110.hc.core5.http.io.ResponseOutOfOrderStrategy;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一种ResponseOutOfOrderStrategy实现，每chunkSize字节检查一次是否存在过早响应。
 * 基于对4KiB至128KiB之间值的测试，默认使用8KiB的分块大小。
 *
 * 该实现针对正确性进行了优化，在达到maxChunksToCheck之前，上传速度最高可达8MiB/s。
 * @author liyibo
 * @date 2026-04-10 11:47
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class MonitoringResponseOutOfOrderStrategy implements ResponseOutOfOrderStrategy {

    private static final int DEFAULT_CHUNK_SIZE = 8 * 1024;

    public static final MonitoringResponseOutOfOrderStrategy INSTANCE = new MonitoringResponseOutOfOrderStrategy();

    private final long chunkSize;
    private final long maxChunksToCheck;

    public MonitoringResponseOutOfOrderStrategy() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public MonitoringResponseOutOfOrderStrategy(final long chunkSize) {
        this(chunkSize, Long.MAX_VALUE);
    }

    public MonitoringResponseOutOfOrderStrategy(final long chunkSize, final long maxChunksToCheck) {
        this.chunkSize = Args.positive(chunkSize, "chunkSize");
        this.maxChunksToCheck = Args.positive(maxChunksToCheck, "maxChunksToCheck");
    }

    @Override
    public boolean isEarlyResponseDetected(final ClassicHttpRequest request,
                                           final HttpClientConnection connection,
                                           final InputStream inputStream,
                                           final long totalBytesSent,
                                           final long nextWriteSize) throws IOException {
        if (nextWriteStartsNewChunk(totalBytesSent, nextWriteSize)) {
            final boolean ssl = connection.getSSLSession() != null;
            return ssl ? connection.isDataAvailable(Timeout.ONE_MILLISECOND) : (inputStream.available() > 0);
        }
        return false;
    }

    private boolean nextWriteStartsNewChunk(final long totalBytesSent, final long nextWriteSize) {
        final long currentChunkIndex = Math.min(totalBytesSent / chunkSize, maxChunksToCheck);
        final long newChunkIndex = Math.min((totalBytesSent + nextWriteSize) / chunkSize, maxChunksToCheck);
        return currentChunkIndex < newChunkIndex;
    }

    @Override
    public String toString() {
        return "DefaultResponseOutOfOrderStrategy{chunkSize=" + chunkSize + ", maxChunksToCheck=" + maxChunksToCheck + '}';
    }
}
