package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 由MIME类型和可选字符集组成的内容类型信息。
 *
 * 该类不会尝试验证MIME类型的有效性，但create(String, String)方法的输入参数中不得包含HTTP规范中保留的字符<">、<;>、<、>。
 * @author liyibo
 * @date 2026-04-03 15:16
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class ContentType implements Serializable {
    private static final long serialVersionUID = -7768694718232371896L;

    private static final String CHARSET = "charset";

    // constants

    public static final ContentType APPLICATION_ATOM_XML = create("application/atom+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_FORM_URLENCODED = create("application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create("application/json", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_NDJSON = create("application/x-ndjson", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = create("application/octet-stream", (Charset) null);
    public static final ContentType APPLICATION_PDF = create("application/pdf", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_SOAP_XML = create("application/soap+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_SVG_XML = create("application/svg+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_XHTML_XML = create("application/xhtml+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_XML = create("application/xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_PROBLEM_JSON = create("application/problem+json", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_PROBLEM_XML = create("application/problem+xml", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_RSS_XML = create("application/rss+xml", StandardCharsets.UTF_8);

    public static final ContentType IMAGE_BMP = create("image/bmp");
    public static final ContentType IMAGE_GIF = create("image/gif");
    public static final ContentType IMAGE_JPEG = create("image/jpeg");
    public static final ContentType IMAGE_PNG = create("image/png");
    public static final ContentType IMAGE_SVG = create("image/svg+xml");
    public static final ContentType IMAGE_TIFF = create("image/tiff");
    public static final ContentType IMAGE_WEBP = create("image/webp");

    public static final ContentType MULTIPART_FORM_DATA = create("multipart/form-data", StandardCharsets.ISO_8859_1);
    public static final ContentType MULTIPART_MIXED = create("multipart/mixed", StandardCharsets.ISO_8859_1);
    public static final ContentType MULTIPART_RELATED = create("multipart/related", StandardCharsets.ISO_8859_1);
    public static final ContentType TEXT_HTML = create("text/html", StandardCharsets.ISO_8859_1);

    public static final ContentType TEXT_MARKDOWN = create("text/markdown", StandardCharsets.UTF_8);

    public static final ContentType TEXT_PLAIN = create("text/plain", StandardCharsets.ISO_8859_1);
    public static final ContentType TEXT_XML = create("text/xml", StandardCharsets.UTF_8);

    public static final ContentType TEXT_EVENT_STREAM = create("text/event-stream", StandardCharsets.UTF_8);
    public static final ContentType WILDCARD = create("*/*", (Charset) null);

    private static final NameValuePair[] EMPTY_NAME_VALUE_PAIR_ARRAY = {};

    @Deprecated
    private static final Map<String, ContentType> CONTENT_TYPE_MAP;

    static {
        final ContentType[] contentTypes = {
                APPLICATION_ATOM_XML,
                APPLICATION_FORM_URLENCODED,
                APPLICATION_JSON,
                APPLICATION_SVG_XML,
                APPLICATION_XHTML_XML,
                APPLICATION_XML,
                IMAGE_BMP,
                IMAGE_GIF,
                IMAGE_JPEG,
                IMAGE_PNG,
                IMAGE_SVG,
                IMAGE_TIFF,
                IMAGE_WEBP,
                MULTIPART_FORM_DATA,
                TEXT_HTML,
                TEXT_PLAIN,
                TEXT_XML };
        final HashMap<String, ContentType> map = new HashMap<>();
        for (final ContentType contentType: contentTypes)
            map.put(contentType.getMimeType(), contentType);
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    private final String mimeType;
    private final Charset charset;
    private final NameValuePair[] params;

    ContentType(final String mimeType, final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = null;
    }

    ContentType(final String mimeType, final Charset charset, final NameValuePair[] params) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = params;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public Charset getCharset(final Charset defaultCharset) {
        return this.charset != null ? this.charset : defaultCharset;
    }

    public String getParameter(final String name) {
        Args.notEmpty(name, "Parameter name");
        if (this.params == null)
            return null;
        for (final NameValuePair param : this.params) {
            if(param.getName().equalsIgnoreCase(name))
                return param.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        final CharArrayBuffer buf = new CharArrayBuffer(64);
        buf.append(this.mimeType);
        if (this.params != null) {
            buf.append("; ");
            BasicHeaderValueFormatter.INSTANCE.formatParameters(buf, this.params, false);
        } else if (this.charset != null) {
            buf.append("; charset=");
            buf.append(this.charset.name());
        }
        return buf.toString();
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';')
                return false;
        }
        return true;
    }

    public static ContentType create(final String mimeType, final Charset charset) {
        final String normalizedMimeType = TextUtils.toLowerCase(Args.notBlank(mimeType, "MIME type"));
        Args.check(valid(normalizedMimeType), "MIME type may not contain reserved characters");
        return new ContentType(normalizedMimeType, charset);
    }

    public static ContentType create(final String mimeType) {
        return create(mimeType, (Charset) null);
    }

    public static ContentType create(final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !TextUtils.isBlank(charset) ? Charset.forName(charset) : null);
    }

    private static ContentType create(final HeaderElement helem, final boolean strict) {
        final String mimeType = helem.getName();
        if (TextUtils.isBlank(mimeType))
            return null;
        return create(helem.getName(), helem.getParameters(), strict);
    }

    private static ContentType create(final String mimeType, final NameValuePair[] params, final boolean strict) {
        Charset charset = null;
        if (params != null) {
            for (final NameValuePair param : params) {
                if (param.getName().equalsIgnoreCase(CHARSET)) {
                    final String s = param.getValue();
                    if (!TextUtils.isBlank(s)) {
                        try {
                            charset = Charset.forName(s);
                        } catch (final UnsupportedCharsetException ex) {
                            if (strict)
                                throw ex;
                        }
                    }
                    break;
                }
            }
        }
        return new ContentType(mimeType, charset, params != null && params.length > 0 ? params : null);
    }

    public static ContentType create(final String mimeType, final NameValuePair... params) throws UnsupportedCharsetException {
        final String type = TextUtils.toLowerCase(Args.notBlank(mimeType, "MIME type"));
        Args.check(valid(type), "MIME type may not contain reserved characters");
        return create(mimeType, params != null ? params.clone() : null, true);
    }

    public static ContentType parse(final CharSequence s) throws UnsupportedCharsetException {
        return parse(s, true);
    }

    public static ContentType parseLenient(final CharSequence s) throws UnsupportedCharsetException {
        return parse(s, false);
    }

    private static ContentType parse(final CharSequence s, final boolean strict) throws UnsupportedCharsetException {
        if (TextUtils.isBlank(s))
            return null;
        final ParserCursor cursor = new ParserCursor(0, s.length());
        final HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(s, cursor);
        if (elements.length > 0)
            return create(elements[0], strict);
        return null;
    }

    @Deprecated
    public static ContentType getByMimeType(final String mimeType) {
        if (mimeType == null)
            return null;
        return CONTENT_TYPE_MAP.get(mimeType);
    }

    public static Charset getCharset(final ContentType contentType, final Charset defaultCharset) {
        return contentType != null ? contentType.getCharset(defaultCharset) : defaultCharset;
    }

    public ContentType withCharset(final Charset charset) {
        return create(this.getMimeType(), charset);
    }

    public ContentType withCharset(final String charset) {
        return create(this.getMimeType(), charset);
    }

    public ContentType withParameters(final NameValuePair... params) throws UnsupportedCharsetException {
        if (params.length == 0)
            return this;
        final Map<String, String> paramMap = new LinkedHashMap<>();
        if (this.params != null) {
            for (final NameValuePair param: this.params)
                paramMap.put(param.getName(), param.getValue());
        }
        for (final NameValuePair param: params)
            paramMap.put(param.getName(), param.getValue());
        final List<NameValuePair> newParams = new ArrayList<>(paramMap.size() + 1);
        if (this.charset != null && !paramMap.containsKey(CHARSET))
            newParams.add(new BasicNameValuePair(CHARSET, this.charset.name()));
        for (final Map.Entry<String, String> entry: paramMap.entrySet())
            newParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        return create(this.getMimeType(), newParams.toArray(EMPTY_NAME_VALUE_PAIR_ARRAY), true);
    }

    public boolean isSameMimeType(final ContentType contentType) {
        return contentType != null && mimeType.equalsIgnoreCase(contentType.getMimeType());
    }
}
