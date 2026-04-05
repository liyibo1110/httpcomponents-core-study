package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.util.Args;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-04-05 14:28
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicHeader implements Header, Cloneable, Serializable {
    private static final long serialVersionUID = -5427236326487562174L;

    private final String name;
    private final boolean sensitive;
    private final String value;

    public BasicHeader(final String name, final Object value) {
        this(name, value, false);
    }

    public BasicHeader(final String name, final Object value, final boolean sensitive) {
        super();
        this.name = Args.notNull(name, "Name");
        this.value = Objects.toString(value, null);
        this.sensitive = sensitive;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isSensitive() {
        return this.sensitive;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(this.getName()).append(": ");
        if (this.getValue() != null)
            buf.append(this.getValue());
        return buf.toString();
    }

    @Override
    public BasicHeader clone() throws CloneNotSupportedException {
        return (BasicHeader) super.clone();
    }
}
