package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.ClassicHttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.ReasonPhraseCatalog;
import com.github.liyibo1110.hc.core5.io.Closer;

import java.io.IOException;
import java.util.Locale;

/**
 * ClassicHttpResponse的基础实现类。
 * @author liyibo
 * @date 2026-04-07 13:36
 */
public class BasicClassicHttpResponse extends BasicHttpResponse implements ClassicHttpResponse {
    private static final long serialVersionUID = 1L;
    private HttpEntity entity;

    public BasicClassicHttpResponse(final int code, final ReasonPhraseCatalog catalog, final Locale locale) {
        super(code, catalog, locale);
    }

    public BasicClassicHttpResponse(final int code, final String reasonPhrase) {
        super(code, reasonPhrase);
    }

    public BasicClassicHttpResponse(final int code) {
        super(code);
    }

    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public void close() throws IOException {
        Closer.close(entity);
    }
}
