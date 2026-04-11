package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import com.github.liyibo1110.hc.core5.http.ExceptionListener;
import com.github.liyibo1110.hc.core5.http.impl.io.HttpService;
import com.github.liyibo1110.hc.core5.http.io.HttpConnectionFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpServerConnection;
import com.github.liyibo1110.hc.core5.http.io.SocketConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在特定ServerSocket上，监听新来的请求。
 * @author liyibo
 * @date 2026-04-10 16:51
 */
class RequestListener implements Runnable {

    private final SocketConfig socketConfig;
    private final ServerSocket serverSocket;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory;
    private final ExceptionListener exceptionListener;
    private final ExecutorService executorService;
    private final AtomicBoolean terminated;

    public RequestListener(final SocketConfig socketConfig,
                           final ServerSocket serversocket,
                           final HttpService httpService,
                           final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory,
                           final ExceptionListener exceptionListener,
                           final ExecutorService executorService) {
        this.socketConfig = socketConfig;
        this.serverSocket = serversocket;
        this.connectionFactory = connectionFactory;
        this.httpService = httpService;
        this.exceptionListener = exceptionListener;
        this.executorService = executorService;
        this.terminated = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        try {
            // 循环监听并处理新来的请求
            while (!isTerminated() && !Thread.interrupted()) {
                final Socket socket = this.serverSocket.accept();
                socket.setSoTimeout(this.socketConfig.getSoTimeout().toMillisecondsIntBound());
                socket.setKeepAlive(this.socketConfig.isSoKeepAlive());
                socket.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
                if (this.socketConfig.getRcvBufSize() > 0)
                    socket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
                if (this.socketConfig.getSndBufSize() > 0)
                    socket.setSendBufferSize(this.socketConfig.getSndBufSize());
                if (this.socketConfig.getSoLinger().toSeconds() >= 0)
                    socket.setSoLinger(true, this.socketConfig.getSoLinger().toSecondsIntBound());
                // 创建Connection
                final HttpServerConnection conn = this.connectionFactory.createConnection(socket);
                // 创建Worker，放到线程池里去跑
                final Worker worker = new Worker(this.httpService, conn, this.exceptionListener);
                this.executorService.execute(worker);
            }
        } catch (final Exception ex) {
            this.exceptionListener.onError(ex);
        }
    }

    public boolean isTerminated() {
        return this.terminated.get();
    }

    public void terminate() throws IOException {
        if (this.terminated.compareAndSet(false, true))
            this.serverSocket.close();
    }
}
