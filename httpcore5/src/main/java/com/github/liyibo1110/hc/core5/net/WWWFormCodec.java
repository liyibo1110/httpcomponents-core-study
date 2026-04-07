package com.github.liyibo1110.hc.core5.net;

import com.github.liyibo1110.hc.core5.http.NameValuePair;

import java.nio.charset.Charset;
import java.util.List;

/**
 * application/x-www-form-urlencoded codec
 * @author liyibo
 * @date 2026-04-07 11:13
 */
public class WWWFormCodec {

    private static final char QP_SEP_A = '&';

    public static List<NameValuePair> parse(final CharSequence s, final Charset charset) {
        return URIBuilder.parseQuery(s, charset, true);
    }

    public static void format(final StringBuilder buf, final Iterable<? extends NameValuePair> params, final Charset charset) {
        URIBuilder.formatQuery(buf, params, charset, true);
    }

    public static String format(final Iterable<? extends NameValuePair> params, final Charset charset) {
        final StringBuilder buf = new StringBuilder();
        URIBuilder.formatQuery(buf, params, charset, true);
        return buf.toString();
    }
}
