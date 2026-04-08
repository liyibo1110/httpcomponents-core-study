package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 用于阻塞式HTTP/1.1连接的会话输出缓冲区。
 * 该接口支持对流向输出流的输出数据进行中间缓冲，并提供了用于写入文本行（行）的方法
 * @author liyibo
 * @date 2026-04-07 14:54
 */
public interface SessionOutputBuffer {

    int length();

    int capacity();

    int available();

    void write(byte[] b, int off, int len, OutputStream outputStream) throws IOException;

    void write(byte[] b, OutputStream outputStream) throws IOException;

    void write(int b, OutputStream outputStream) throws IOException;

    void writeLine(CharArrayBuffer buffer, OutputStream outputStream) throws IOException;

    void flush(OutputStream outputStream) throws IOException;

    HttpTransportMetrics getMetrics();
}
