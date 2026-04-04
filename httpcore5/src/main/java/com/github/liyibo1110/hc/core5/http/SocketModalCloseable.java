package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.io.ModalCloseable;
import com.github.liyibo1110.hc.core5.util.Timeout;

/**
 * 基于网络socket的ModalCloseable子类。
 * @author liyibo
 * @date 2026-04-03 15:07
 */
public interface SocketModalCloseable extends ModalCloseable {

    Timeout getSocketTimeout();

    void setSocketTimeout(Timeout timeout);
}
