package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 实用类，用于保存一个Socket及其InputStream和OutputStream的副本。
 * @author liyibo
 * @date 2026-04-09 15:52
 */
public class SocketHolder {
    private final Socket socket;
    private final AtomicReference<InputStream> inputStreamRef;
    private final AtomicReference<OutputStream> outputStreamRef;

    public SocketHolder(final Socket socket) {
        this.socket = Args.notNull(socket, "Socket");
        this.inputStreamRef = new AtomicReference<>();
        this.outputStreamRef = new AtomicReference<>();
    }

    public final Socket getSocket() {
        return socket;
    }

    public final InputStream getInputStream() throws IOException {
        InputStream local = inputStreamRef.get();
        if (local != null)
            return local;
        local = getInputStream(socket);
        if (inputStreamRef.compareAndSet(null, local))
            return local;
        return inputStreamRef.get();
    }

    protected InputStream getInputStream(final Socket socket) throws IOException {
        return socket.getInputStream();
    }

    public final OutputStream getOutputStream() throws IOException {
        OutputStream local = outputStreamRef.get();
        if (local != null)
            return local;
        local = getOutputStream(socket);
        if (outputStreamRef.compareAndSet(null, local))
            return local;
        return outputStreamRef.get();
    }

    protected OutputStream getOutputStream(final Socket socket) throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
