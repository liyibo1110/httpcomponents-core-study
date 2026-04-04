package com.github.liyibo1110.hc.core5.io;

import java.io.Closeable;

/**
 * 可立即或优雅关闭的endpoint或process。
 * @author liyibo
 * @date 2026-04-03 15:01
 */
public interface ModalCloseable extends Closeable {

    /**
     * 关闭此process或endpoint，并释放与其关联的所有系统资源。
     * 如果process或endpoint已被关闭，则调用不产生任何效果。
     */
    void close(CloseMode closeMode);
}
