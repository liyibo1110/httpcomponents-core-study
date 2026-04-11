package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

import com.github.liyibo1110.hc.core5.http.ExceptionListener;
import com.github.liyibo1110.hc.core5.http.impl.io.HttpService;
import com.github.liyibo1110.hc.core5.http.io.HttpServerConnection;
import com.github.liyibo1110.hc.core5.http.protocol.BasicHttpContext;
import com.github.liyibo1110.hc.core5.http.protocol.HttpCoreContext;
import com.github.liyibo1110.hc.core5.io.CloseMode;

/**
 * server中，处理一个已接入连接的执行单元。
 * 即某个socket已经经过了accept了，这个worker绑定了一个connection，顺序执行多次request/response exchange，直到连接关闭或不再keep-alive。
 * @author liyibo
 * @date 2026-04-10 15:42
 */
class Worker implements Runnable {
    private final HttpService httpservice;
    private final HttpServerConnection conn;
    private final ExceptionListener exceptionListener;

    Worker(final HttpService httpservice, final HttpServerConnection conn, final ExceptionListener exceptionListener) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
        this.exceptionListener = exceptionListener;
    }

    public HttpServerConnection getConnection() {
        return this.conn;
    }

    @Override
    public void run() {
        try {
            final BasicHttpContext localContext = new BasicHttpContext();
            final HttpCoreContext context = HttpCoreContext.adapt(localContext);
            // 开始循环处理收到的request
            while (!Thread.interrupted() && conn.isOpen()) {
                httpservice.handleRequest(conn, context);   // 注意这里代表完整地处理了一轮
                localContext.clear();   // 清理上一次的context内容
            }
            conn.close();
        } catch (final Exception ex) {
            this.exceptionListener.onError(this.conn, ex);
        } finally {
            conn.close(CloseMode.IMMEDIATE);
        }
    }
}
