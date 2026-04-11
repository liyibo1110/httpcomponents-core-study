package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.ContentLengthStrategy;
import com.github.liyibo1110.hc.core5.http.HeaderElements;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpHeaders;
import com.github.liyibo1110.hc.core5.http.HttpStatus;
import com.github.liyibo1110.hc.core5.http.HttpVersion;
import com.github.liyibo1110.hc.core5.http.LengthRequiredException;
import com.github.liyibo1110.hc.core5.http.NoHttpResponseException;
import com.github.liyibo1110.hc.core5.http.ProtocolException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.UnsupportedHttpVersionException;
import com.github.liyibo1110.hc.core5.http.config.Http1Config;
import com.github.liyibo1110.hc.core5.http.impl.DefaultContentLengthStrategy;
import com.github.liyibo1110.hc.core5.http.io.HttpClientConnection;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParser;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageParserFactory;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriter;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriterFactory;
import com.github.liyibo1110.hc.core5.http.io.ResponseOutOfOrderStrategy;
import com.github.liyibo1110.hc.core5.http.message.BasicTokenIterator;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

/**
 * HttpClientConnection接口的默认实现，大部分共用功能封装到了BHttpConnectionBase。
 * 1、写request head和body。
 * 2、读response head和body。
 * @author liyibo
 * @date 2026-04-10 11:50
 */
public class DefaultBHttpClientConnection extends BHttpConnectionBase implements HttpClientConnection {

    /** 负责把server返回的原始字节流，解析成ClassicHttpResponse的响应头对象，注意只解析head。 */
    private final HttpMessageParser<ClassicHttpResponse> responseParser;

    /** 负责把ClassicHttpRequest的head，写到输出流中。 */
    private final HttpMessageWriter<ClassicHttpRequest> requestWriter;

    /** 决定收到的response body应该按哪种边界规则读取，例如content-length、chunked、identity、no body。 */
    private final ContentLengthStrategy incomingContentStrategy;

    /** 决定request body应该按哪种边界规则写出去。 */
    private final ContentLengthStrategy outgoingContentStrategy;

    /** 决定在client发送request body过程中，如何探测server是否已经提前返回响应了。 */
    private final ResponseOutOfOrderStrategy responseOutOfOrderStrategy;

    /** 表示这个client连接在request/response交换之后，协议状态是否仍然一致，还适不适合继续复用（即连接还是否可靠）。 */
    private volatile boolean consistent;

    public DefaultBHttpClientConnection(final Http1Config http1Config,
                                        final CharsetDecoder charDecoder,
                                        final CharsetEncoder charEncoder,
                                        final ContentLengthStrategy incomingContentStrategy,
                                        final ContentLengthStrategy outgoingContentStrategy,
                                        final ResponseOutOfOrderStrategy responseOutOfOrderStrategy,
                                        final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory,
                                        final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        super(http1Config, charDecoder, charEncoder);
        this.requestWriter = (requestWriterFactory != null ? requestWriterFactory : DefaultHttpRequestWriterFactory.INSTANCE).create();
        this.responseParser = (responseParserFactory != null ? responseParserFactory : DefaultHttpResponseParserFactory.INSTANCE).create(http1Config);
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.responseOutOfOrderStrategy = responseOutOfOrderStrategy != null ? responseOutOfOrderStrategy : NoResponseOutOfOrderStrategy.INSTANCE;
        this.consistent = true;
    }

    public DefaultBHttpClientConnection(final Http1Config http1Config,
                                        final CharsetDecoder charDecoder,
                                        final CharsetEncoder charEncoder,
                                        final ContentLengthStrategy incomingContentStrategy,
                                        final ContentLengthStrategy outgoingContentStrategy,
                                        final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory,
                                        final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this(http1Config, charDecoder, charEncoder, incomingContentStrategy, outgoingContentStrategy,
             null, requestWriterFactory, responseParserFactory);
    }

    public DefaultBHttpClientConnection(final Http1Config http1Config,
                                        final CharsetDecoder charDecoder,
                                        final CharsetEncoder charEncoder) {
        this(http1Config, charDecoder, charEncoder, null, null, null, null);
    }

    public DefaultBHttpClientConnection(final Http1Config http1Config) {
        this(http1Config, null, null);
    }

    /**
     * 子类可以实现的钩子方法
     */
    protected void onResponseReceived(final ClassicHttpResponse response) {}

    /**
     * 子类可以实现的钩子方法
     */
    protected void onRequestSubmitted(final ClassicHttpRequest request) {}

    @Override
    public void bind(final Socket socket) throws IOException {
        super.bind(socket);
    }

    @Override
    public void sendRequestHeader(final ClassicHttpRequest request) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        final SocketHolder socketHolder = ensureOpen();
        this.requestWriter.write(request, this.outbuffer, socketHolder.getOutputStream());
        onRequestSubmitted(request);
        incrementRequestCount();
    }

    /**
     * 将request的body按匹配的传输策略写出去，并在写的过程中支持提前响应探测的附加功能。
     */
    @Override
    public void sendRequestEntity(final ClassicHttpRequest request) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        final SocketHolder socketHolder = ensureOpen();
        final HttpEntity entity = request.getEntity();
        if (entity == null)
            return;

        // 计算content-length
        final long len = this.outgoingContentStrategy.determineLength(request);
        if (len == ContentLengthStrategy.UNDEFINED)
            throw new LengthRequiredException();

        // 创建body的输出流，会用len选择不同的OutputStream子类实现
        try (final OutputStream outStream = createContentOutputStream(len, this.outbuffer, new OutputStream() {
                    final OutputStream socketOutputStream = socketHolder.getOutputStream();
                    final InputStream socketInputStream = socketHolder.getInputStream();
                    long totalBytes;

                    /**
                     * 之所以要匿名OutputStream实现，就是为了要有这个方法。
                     * 不断的探测：server是否已经提前响应结果了。
                     */
                    void checkForEarlyResponse(final long totalBytesSent, final int nextWriteSize) throws IOException {
                        if (responseOutOfOrderStrategy.isEarlyResponseDetected(
                                request,
                                DefaultBHttpClientConnection.this,
                                socketInputStream,
                                totalBytesSent,
                                nextWriteSize)) {
                            throw new ResponseOutOfOrderException();
                        }
                    }

                    @Override
                    public void write(final byte[] b) throws IOException {
                        checkForEarlyResponse(totalBytes, b.length);
                        totalBytes += b.length;
                        socketOutputStream.write(b);
                    }

                    @Override
                    public void write(final byte[] b, final int off, final int len) throws IOException {
                        checkForEarlyResponse(totalBytes, len);
                        totalBytes += len;
                        socketOutputStream.write(b, off, len);
                    }

                    @Override
                    public void write(final int b) throws IOException {
                        checkForEarlyResponse(totalBytes, 1);
                        totalBytes++;
                        socketOutputStream.write(b);
                    }

                    @Override
                    public void flush() throws IOException {
                        socketOutputStream.flush();
                    }

                    @Override
                    public void close() throws IOException {
                        socketOutputStream.close();
                    }

                }, entity.getTrailers())) {
            entity.writeTo(outStream);  // 真正地写entity
        } catch (final ResponseOutOfOrderException ex) {
            // 说明真发生了提前响应
            if (len > 0)
                this.consistent = false;
        }
    }

    @Override
    public boolean isConsistent() {
        return this.consistent;
    }

    /**
     * 在某些场景下，尝试把一个请求以尽量可控的方式来收尾或终止掉。
     * 表达一种保守的策略：低风险就正常收尾，否则就把连接判成不一致。
     */
    @Override
    public void terminateRequest(final ClassicHttpRequest request) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        final SocketHolder socketHolder = ensureOpen();
        final HttpEntity entity = request.getEntity();
        if (entity == null)
            return;

        // 检查request头，有没有Connection: close
        final Iterator<String> ti = new BasicTokenIterator(request.headerIterator(HttpHeaders.CONNECTION));
        while (ti.hasNext()) {
            final String token = ti.next();
            if (HeaderElements.CLOSE.equalsIgnoreCase(token)) {
                this.consistent = false;
                return;
            }
        }

        // 重新判断content-length策略
        final long len = this.outgoingContentStrategy.determineLength(request);
        if (len == ContentLengthStrategy.CHUNKED) {
            // 通过关闭chunked流，把终止（0-chunk/trailers结束标记）写出去，即：正常结束chunked编码
            try (final OutputStream outStream = createContentOutputStream(len, this.outbuffer, socketHolder.getOutputStream(), entity.getTrailers())) {
                // just close
            }
        } else if (len >= 0 && len <= 1024) {
            // 说明body不算大，直接让它全部写完，从而保持连接状态一致
            try (final OutputStream outStream = createContentOutputStream(len, this.outbuffer, socketHolder.getOutputStream(), null)) {
                entity.writeTo(outStream);
            }
        } else {
            // 对应其它情况：不是chunked，又不是很小的固定长度的body，不再冒险做复杂补齐，直接认为连接状态不再可靠
            this.consistent = false;
        }
    }

    @Override
    public ClassicHttpResponse receiveResponseHeader() throws HttpException, IOException {
        final SocketHolder socketHolder = ensureOpen();
        // 读取输入缓冲，解析出response head部分
        final ClassicHttpResponse response = this.responseParser.parse(this.inBuffer, socketHolder.getInputStream());
        if (response == null)
            throw new NoHttpResponseException("The target server failed to respond");

        // 检查返回的协议版本
        final ProtocolVersion transportVersion = response.getVersion();
        if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2))
            throw new UnsupportedHttpVersionException(transportVersion);
        this.version = transportVersion;

        onResponseReceived(response);

        // 校验状态码合法性
        final int status = response.getCode();
        if (status < HttpStatus.SC_INFORMATIONAL)
            throw new ProtocolException("Invalid response: " + status);
        if (response.getCode() >= HttpStatus.SC_SUCCESS)    // 200以上的响应才算真正要计数的响应
            incrementResponseCount();
        return response;
    }

    @Override
    public void receiveResponseEntity( final ClassicHttpResponse response) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        final SocketHolder socketHolder = ensureOpen();
        // 判断response body的长度
        final long len = this.incomingContentStrategy.determineLength(response);
        // 调用父类的方法，最终生成IncomingHttpEntity，挂到response对象上
        response.setEntity(createIncomingEntity(response, this.inBuffer, socketHolder.getInputStream(), len));
    }
}
