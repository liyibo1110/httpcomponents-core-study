package com.github.liyibo1110.hc.core5.http.io;

import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一种在调用close()方法或遇到EOF时触发操作的流封装器。
 * 主要用于在响应正文已被读取完毕或不再需要时，自动释放底层的托管连接。
 * @author liyibo
 * @date 2026-04-07 14:38
 */
public class EofSensorInputStream extends InputStream {

    private InputStream wrappedStream;

    /**
     * 表示该流本身是否已关闭。
     * 如果该流未关闭，但wrappedStream为null，则表示当前处于EOF模式。所有读取操作都会返回EOF，而不会访问底层流。
     * 关闭该流后，读取操作将引发IOException。
     */
    private boolean selfClosed;

    /**
     * 要通知的监听器。
     */
    private final EofSensorWatcher eofWatcher;

    public EofSensorInputStream(final InputStream in, final EofSensorWatcher watcher) {
        Args.notNull(in, "Wrapped stream");
        wrappedStream = in;
        selfClosed = false;
        eofWatcher = watcher;
    }

    boolean isSelfClosed() {
        return selfClosed;
    }

    InputStream getWrappedStream() {
        return wrappedStream;
    }

    /**
     * 检查底层流是否可供读取。
     */
    private boolean isReadAllowed() throws IOException {
        if (selfClosed)
            throw new IOException("Attempted read on closed stream.");
        return wrappedStream != null;
    }

    @Override
    public int read() throws IOException {
        int b = -1;
        if (isReadAllowed()) {
            try {
                b = wrappedStream.read();
                checkEOF(b);
            } catch (final IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return b;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        int readLen = -1;
        if (isReadAllowed()) {
            try {
                readLen = wrappedStream.read(b,  off,  len);
                checkEOF(readLen);
            } catch (final IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return readLen;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int available() throws IOException {
        int a = 0; // not -1
        if (isReadAllowed()) {
            try {
                a = wrappedStream.available();
                // no checkEOF() here, available() can't trigger EOF
            } catch (final IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return a;
    }

    @Override
    public void close() throws IOException {
        // tolerate multiple calls to close()
        selfClosed = true;
        checkClose();
    }

    /**
     * 检测EOF并通知监听器。仅当底层流仍可访问时，才应调用此方法。请使用isReadAllowed方法检查该条件。
     * 如果检测到EOF，监听器将收到通知，且该流将与底层流脱离，这可防止该流发出多次通知。
     */
    private void checkEOF(final int eof) throws IOException {
        final InputStream toCheckStream = wrappedStream;
        if ((toCheckStream != null) && (eof < 0)) {
            try {
                boolean scws = true; // should close wrapped stream?
                if (eofWatcher != null)
                    scws = eofWatcher.eofDetected(toCheckStream);
                if (scws)
                    toCheckStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }

    /**
     * 检测流关闭并通知监听器。由于此方法由close调用，因此无需进行太多检测。
     * 只有当该流首次关闭且在检测到文件末尾 (EOF) 之前，监听器才会收到通知。
     * 该流将与底层流解耦，以避免向监听器发送多次通知。
     */
    private void checkClose() throws IOException {

        final InputStream toCloseStream = wrappedStream;
        if (toCloseStream != null) {
            try {
                boolean scws = true; // should close wrapped stream?
                if (eofWatcher != null)
                    scws = eofWatcher.streamClosed(toCloseStream);
                if (scws)
                    toCloseStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }

    /**
     * 检测流中止并通知监听器。由于此方法由abort()调用，因此无需进行太多检测。
     * 只有当该流首次被中止，且在检测到文件末尾（EOF）或流被正常关闭之前，监听器才会收到通知。
     * 该流将与底层流解耦，以防止向监听器发送多次通知。
     */
    private void checkAbort() throws IOException {
        final InputStream toAbortStream = wrappedStream;
        if (toAbortStream != null) {
            try {
                boolean scws = true; // should close wrapped stream?
                if (eofWatcher != null)
                    scws = eofWatcher.streamAbort(toAbortStream);
                if (scws)
                    toAbortStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }

    /**
     * 终止此流。这是close()方法的一个特殊版本，它会阻止底层连接（如有）被重复使用。
     * 调用此方法表示在流结束之前不应尝试进行读取操作。
     */
    public void abort() throws IOException {
        // tolerate multiple calls
        selfClosed = true;
        checkAbort();
    }
}
