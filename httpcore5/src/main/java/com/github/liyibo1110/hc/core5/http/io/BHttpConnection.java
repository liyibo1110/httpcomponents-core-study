package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.http.HttpConnection;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.io.IOException;

/**
 * 阻塞的HttpConnection扩展抽象
 * @author liyibo
 * @date 2026-04-07 13:56
 */
public interface BHttpConnection extends HttpConnection {

    /**
     * 检查连接中是否存在可用输入数据。
     * 可能会等待指定的timeout，直到有数据可用。
     * 请注意，某些实现可能会完全忽略timeout。
     */
    boolean isDataAvailable(Timeout timeout) throws IOException;

    /**
     * 检查此连接是否已断开。
     * 由于多种原因，网络连接在一段时间内处于闲置状态时可能会被关闭。下次尝试读取此类连接时，将抛出IOException。
     * 本方法试图通过检查连接是否仍可用来缓解这一不便。具体实现可能通过设置极短的超时时间来尝试读取。
     * 因此，本方法在返回结果前可能会阻塞一小段时间。因此这是一项开销较大的操作。
     */
    boolean isStale() throws IOException;

    /**
     * 将所有缓冲中的待传输数据通过已建立的连接写出。
     */
    void flush() throws IOException;
}
