package com.github.liyibo1110.hc.core5.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * close工具类
 * @author liyibo
 * @date 2026-04-03 15:05
 */
public final class Closer {

    /**
     * 以空安全的方式关闭指定的Closeable对象。
     */
    public static void close(final Closeable closeable) throws IOException {
        if (closeable != null)
            closeable.close();
    }

    /**
     * 以空安全的方式关闭指定的ModalCloseable对象。
     */
    public static void close(final ModalCloseable closeable, final CloseMode closeMode) {
        if (closeable != null)
            closeable.close(closeMode);
    }

    /**
     * 以空安全的方式静默关闭指定的Closeable对象，即使发生异常也不例外。
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            close(closeable);
        } catch (final IOException e) {
            // Quietly ignore
        }
    }
}
