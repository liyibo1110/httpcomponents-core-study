package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.http.ClassicHttpRequest;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriter;
import com.github.liyibo1110.hc.core5.http.io.HttpMessageWriterFactory;
import com.github.liyibo1110.hc.core5.http.message.BasicLineFormatter;
import com.github.liyibo1110.hc.core5.http.message.LineFormatter;

/**
 * DefaultHttpRequestWriter对象的工厂。
 * @author liyibo
 * @date 2026-04-10 11:31
 */
public class DefaultHttpRequestWriterFactory implements HttpMessageWriterFactory<ClassicHttpRequest> {

    public static final DefaultHttpRequestWriterFactory INSTANCE = new DefaultHttpRequestWriterFactory();

    private final LineFormatter lineFormatter;

    public DefaultHttpRequestWriterFactory(final LineFormatter lineFormatter) {
        super();
        this.lineFormatter = lineFormatter != null ? lineFormatter : BasicLineFormatter.INSTANCE;
    }

    public DefaultHttpRequestWriterFactory() {
        this(null);
    }

    @Override
    public HttpMessageWriter<ClassicHttpRequest> create() {
        return new DefaultHttpRequestWriter(this.lineFormatter);
    }
}
