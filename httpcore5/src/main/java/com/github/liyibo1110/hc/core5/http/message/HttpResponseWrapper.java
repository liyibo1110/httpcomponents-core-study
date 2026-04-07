package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HttpResponse;

import java.util.Locale;

/**
 * HttpResponse的wrapper组件。
 * @author liyibo
 * @date 2026-04-07 11:20
 */
public class HttpResponseWrapper extends AbstractMessageWrapper<HttpResponse> implements HttpResponse {

    public HttpResponseWrapper(final HttpResponse message) {
        super(message);
    }

    @Override
    public int getCode() {
        return getMessage().getCode();
    }

    @Override
    public void setCode(final int code) {
        getMessage().setCode(code);
    }

    @Override
    public String getReasonPhrase() {
        return getMessage().getReasonPhrase();
    }

    @Override
    public void setReasonPhrase(final String reason) {
        getMessage().setReasonPhrase(reason);
    }

    @Override
    public Locale getLocale() {
        return getMessage().getLocale();
    }

    @Override
    public void setLocale(final Locale loc) {
        getMessage().setLocale(loc);
    }
}
