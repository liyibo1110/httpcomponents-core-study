package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * HeaderValueFormatter接口的基础实现。
 * @author liyibo
 * @date 2026-04-07 13:38
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicHeaderValueFormatter implements HeaderValueFormatter {
    public final static BasicHeaderValueFormatter INSTANCE = new BasicHeaderValueFormatter();

    private final static String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    private final static String UNSAFE_CHARS = "\"\\";

    public BasicHeaderValueFormatter() {
        super();
    }

    @Override
    public void formatElements(final CharArrayBuffer buffer, final HeaderElement[] elems, final boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(elems, "Header element array");
        for (int i = 0; i < elems.length; i++) {
            if (i > 0)
                buffer.append(", ");
            formatHeaderElement(buffer, elems[i], quote);
        }
    }

    @Override
    public void formatHeaderElement(final CharArrayBuffer buffer, final HeaderElement elem, final boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(elem, "Header element");

        buffer.append(elem.getName());
        final String value = elem.getValue();
        if (value != null) {
            buffer.append('=');
            formatValue(buffer, value, quote);
        }

        final int c = elem.getParameterCount();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                buffer.append("; ");
                formatNameValuePair(buffer, elem.getParameter(i), quote);
            }
        }
    }

    @Override
    public void formatParameters(final CharArrayBuffer buffer, final NameValuePair[] nvps, final boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(nvps, "Header parameter array");
        for (int i = 0; i < nvps.length; i++) {
            if (i > 0)
                buffer.append("; ");
            formatNameValuePair(buffer, nvps[i], quote);
        }
    }

    @Override
    public void formatNameValuePair(final CharArrayBuffer buffer, final NameValuePair nvp, final boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(nvp, "Name / value pair");
        buffer.append(nvp.getName());
        final String value = nvp.getValue();
        if (value != null) {
            buffer.append('=');
            formatValue(buffer, value, quote);
        }
    }

    void formatValue(final CharArrayBuffer buffer, final String value, final boolean quote) {
        boolean quoteFlag = quote;
        if (!quoteFlag) {
            for (int i = 0; (i < value.length()) && !quoteFlag; i++)
                quoteFlag = isSeparator(value.charAt(i));
        }

        if (quoteFlag) {
            buffer.append('"');
        }
        for (int i = 0; i < value.length(); i++) {
            final char ch = value.charAt(i);
            if (isUnsafe(ch))
                buffer.append('\\');
            buffer.append(ch);
        }
        if (quoteFlag)
            buffer.append('"');
    }

    boolean isSeparator(final char ch) {
        return SEPARATORS.indexOf(ch) >= 0;
    }

    boolean isUnsafe(final char ch) {
        return UNSAFE_CHARS.indexOf(ch) >= 0;
    }
}
