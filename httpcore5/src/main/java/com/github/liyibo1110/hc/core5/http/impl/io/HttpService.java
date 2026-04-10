package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpRequestMapper;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.UnsupportedHttpVersionException;
import com.github.liyibo1110.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import com.github.liyibo1110.hc.core5.http.impl.Http1StreamListener;
import com.github.liyibo1110.hc.core5.http.impl.ServerSupport;
import com.github.liyibo1110.hc.core5.http.io.HttpRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.HttpServerConnection;
import com.github.liyibo1110.hc.core5.http.io.HttpServerRequestHandler;
import com.github.liyibo1110.hc.core5.http.io.entity.EntityUtils;
import com.github.liyibo1110.hc.core5.http.io.entity.StringEntity;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.message.MessageSupport;
import com.github.liyibo1110.hc.core5.http.protocol.HttpContext;
import com.github.liyibo1110.hc.core5.http.protocol.HttpCoreContext;
import com.github.liyibo1110.hc.core5.http.protocol.HttpProcessor;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HttpService是一个基于经典（阻塞）I/O模型的服务器端HTTP协议处理器。
 * HttpService依赖于HttpProcessor来为所有发出的消息生成必需的协议头，并对所有传入和传出的消息应用通用的、横切的消息转换，
 * 而各个HttpRequestHandler则负责实现应用程序特有的内容生成和处理。
 * HttpService使用HttpRequestMapper来为传入HTTP请求的特定请求 URI 映射匹配的请求处理程序。
 *
 * 首先要注意一个巨大的认知误区：这个core项目是包含server side组件的，这个HttpService就算一个，平时开发人员在项目中使用的基本都是client项目，
 * core项目既提供了client所需要的底层接口和实现，又提供了server端的相关接口和实现。
 *
 * HttpService可以看作是：一次HTTP连接请求，负责classic server主干流程的调度总控制，大概流程是：
 * 1、读入请求
 * 2、跑协议处理器
 * 3、找到业务处理器
 * 4、生成响应
 * 5、写回响应
 * 6、判断是否 keep-alive
 * @author liyibo
 * @date 2026-04-08 17:52
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpService {

    /** 协议处理链/拦截器链，负责请求和响应的通用协议处理，不负责具体业务处理。 */
    private final HttpProcessor processor;

    /**
     * 真正的server端请求处理器的入口。
     * 不只是单个业务handler，它里面还有ResponseTrigger，即可以控制响应提交的流程。
     */
    private final HttpServerRequestHandler requestHandler;

    /** 决定请求结束后，连接能否继续复用。 */
    private final ConnectionReuseStrategy connReuseStrategy;

    /** 监听request头、response头、一次交换完成等事件的观察器。 */
    private final Http1StreamListener streamListener;

    public HttpService(final HttpProcessor processor,
                       final HttpRequestMapper<HttpRequestHandler> handlerMapper,
                       final ConnectionReuseStrategy connReuseStrategy,
                       final HttpResponseFactory<ClassicHttpResponse> responseFactory,
                       final Http1StreamListener streamListener) {

    }

    public HttpService(final HttpProcessor processor,
                       final HttpRequestMapper<HttpRequestHandler> handlerMapper,
                       final ConnectionReuseStrategy connReuseStrategy,
                       final HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this(processor, handlerMapper, connReuseStrategy, responseFactory, null);
    }

    public HttpService(final HttpProcessor processor,
                       final HttpServerRequestHandler requestHandler,
                       final ConnectionReuseStrategy connReuseStrategy,
                       final Http1StreamListener streamListener) {
        super();
        this.processor =  Args.notNull(processor, "HTTP processor");
        this.requestHandler =  Args.notNull(requestHandler, "Request handler");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.streamListener = streamListener;
    }

    public HttpService(final HttpProcessor processor,
                       final HttpServerRequestHandler requestHandler) {
        this(processor, requestHandler, null, null);
    }

    /**
     * server side的核心处理方法。
     *
     * HttpServerConnection代表了server端classic HTTP连接的抽象，它负责：
     * 1、接收request header/entity。
     * 2、发出response header/entity。
     * 3、close和flush。
     * 可以看作是：socket之上的HTTP/1.1的连接外壳。
     */
    public void handleRequest(final HttpServerConnection conn, final HttpContext context) throws IOException, HttpException {
        /**
         * 防止重复提交最终响应。
         * 因为后面最重要的handle调用，传入的是new出来的ResponseTrigger，在里面会判断是否已经提交过响应了。
         */
        final AtomicBoolean responseSubmitted = new AtomicBoolean(false);

        try {
            // 从request底层流里读请求header
            final ClassicHttpRequest request = conn.receiveRequestHeader();
            if (request == null) {
                conn.close();
                return;
            }
            if (streamListener != null)
                streamListener.onRequestHead(conn, request);
            // 从request底层流里读请求entity
            conn.receiveRequestEntity(request);

            // 关键信息写入context，目的就是给后续流程共享
            final ProtocolVersion transportVersion = request.getVersion();
            context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
            context.setAttribute(HttpCoreContext.SSL_SESSION, conn.getSSLSession());
            context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, conn.getEndpointDetails());
            context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
            this.processor.process(request, request.getEntity(), context);

            // 核心调用
            this.requestHandler.handle(request, new HttpServerRequestHandler.ResponseTrigger() {
                /**
                 * 发1xx中间临时响应，注意不会发entity，因为要求如此。
                 */
                @Override
                public void sendInformation(final ClassicHttpResponse response) throws HttpException, IOException {
                    if (responseSubmitted.get())
                        throw new HttpException("Response already submitted");
                    // 临时响应的状态码必须是小于200的
                    if (response.getCode() >= HttpStatus.SC_SUCCESS)
                        throw new HttpException("Invalid intermediate response");
                    if (streamListener != null)
                        streamListener.onResponseHead(conn, response);
                    conn.sendResponseHeader(response);
                    conn.flush();
                }

                @Override
                public void submitResponse(final ClassicHttpResponse response) throws HttpException, IOException {
                    try {
                        // 检查协议版本
                        final ProtocolVersion transportVersion = response.getVersion();
                        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2))
                            throw new UnsupportedHttpVersionException(transportVersion);
                        ServerSupport.validateResponse(response, response.getEntity());
                        context.setProtocolVersion(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1);
                        context.setAttribute(HttpCoreContext.HTTP_RESPONSE, response);
                        processor.process(response, response.getEntity(), context);

                        responseSubmitted.set(true);
                        // 返回header
                        conn.sendResponseHeader(response);
                        if (streamListener != null)
                            streamListener.onResponseHead(conn, response);
                        // 返回entity
                        if (MessageSupport.canResponseHaveBody(request.getMethod(), response))
                            conn.sendResponseEntity(response);
                        // 重点：要确保entity关闭，避免后续的连接复用出问题
                        EntityUtils.consume(request.getEntity());
                        // 决定是否连接复用
                        final boolean keepAlive = connReuseStrategy.keepAlive(request, response, context);
                        if (streamListener != null)
                            streamListener.onExchangeComplete(conn, keepAlive);
                        if (!keepAlive)
                            conn.close();
                        conn.flush();
                    } finally {
                        response.close();
                    }
                }
            }, context);

        } catch (final HttpException ex) {
            // 如果协议层已经发送响应了，只能原样抛出，因为已经无法再封装新的异常覆盖到响应里面去了
            if (responseSubmitted.get())
                throw ex;
            // 如果响应还未发布，则封装一个500错误返回
            try (final ClassicHttpResponse errorResponse = new BasicClassicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR)) {
                handleException(ex, errorResponse);
                errorResponse.setHeader(HttpHeaders.CONNECTION, HeaderElements.CLOSE);
                context.setAttribute(HttpCoreContext.HTTP_RESPONSE, errorResponse);
                this.processor.process(errorResponse, errorResponse.getEntity(), context);

                conn.sendResponseHeader(errorResponse);
                if (streamListener != null)
                    streamListener.onResponseHead(conn, errorResponse);
                conn.sendResponseEntity(errorResponse);
                conn.close();
            }
        }
    }

    /**
     * 处理给定的异常，并生成一个HTTP响应发送回客户端，以告知在请求处理过程中遇到的异常情况。
     */
    protected void handleException(final HttpException ex, final ClassicHttpResponse response) {
        response.setCode(toStatusCode(ex));
        response.setEntity(new StringEntity(ServerSupport.toErrorMessage(ex), ContentType.TEXT_PLAIN));
    }

    protected int toStatusCode(final Exception ex) {
        return ServerSupport.toStatusCode(ex);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private HttpProcessor processor;
        private HttpServerRequestHandler requestHandler;
        private ConnectionReuseStrategy connReuseStrategy;
        private Http1StreamListener streamListener;

        private Builder() {}

        public Builder withHttpProcessor(final HttpProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder withHttpServerRequestHandler(final HttpServerRequestHandler requestHandler) {
            this.requestHandler = requestHandler;
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

        public HttpService build() {
            return new HttpService(processor, requestHandler, connReuseStrategy, streamListener);
        }
    }
}
