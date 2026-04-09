package com.github.liyibo1110.hc.core5.http.io.entity;

import com.github.liyibo1110.hc.core5.function.Supplier;
import com.github.liyibo1110.hc.core5.http.ContentType;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.HttpEntity;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.io.IOCallback;
import com.github.liyibo1110.hc.core5.net.WWWFormCodec;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * HttpEntity各种对象的静态工厂组件。
 * @author liyibo
 * @date 2026-04-08 16:34
 */
public final class HttpEntities {

    private HttpEntities() {}

    /**
     * StringEntity
     */
    public static HttpEntity create(final String content, final ContentType contentType) {
        return new StringEntity(content, contentType);
    }

    /**
     * StringEntity
     */
    public static HttpEntity create(final String content, final Charset charset) {
        return new StringEntity(content, ContentType.TEXT_PLAIN.withCharset(charset));
    }

    /**
     * StringEntity
     */
    public static HttpEntity create(final String content) {
        return new StringEntity(content, ContentType.TEXT_PLAIN);
    }

    /**
     * ByteArrayEntity
     */
    public static HttpEntity create(final byte[] content, final ContentType contentType) {
        return new ByteArrayEntity(content, contentType);
    }

    /**
     * FileEntity
     */
    public static HttpEntity create(final File content, final ContentType contentType) {
        return new FileEntity(content, contentType);
    }

    /**
     * SerializableEntity
     */
    public static HttpEntity create(final Serializable serializable, final ContentType contentType) {
        return new SerializableEntity(serializable, contentType);
    }

    public static HttpEntity createUrlEncoded(final Iterable <? extends NameValuePair> parameters, final Charset charset) {
        final ContentType contentType = charset != null ?
                ContentType.APPLICATION_FORM_URLENCODED.withCharset(charset) :
                ContentType.APPLICATION_FORM_URLENCODED;
        return create(WWWFormCodec.format(parameters, contentType.getCharset()), contentType);
    }

    /**
     * EntityTemplate
     */
    public static HttpEntity create(final IOCallback<OutputStream> callback, final ContentType contentType) {
        return new EntityTemplate(-1, contentType, null, callback);
    }

    public static HttpEntity gzip(final HttpEntity entity) {
        return new HttpEntityWrapper(entity) {

            @Override
            public String getContentEncoding() {
                return "gzip";
            }

            @Override
            public long getContentLength() {
                return -1;
            }

            @Override
            public InputStream getContent() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void writeTo(final OutputStream outStream) throws IOException {
                Args.notNull(outStream, "Output stream");
                final GZIPOutputStream gzip = new GZIPOutputStream(outStream);
                super.writeTo(gzip);
                // Only close output stream if the wrapped entity has been
                // successfully written out
                gzip.close();
            }
        };
    }

    public static HttpEntity createGzipped(final String content, final ContentType contentType) {
        return gzip(create(content, contentType));
    }

    public static HttpEntity createGzipped(final String content, final Charset charset) {
        return gzip(create(content, charset));
    }

    public static HttpEntity createGzipped(final String content) {
        return gzip(create(content));
    }

    public static HttpEntity createGzipped(final byte[] content, final ContentType contentType) {
        return gzip(create(content, contentType));
    }

    public static HttpEntity createGzipped(final File content, final ContentType contentType) {
        return gzip(create(content, contentType));
    }

    public static HttpEntity createGzipped(final Serializable serializable, final ContentType contentType) {
        return gzip(create(serializable, contentType));
    }

    public static HttpEntity createGzipped(final IOCallback<OutputStream> callback, final ContentType contentType) {
        return gzip(create(callback, contentType));
    }

    public static HttpEntity createGzipped(final Path content, final ContentType contentType) {
        return gzip(create(content, contentType));
    }

    public static HttpEntity withTrailers(final HttpEntity entity, final Header... trailers) {
        return new HttpEntityWrapper(entity) {

            @Override
            public boolean isChunked() {
                // Must be chunk coded
                return true;
            }

            @Override
            public long getContentLength() {
                return -1;
            }

            @Override
            public Supplier<List<? extends Header>> getTrailers() {
                return () -> Arrays.asList(trailers);
            }

            @Override
            public Set<String> getTrailerNames() {
                final Set<String> names = new LinkedHashSet<>();
                for (final Header trailer: trailers)
                    names.add(trailer.getName());
                return names;
            }
        };
    }

    public static HttpEntity create(final String content, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(content, contentType), trailers);
    }

    public static HttpEntity create(final String content, final Charset charset, final Header... trailers) {
        return withTrailers(create(content, charset), trailers);
    }

    public static HttpEntity create(final String content, final Header... trailers) {
        return withTrailers(create(content), trailers);
    }

    public static HttpEntity create(final byte[] content, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(content, contentType), trailers);
    }

    public static HttpEntity create(final File content, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(content, contentType), trailers);
    }

    public static HttpEntity create(final Serializable serializable, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(serializable, contentType), trailers);
    }

    public static HttpEntity create(final IOCallback<OutputStream> callback, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(callback, contentType), trailers);
    }

    public static HttpEntity create(final Path content, final ContentType contentType) {
        return new PathEntity(content, contentType);
    }

    public static HttpEntity create(final Path content, final ContentType contentType, final Header... trailers) {
        return withTrailers(create(content, contentType), trailers);
    }
}
