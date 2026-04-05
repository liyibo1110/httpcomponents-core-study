package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.LangUtils;
import com.github.liyibo1110.hc.core5.util.TextUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-04-05 14:19
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicNameValuePair implements NameValuePair, Serializable {
    private static final long serialVersionUID = -6437800749411518984L;

    private final String name;
    private final String value;

    public BasicNameValuePair(final String name, final String value) {
        super();
        this.name = Args.notNull(name, "Name");
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        if (this.value == null)
            return name;
        final int len = this.name.length() + 1 + this.value.length();
        final StringBuilder buffer = new StringBuilder(len);
        buffer.append(this.name);
        buffer.append("=");
        buffer.append(this.value);
        return buffer.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof BasicNameValuePair that)
            return this.name.equalsIgnoreCase(that.name) && Objects.equals(this.value, that.value);
        return false;
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, TextUtils.toLowerCase(this.name));
        hash = LangUtils.hashCode(hash, this.value);
        return hash;
    }
}
