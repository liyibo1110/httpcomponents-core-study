package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.concurrent.DefaultThreadFactory;
import com.github.liyibo1110.hc.core5.function.Callback;
import com.github.liyibo1110.hc.core5.http.ExceptionListener;
import com.github.liyibo1110.hc.core5.http.URIScheme;
import com.github.liyibo1110.hc.core5.http.config.CharCodingConfig;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import com.github.liyibo1110.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import com.github.liyibo1110.hc.core5.http.impl.io.HttpService;
import com.github.liyibo1110.hc.core5.http.io.HttpConnectionFactory;
import com.github.liyibo1110.hc.core5.http.io.SocketConfig;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.io.Closer;
import com.github.liyibo1110.hc.core5.io.ModalCloseable;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TimeValue;
import com.github.liyibo1110.hc.core5.util.Timeout;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 名字已经说明用途了，就是传统意义上的阻塞式的可启动/关闭的server，职责是：
 * 1、接收连接。
 * 2、为连接创建对应的worker和service执行环境。
 * 3、委托给HttpService和DefaultBHttpServerConnection去处理请求。
 * @author liyibo
 * @date 2026-04-10 15:41
 */
public class HttpServer implements ModalCloseable {

    enum Status {
        READY,
        ACTIVE,
        STOPPING
    }

    private final int port;
    private final InetAddress ifAddress;
    private final SocketConfig socketConfig;
    private final ServerSocketFactory serverSocketFactory;
    private final HttpService httpService;
    private final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private final Callback<SSLParameters> sslSetupHandler;
    private final ExceptionListener exceptionListener;

    /** 专门跑RequestListener的线程池 */
    private final ThreadPoolExecutor listenerExecutorService;
    private final ThreadGroup workerThreads;

    /** 专门跑Worker的线程池 */
    private final WorkerPoolExecutor workerExecutorService;
    private final AtomicReference<Status> status;

    private volatile ServerSocket serverSocket;
    private volatile RequestListener requestListener;

    @Internal
    public HttpServer(final int port,
                      final HttpService httpService,
                      final InetAddress ifAddress,
                      final SocketConfig socketConfig,
                      final ServerSocketFactory serverSocketFactory,
                      final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory,
                      final Callback<SSLParameters> sslSetupHandler,
                      final ExceptionListener exceptionListener) {
        this.port = Args.notNegative(port, "Port value is negative");
        this.httpService = Args.notNull(httpService, "HTTP service");
        this.ifAddress = ifAddress;
        this.socketConfig = socketConfig != null ? socketConfig : SocketConfig.DEFAULT;
        this.serverSocketFactory = serverSocketFactory != null ? serverSocketFactory : ServerSocketFactory.getDefault();
        this.connectionFactory = connectionFactory != null ? connectionFactory : new DefaultBHttpServerConnectionFactory(
                this.serverSocketFactory instanceof SSLServerSocketFactory ? URIScheme.HTTPS.id : URIScheme.HTTP.id,
                Http1Config.DEFAULT,
                CharCodingConfig.DEFAULT);
        this.sslSetupHandler = sslSetupHandler;
        this.exceptionListener = exceptionListener != null ? exceptionListener : ExceptionListener.NO_OP;
        this.listenerExecutorService = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new DefaultThreadFactory("HTTP-listener-" + this.port));
        this.workerThreads = new ThreadGroup("HTTP-workers");
        this.workerExecutorService = new WorkerPoolExecutor(
                0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new DefaultThreadFactory("HTTP-worker", this.workerThreads, true));
        this.status = new AtomicReference<>(Status.READY);
    }

    public InetAddress getInetAddress() {
        final ServerSocket localSocket = this.serverSocket;
        if (localSocket != null)
            return localSocket.getInetAddress();
        return null;
    }

    public int getLocalPort() {
        final ServerSocket localSocket = this.serverSocket;
        if (localSocket != null)
            return localSocket.getLocalPort();
        return -1;
    }

    public void start() throws IOException {
        if (this.status.compareAndSet(Status.READY, Status.ACTIVE)) {
            this.serverSocket = this.serverSocketFactory.createServerSocket(this.port, this.socketConfig.getBacklogSize(), this.ifAddress);
            this.serverSocket.setReuseAddress(this.socketConfig.isSoReuseAddress());
            if (this.socketConfig.getRcvBufSize() > 0)
                this.serverSocket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
            if (this.sslSetupHandler != null && this.serverSocket instanceof SSLServerSocket) {
                final SSLServerSocket sslServerSocket = (SSLServerSocket) this.serverSocket;
                final SSLParameters sslParameters = sslServerSocket.getSSLParameters();
                this.sslSetupHandler.execute(sslParameters);
                sslServerSocket.setSSLParameters(sslParameters);
            }
            // 在这里初始化真正接收request的线程组件
            this.requestListener = new RequestListener(this.socketConfig, this.serverSocket, this.httpService,
                                                       this.connectionFactory, this.exceptionListener, this.workerExecutorService);
            this.listenerExecutorService.execute(this.requestListener);
        }
    }

    public void stop() {
        if (this.status.compareAndSet(Status.ACTIVE, Status.STOPPING)) {
            this.listenerExecutorService.shutdownNow();
            this.workerExecutorService.shutdown();
            final RequestListener local = this.requestListener;
            if(local != null) {
                try {
                    local.terminate();
                } catch (final IOException ex) {
                    this.exceptionListener.onError(ex);
                }
            }
            this.workerThreads.interrupt();
        }
    }

    public void initiateShutdown() {
        stop();
    }

    public void awaitTermination(final TimeValue waitTime) throws InterruptedException {
        Args.notNull(waitTime, "Wait time");
        this.workerExecutorService.awaitTermination(waitTime.getDuration(), waitTime.getTimeUnit());
    }

    @Override
    public void close(final CloseMode closeMode) {
        close(closeMode, Timeout.ofSeconds(5));
    }

    public void close(final CloseMode closeMode, final Timeout timeout) {
        initiateShutdown();
        if (closeMode == CloseMode.GRACEFUL) {
            try {
                awaitTermination(timeout);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        final Set<Worker> workers = this.workerExecutorService.getWorkers();
        for (final Worker worker: workers)
            Closer.close(worker.getConnection(), CloseMode.GRACEFUL);
    }

    @Override
    public void close() {
        close(CloseMode.GRACEFUL);
    }
}
