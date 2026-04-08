package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpException;
import com.github.liyibo1110.hc.core5.http.HttpRequest;
import com.github.liyibo1110.hc.core5.http.HttpRequestInterceptor;
import com.github.liyibo1110.hc.core5.http.HttpResponse;
import com.github.liyibo1110.hc.core5.http.HttpResponseInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * HttpProcessor接口的默认实现类。
 * @author liyibo
 * @date 2026-04-07 16:23
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public final class DefaultHttpProcessor implements HttpProcessor {

    private final HttpRequestInterceptor[] requestInterceptors;
    private final HttpResponseInterceptor[] responseInterceptors;

    public DefaultHttpProcessor(final HttpRequestInterceptor[] requestInterceptors,
                                final HttpResponseInterceptor[] responseInterceptors) {
        super();
        if (requestInterceptors != null) {
            final int l = requestInterceptors.length;
            this.requestInterceptors = new HttpRequestInterceptor[l];
            System.arraycopy(requestInterceptors, 0, this.requestInterceptors, 0, l);
        } else {
            this.requestInterceptors = new HttpRequestInterceptor[0];
        }

        if (responseInterceptors != null) {
            final int l = responseInterceptors.length;
            this.responseInterceptors = new HttpResponseInterceptor[l];
            System.arraycopy(responseInterceptors, 0, this.responseInterceptors, 0, l);
        } else {
            this.responseInterceptors = new HttpResponseInterceptor[0];
        }
    }

    public DefaultHttpProcessor(final List<HttpRequestInterceptor> requestInterceptors,
                                final List<HttpResponseInterceptor> responseInterceptors) {
        super();
        if (requestInterceptors != null) {
            final int l = requestInterceptors.size();
            this.requestInterceptors = requestInterceptors.toArray(new HttpRequestInterceptor[l]);
        } else {
            this.requestInterceptors = new HttpRequestInterceptor[0];
        }

        if (responseInterceptors != null) {
            final int l = responseInterceptors.size();
            this.responseInterceptors = responseInterceptors.toArray(new HttpResponseInterceptor[l]);
        } else {
            this.responseInterceptors = new HttpResponseInterceptor[0];
        }
    }

    public DefaultHttpProcessor(final HttpRequestInterceptor... requestInterceptors) {
        this(requestInterceptors, null);
    }

    public DefaultHttpProcessor(final HttpResponseInterceptor... responseInterceptors) {
        this(null, responseInterceptors);
    }

    @Override
    public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
            throws IOException, HttpException {
        for (final HttpRequestInterceptor requestInterceptor : this.requestInterceptors)
            requestInterceptor.process(request, entity, context);
    }

    @Override
    public void process(final HttpResponse response, final EntityDetails entity, final HttpContext context)
            throws IOException, HttpException {
        for (final HttpResponseInterceptor responseInterceptor : this.responseInterceptors)
            responseInterceptor.process(response, entity, context);
    }
}
