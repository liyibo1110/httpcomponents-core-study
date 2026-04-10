package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 用于HTTP/1.1阻塞连接的会话输入缓冲区。
 * 该接口支持对来自输入流的输入数据进行中间缓冲，并提供了用于读取文本行的方法。
 * @author liyibo
 * @date 2026-04-07 14:49
 */
public interface SessionInputBuffer {

    /**
     * 返回缓冲区中存储的数据长度。
     */
    int length();

    /**
     * 返回缓冲区的总容量。
     */
    int capacity();

    /**
     * 返回缓冲区的可用容量。
     */
    int available();

    /**
     * 从会话缓冲区读取最多len个字节的数据到一个字节数组中。该函数会尝试读取len个字节，但实际读取的字节数可能较少，甚至为零。
     * 实际读取的字节数将作为整数返回。
     *
     * 此方法将阻塞，直到有输入数据可用、检测到文件末尾，或抛出异常。
     * 如果off为负数，或len为负数，或off+len大于数组b的长度，则会抛出IndexOutOfBoundsException。
     */
    int read(byte[] b, int off, int len, InputStream inputStream) throws IOException;

    int read(byte[] b, InputStream inputStream) throws IOException;

    int read(InputStream inputStream) throws IOException;

    /**
     * 从该会话缓冲区读取完整的一行字符（直至行分隔符）并写入给定的行缓冲区。返回实际读取的字符数（整数）。
     * 行分隔符本身将被丢弃。如果因已到达流尾而无字符可读取，则返回-1。
     *
     * 此方法将阻塞，直到有输入数据可用、检测到文件末尾或抛出异常为止。
     *
     * 字符编码和行分隔符序列的选择取决于该接口的具体实现。
     */
    int readLine(CharArrayBuffer buffer, InputStream inputStream) throws IOException;

    HttpTransportMetrics getMetrics();
}
