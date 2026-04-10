package com.github.liyibo1110.hc.core5.http.impl.io;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseFactory;
import com.github.liyibo1110.hc.core5.http.ReasonPhraseCatalog;
import com.github.liyibo1110.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import com.github.liyibo1110.hc.core5.http.message.BasicClassicHttpResponse;
import com.github.liyibo1110.hc.core5.util.Args;

/**
 * 创建BasicClassicHttpResponse对象的工厂。
 * @author liyibo
 * @date 2026-04-09 15:24
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultClassicHttpResponseFactory implements HttpResponseFactory<ClassicHttpResponse> {

    public static final DefaultClassicHttpResponseFactory INSTANCE = new DefaultClassicHttpResponseFactory();

    private final ReasonPhraseCatalog reasonCatalog;

    public DefaultClassicHttpResponseFactory(final ReasonPhraseCatalog catalog) {
        this.reasonCatalog = Args.notNull(catalog, "Reason phrase catalog");
    }

    public DefaultClassicHttpResponseFactory() {
        this(EnglishReasonPhraseCatalog.INSTANCE);
    }

    @Override
    public ClassicHttpResponse newHttpResponse(final int status, final String reasonPhrase) {
        return new BasicClassicHttpResponse(status, reasonPhrase);
    }

    @Override
    public ClassicHttpResponse newHttpResponse(final int status) {
        return new BasicClassicHttpResponse(status, this.reasonCatalog, null);
    }
}
