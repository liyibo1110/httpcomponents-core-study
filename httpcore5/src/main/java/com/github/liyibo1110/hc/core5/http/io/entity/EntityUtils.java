package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.http.EntityDetails;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.io.Closer;
import com.github.liyibo1110.hc.core5.net.WWWFormCodec;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.ByteArrayBuffer;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpEntity对象相关的辅助方法。
 * @author liyibo
 * @date 2026-04-08 16:42
 */
public final class EntityUtils {

    private static final int DEFAULT_ENTITY_RETURN_MAX_LENGTH = Integer.MAX_VALUE;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final int DEFAULT_CHAR_BUFFER_SIZE = 1024;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 4096;

    private EntityUtils() {}

    /**
     * 内部调用consume，并忽略close方法抛出的异常。
     */
    public static void consumeQuietly(final HttpEntity entity) {
        try {
            consume(entity);
        } catch (final IOException ignore) {

        }
    }

    /**
     * 确保entity的内容已被完全读取，然后尝试关闭输入流。
     */
    public static void consume(final HttpEntity entity) throws IOException {
        if (entity == null)
            return;
        if (entity.isStreaming())
            Closer.close(entity.getContent());
    }

    private static int toContentLength(final int contentLength) {
        return contentLength < 0 ? DEFAULT_BYTE_BUFFER_SIZE : contentLength;
    }

    static long checkContentLength(final EntityDetails entityDetails) {
        return Args.checkRange(entityDetails.getContentLength(), -1, Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory)");
    }

    public static byte[] toByteArray(final HttpEntity entity) throws IOException {
        Args.notNull(entity, "HttpEntity");
        final int contentLength = toContentLength((int) checkContentLength(entity));
        try (final InputStream is = entity.getContent()) {
            if (is == null)
                return null;
            final ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
            final byte[] tmp = new byte[DEFAULT_BYTE_BUFFER_SIZE];
            int l;
            while ((l = is.read(tmp)) != -1)
                buffer.append(tmp, 0, l);
            return buffer.toByteArray();
        }
    }

    public static byte[] toByteArray(final HttpEntity entity, final int maxResultLength) throws IOException {
        Args.notNull(entity, "HttpEntity");
        final int contentLength = toContentLength((int) checkContentLength(entity));
        try (final InputStream is = entity.getContent()) {
            if (is == null)
                return null;
            final ByteArrayBuffer buffer = new ByteArrayBuffer(Math.min(maxResultLength, contentLength));
            final byte[] tmp = new byte[DEFAULT_BYTE_BUFFER_SIZE];
            int l;
            while ((l = is.read(tmp)) != -1 && buffer.length() < maxResultLength)
                buffer.append(tmp, 0, l);
            buffer.setLength(Math.min(buffer.length(), maxResultLength));
            return buffer.toByteArray();
        }
    }

    private static CharArrayBuffer toCharArrayBuffer(final InputStream inStream, final int contentLength,
                                                     final Charset charset, final int maxResultLength) throws IOException {
        Args.notNull(inStream, "InputStream");
        Args.positive(maxResultLength, "maxResultLength");
        final Charset actualCharset = charset == null ? DEFAULT_CHARSET : charset;
        final CharArrayBuffer buf = new CharArrayBuffer(Math.min(maxResultLength, contentLength > 0 ? contentLength : DEFAULT_CHAR_BUFFER_SIZE));
        final Reader reader = new InputStreamReader(inStream, actualCharset);
        final char[] tmp = new char[DEFAULT_CHAR_BUFFER_SIZE];
        int chReadCount;
        while ((chReadCount = reader.read(tmp)) != -1 && buf.length() < maxResultLength)
            buf.append(tmp, 0, chReadCount);
        buf.setLength(Math.min(buf.length(), maxResultLength));
        return buf;
    }

    private static final Map<String, ContentType> CONTENT_TYPE_MAP;

    static {
        final ContentType[] contentTypes = {
                ContentType.APPLICATION_ATOM_XML,
                ContentType.APPLICATION_FORM_URLENCODED,
                ContentType.APPLICATION_JSON,
                ContentType.APPLICATION_SVG_XML,
                ContentType.APPLICATION_XHTML_XML,
                ContentType.APPLICATION_XML,
                ContentType.MULTIPART_FORM_DATA,
                ContentType.TEXT_HTML,
                ContentType.TEXT_PLAIN,
                ContentType.TEXT_XML };
        final HashMap<String, ContentType> map = new HashMap<>();
        for (final ContentType contentType: contentTypes)
            map.put(contentType.getMimeType(), contentType);
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private static String toString(final HttpEntity entity, final ContentType contentType, final int maxResultLength)
            throws IOException {
        Args.notNull(entity, "HttpEntity");
        final int contentLength = toContentLength((int) checkContentLength(entity));
        try (final InputStream is = entity.getContent()) {
            if (is == null)
                return null;
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.getCharset();
                if (charset == null) {
                    final ContentType defaultContentType = CONTENT_TYPE_MAP.get(contentType.getMimeType());
                    charset = defaultContentType != null ? defaultContentType.getCharset() : null;
                }
            }
            return toCharArrayBuffer(is, contentLength, charset, maxResultLength).toString();
        }
    }

    public static String toString(final HttpEntity entity, final Charset defaultCharset) throws IOException, ParseException {
        return toString(entity, defaultCharset, DEFAULT_ENTITY_RETURN_MAX_LENGTH);
    }

    public static String toString(final HttpEntity entity, final Charset defaultCharset, final int maxResultLength) throws IOException, ParseException {
        Args.notNull(entity, "HttpEntity");
        ContentType contentType = null;
        try {
            contentType = ContentType.parse(entity.getContentType());
        } catch (final UnsupportedCharsetException ex) {
            if (defaultCharset == null)
                throw new UnsupportedEncodingException(ex.getMessage());
        }
        if (contentType != null) {
            if (contentType.getCharset() == null)
                contentType = contentType.withCharset(defaultCharset);
        } else {
            contentType = ContentType.DEFAULT_TEXT.withCharset(defaultCharset);
        }
        return toString(entity, contentType, maxResultLength);
    }

    public static String toString(final HttpEntity entity, final String defaultCharset) throws IOException, ParseException {
        return toString(entity, defaultCharset, DEFAULT_ENTITY_RETURN_MAX_LENGTH);
    }

    public static String toString(final HttpEntity entity, final String defaultCharset, final int maxResultLength) throws IOException, ParseException {
        return toString(entity, defaultCharset != null ? Charset.forName(defaultCharset) : null, maxResultLength);
    }

    public static String toString(final HttpEntity entity) throws IOException, ParseException {
        return toString(entity, DEFAULT_ENTITY_RETURN_MAX_LENGTH);
    }

    public static String toString(final HttpEntity entity, final int maxResultLength) throws IOException, ParseException {
        Args.notNull(entity, "HttpEntity");
        return toString(entity, ContentType.parse(entity.getContentType()), maxResultLength);
    }

    public static List<NameValuePair> parse(final HttpEntity entity) throws IOException {
        return parse(entity, DEFAULT_ENTITY_RETURN_MAX_LENGTH);
    }

    public static List<NameValuePair> parse(final HttpEntity entity, final int maxStreamLength) throws IOException {
        Args.notNull(entity, "HttpEntity");
        final int contentLength = toContentLength((int) checkContentLength(entity));
        final ContentType contentType = ContentType.parse(entity.getContentType());
        if (!ContentType.APPLICATION_FORM_URLENCODED.isSameMimeType(contentType))
            return Collections.emptyList();
        final Charset charset = contentType.getCharset(DEFAULT_CHARSET);
        final CharArrayBuffer buf;
        try (final InputStream inStream = entity.getContent()) {
            if (inStream == null)
                return Collections.emptyList();
            buf = toCharArrayBuffer(inStream, contentLength, charset, maxStreamLength);
        }
        if (buf.isEmpty())
            return Collections.emptyList();
        return WWWFormCodec.parse(buf, charset);
    }
}
