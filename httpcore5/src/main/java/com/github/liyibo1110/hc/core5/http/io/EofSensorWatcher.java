package com.github.liyibo1110.hc.core5.http.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * EofSensorInputStream的监听器。每个流最多会向其监听器发送一次通知。
 * @author liyibo
 * @date 2026-04-07 14:40
 */
public interface EofSensorWatcher {

    /**
     * 表示检测到EOF。
     */
    boolean eofDetected(InputStream wrapped) throws IOException;

    /**
     * 表示流已关闭。
     * 只有在关闭前未检测到EOF时，才会调用此方法。否则将调用eofDetected方法。
     */
    boolean streamClosed(InputStream wrapped) throws IOException;

    /**
     * 表示流已被中止。只有在中止之前未检测到EOF时，才会调用此方法。否则将调用eofDetected方法。
     * 当输入操作抛出IOException时，也会调用此方法，以确保输入流被关闭。
     */
    boolean streamAbort(InputStream wrapped) throws IOException;
}
