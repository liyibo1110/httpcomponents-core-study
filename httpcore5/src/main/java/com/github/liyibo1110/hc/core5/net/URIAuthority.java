package com.github.liyibo1110.hc.core5.net;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.LangUtils;
import com.github.liyibo1110.hc.core5.util.TextUtils;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * 表示请求的认证组件java.net.URI。
 * @author liyibo
 * @date 2026-04-07 10:25
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class URIAuthority implements NamedEndpoint, Serializable {
    private static final long serialVersionUID = 1L;

    private final String userInfo;
    private final Host host;

    static URIAuthority parse(final CharSequence s, final Tokenizer.Cursor cursor) throws URISyntaxException {
        final Tokenizer tokenizer = Tokenizer.INSTANCE;
        String userInfo = null;
        final int initPos = cursor.getPos();
        final String token = tokenizer.parseContent(s, cursor, URISupport.HOST_SEPARATORS);
        if (!cursor.atEnd() && s.charAt(cursor.getPos()) == '@') {
            cursor.updatePos(cursor.getPos() + 1);
            if (!TextUtils.isBlank(token)) {
                userInfo = token;
            }
        } else {
            //Rewind
            cursor.updatePos(initPos);
        }
        final Host host = Host.parse(s, cursor);
        return new URIAuthority(userInfo, host);
    }

    static URIAuthority parse(final CharSequence s) throws URISyntaxException {
        final Tokenizer.Cursor cursor = new Tokenizer.Cursor(0, s.length());
        return parse(s, cursor);
    }

    static void format(final StringBuilder buf, final URIAuthority uriAuthority) {
        if (uriAuthority.getUserInfo() != null) {
            buf.append(uriAuthority.getUserInfo());
            buf.append("@");
        }
        Host.format(buf, uriAuthority);
    }

    static String format(final URIAuthority uriAuthority) {
        final StringBuilder buf = new StringBuilder();
        format(buf, uriAuthority);
        return buf.toString();
    }

    public URIAuthority(final String userInfo, final String hostname, final int port) {
        super();
        this.userInfo = userInfo;
        this.host = new Host(hostname, port);
    }

    public URIAuthority(final String hostname, final int port) {
        this(null, hostname, port);
    }

    public URIAuthority(final String userInfo, final Host host) {
        super();
        Args.notNull(host, "Host");
        this.userInfo = userInfo;
        this.host = host;
    }

    public URIAuthority(final Host host) {
        this(null, host);
    }

    public URIAuthority(final String userInfo, final NamedEndpoint endpoint) {
        super();
        Args.notNull(endpoint, "Endpoint");
        this.userInfo = userInfo;
        this.host = new Host(endpoint.getHostName(), endpoint.getPort());
    }

    public URIAuthority(final NamedEndpoint namedEndpoint) {
        this(null, namedEndpoint);
    }

    public static URIAuthority create(final String s) throws URISyntaxException {
        if (TextUtils.isBlank(s))
            return null;
        final Tokenizer.Cursor cursor = new Tokenizer.Cursor(0, s.length());
        final URIAuthority uriAuthority = parse(s, cursor);
        if (!cursor.atEnd())
            throw URISupport.createException(s, cursor, "Unexpected content");
        return uriAuthority;
    }

    public URIAuthority(final String hostname) {
        this(null, hostname, -1);
    }

    public String getUserInfo() {
        return userInfo;
    }

    @Override
    public String getHostName() {
        return host.getHostName();
    }

    @Override
    public int getPort() {
        return host.getPort();
    }

    @Override
    public String toString() {
        return format(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof URIAuthority that)
            return Objects.equals(this.userInfo, that.userInfo) && Objects.equals(this.host, that.host);
        return false;
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, userInfo);
        hash = LangUtils.hashCode(hash, host);
        return hash;
    }
}
