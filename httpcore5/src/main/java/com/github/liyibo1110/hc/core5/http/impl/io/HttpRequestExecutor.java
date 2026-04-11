package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.UnsupportedHttpVersionException;
import com.github.liyibo1110.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.impl.Http1StreamListener;
import com.github.liyibo1110.hc.core5.http.io.HttpClientConnection;
import com.github.liyibo1110.hc.core5.http.io.HttpResponseInformationCallback;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.http.message.StatusLine;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.http.protocol.HttpCoreContext;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;
import com.github.liyibo1110.hc.core5.io.Closer;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * HttpRequestExecutor是一个基于阻塞I/O模型的客户端HTTP协议处理器。
 *
 * HttpRequestExecutor依赖于HttpProcessor来为所有出站消息生成必需的协议头，并对所有入站和出站消息应用通用的、横切的消息转换。
 * 在请求执行完毕且收到响应后，应用程序特有的处理可以在HttpRequestExecutor外部实现。
 *
 * 即HttpService是server端的对一次请求处理的总控，HttpRequestExecutor对应就是client端的一次请求执行的总控。
 * 负责按HTTP/1.1协议，做发请求，以及收响应这一整次的exchange，还负责：
 * 1、处理Expect: 100-continue。
 * 2、处理1xx中间响应。
 * 3、决定连接能否用keep-alive。
 *
 * HttpRequestExecutor
 *    ↓ 调度
 * HttpClientConnection（通常是DefaultBHttpClientConnection）
 *    ↓ 依赖
 * BHttpConnectionBase
 *    ↓ 操作
 * Socket / InputStream / OutputStream
 *
 * @author liyibo
 * @date 2026-04-10 13:47
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class HttpRequestExecutor {

    public static final Timeout DEFAULT_WAIT_FOR_CONTINUE = Timeout.ofSeconds(3);

    /** 专用于Expect: 100-continue，默认是上面的3秒，如果request带了这个头，会最多等3秒，看server会不会回复临时响应 */
    private final Timeout waitForContinue;
    private final ConnectionReuseStrategy connReuseStrategy;
    private final Http1StreamListener streamListener;

    public HttpRequestExecutor(final Timeout waitForContinue,
                               final ConnectionReuseStrategy connReuseStrategy,
                               final Http1StreamListener streamListener) {
        super();
        this.waitForContinue = Args.positive(waitForContinue, "Wait for continue time");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public HttpRequestExecutor(final ConnectionReuseStrategy connReuseStrategy) {
        this(DEFAULT_WAIT_FOR_CONTINUE, connReuseStrategy, null);
    }

    public HttpRequestExecutor() {
        this(DEFAULT_WAIT_FOR_CONTINUE, null, null);
    }

    public ClassicHttpResponse execute(final ClassicHttpRequest request,
                                       final HttpClientConnection conn,
                                       final HttpResponseInformationCallback informationCallback,
                                       final HttpContext context) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(conn, "Client connection");
        Args.notNull(context, "HTTP context");
        try {
            context.setAttribute(HttpCoreContext.SSL_SESSION, conn.getSSLSession());
            context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, conn.getEndpointDetails());

            // 发request head
            conn.sendRequestHeader(request);
            if (streamListener != null)
                streamListener.onRequestHead(conn, request);

            boolean expectContinue = false;
            final HttpEntity entity = request.getEntity();
            if (entity != null) {
                final Header expect = request.getFirstHeader(HttpHeaders.EXPECT);
                expectContinue = expect != null && HeaderElements.CONTINUE.equalsIgnoreCase(expect.getValue());
                if (!expectContinue)
                    conn.sendRequestEntity(request);    // 有body，同时没有Expect: 100-continue，就发request body
            }
            conn.flush();

            ClassicHttpResponse response = null;
            // 轮询等待response
            while (response == null) {
                if (expectContinue) {   // 等Expect: 100-continue的临时响应的路线
                    if (conn.isDataAvailable(this.waitForContinue)) {
                        response = conn.receiveResponseHeader();
                        if (streamListener != null)
                            streamListener.onResponseHead(conn, response);
                        final int status = response.getCode();
                        if (status == HttpStatus.SC_CONTINUE) { // 收到了100响应，继续发body然后重新等
                            response = null;
                            conn.sendRequestEntity(request);
                        } else if(status < HttpStatus.SC_SUCCESS) { // 收到了1xx响应，执行传入的回调，直接重新等
                            if (informationCallback != null)
                                informationCallback.execute(response, conn, context);
                            response = null;
                            continue;
                        } else if(status >= HttpStatus.SC_CLIENT_ERROR) {   // 响应结果是错误
                            conn.terminateRequest(request);
                        } else {
                            conn.sendRequestEntity(request);
                        }
                    } else {    // 超时了没有等到100响应，直接发body
                        conn.sendRequestEntity(request);
                    }
                    conn.flush();
                    expectContinue = false;
                } else {    // 正常的没有Expect: 100-continue的路线
                    response = conn.receiveResponseHeader();    // 阻塞获取response head
                    if (streamListener != null)
                        streamListener.onResponseHead(conn, response);
                    // 验证status code有效性
                    final int status = response.getCode();
                    if (status < HttpStatus.SC_INFORMATIONAL)
                        throw new ProtocolException("Invalid response: " + new StatusLine(response));
                    // 收到了1xx的响应
                    if (status < HttpStatus.SC_SUCCESS)
                        if (informationCallback != null && status != HttpStatus.SC_CONTINUE) {
                            informationCallback.execute(response, conn, context);
                        response = null;
                    }
                }
            }
            // 上面只接收了response的head部分，到这里才接收body部分
            if (MessageSupport.canResponseHaveBody(request.getMethod(), response))
                conn.receiveResponseEntity(response);
            return response;
        } catch (final HttpException | IOException | RuntimeException ex) {
            Closer.closeQuietly(conn);
            throw ex;
        }
    }

    public ClassicHttpResponse execute(final ClassicHttpRequest request,
                                       final HttpClientConnection conn,
                                       final HttpContext context) throws IOException, HttpException {
        return execute(request, conn, null, context);
    }

    /**
     * 触发发送request之前的HttpProcessor的职责链。
     */
    public void preProcess(final ClassicHttpRequest request, final HttpProcessor processor, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(processor, "HTTP processor");
        Args.notNull(context, "HTTP context");
        final ProtocolVersion transportVersion = request.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2))
            throw new UnsupportedHttpVersionException(transportVersion);

        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
        processor.process(request, request.getEntity(), context);
    }

    /**
     * 触发收到response之后的HttpProcessor的职责链。
     */
    public void postProcess(final ClassicHttpResponse response, final HttpProcessor processor, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        Args.notNull(processor, "HTTP processor");
        Args.notNull(context, "HTTP context");
        final ProtocolVersion transportVersion = response.getVersion();
        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
        context.setAttribute(HttpCoreContext.HTTP_RESPONSE, response);
        processor.process(response, response.getEntity(), context);
    }

    public boolean keepAlive(final ClassicHttpRequest request,
                             final ClassicHttpResponse response,
                             final HttpClientConnection connection,
                             final HttpContext context) throws IOException {
        Args.notNull(connection, "HTTP connection");
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        // 委托策略，返回是否可以keep-alive
        final boolean keepAlive = connection.isConsistent() && connReuseStrategy.keepAlive(request, response, context);
        if (streamListener != null)
            streamListener.onExchangeComplete(connection, keepAlive);
        return keepAlive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Timeout waitForContinue;
        private ConnectionReuseStrategy connReuseStrategy;
        private Http1StreamListener streamListener;

        private Builder() {}

        public Builder withWaitForContinue(final Timeout waitForContinue) {
            this.waitForContinue = waitForContinue;
            return this;
        }

        public Builder withConnectionReuseStrategy(final ConnectionReuseStrategy connReuseStrategy) {
            this.connReuseStrategy = connReuseStrategy;
            return this;
        }

        public Builder withHttp1StreamListener(final Http1StreamListener streamListener) {
            this.streamListener = streamListener;
            return this;
        }

        public HttpRequestExecutor build() {
            return new HttpRequestExecutor(waitForContinue, connReuseStrategy, streamListener);
        }
    }
}
