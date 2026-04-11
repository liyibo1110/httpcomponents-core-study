package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriter;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriterFactory;
import com.github.liyibo1110.hc.core5.http.message.BasicLineFormatter;
import com.github.liyibo1110.hc.core5.http.message.LineFormatter;

/**
 * @author liyibo
 * @date 2026-04-10 11:43
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseWriterFactory implements HttpMessageWriterFactory<ClassicHttpResponse> {

    public static final DefaultHttpResponseWriterFactory INSTANCE = new DefaultHttpResponseWriterFactory();

    private final LineFormatter lineFormatter;

    public DefaultHttpResponseWriterFactory(final LineFormatter lineFormatter) {
        super();
        this.lineFormatter = lineFormatter != null ? lineFormatter : BasicLineFormatter.INSTANCE;
    }

    public DefaultHttpResponseWriterFactory() {
        this(null);
    }

    @Override
    public HttpMessageWriter<ClassicHttpResponse> create() {
        return new DefaultHttpResponseWriter(this.lineFormatter);
    }
}
