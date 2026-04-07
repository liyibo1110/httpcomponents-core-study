package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.http.ReasonPhraseCatalog;
import com.github.liyibo1110.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.util.Locale;

/**
 * HttpResponse的基础实现类。
 * @author liyibo
 * @date 2026-04-07 13:22
 */
public class BasicHttpResponse extends HeaderGroup implements HttpResponse {
    private static final long serialVersionUID = 1L;

    private final ReasonPhraseCatalog reasonCatalog;

    private ProtocolVersion version;
    private Locale locale;
    private int code;
    private String reasonPhrase;

    public BasicHttpResponse(final int code, final ReasonPhraseCatalog catalog, final Locale locale) {
        super();
        this.code = Args.positive(code, "Status code");
        this.reasonCatalog = catalog != null ? catalog : EnglishReasonPhraseCatalog.INSTANCE;
        this.locale = locale;
    }

    public BasicHttpResponse(final int code, final String reasonPhrase) {
        this.code = Args.positive(code, "Status code");
        this.reasonPhrase = reasonPhrase;
        this.reasonCatalog = EnglishReasonPhraseCatalog.INSTANCE;
    }

    public BasicHttpResponse(final int code) {
        this.code = Args.positive(code, "Status code");
        this.reasonPhrase = null;
        this.reasonCatalog = EnglishReasonPhraseCatalog.INSTANCE;
    }

    @Override
    public void addHeader(final String name, final Object value) {
        Args.notNull(name, "Header name");
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(final String name, final Object value) {
        Args.notNull(name, "Header name");
        setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setVersion(final ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void setCode(final int code) {
        Args.positive(code, "Status code");
        this.code = code;
        this.reasonPhrase = null;
    }

    @Override
    public String getReasonPhrase() {
        return this.reasonPhrase != null ? this.reasonPhrase : getReason(this.code);
    }

    @Override
    public void setReasonPhrase(final String reason) {
        this.reasonPhrase = TextUtils.isBlank(reason) ? null : reason;
    }

    @Override
    public void setLocale(final Locale locale) {
        this.locale = Args.notNull(locale, "Locale");
    }

    protected String getReason(final int code) {
        return this.reasonCatalog != null
                ? this.reasonCatalog.getReason(code, this.locale != null ? this.locale
                : Locale.getDefault()) : null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.code).append(' ').append(this.reasonPhrase).append(' ').append(this.version);
        return sb.toString();
    }
}
