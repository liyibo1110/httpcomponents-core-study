package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.util.Args;

/**
 * @author liyibo
 * @date 2026-04-05 14:26
 */
public class BasicHeaderElement implements HeaderElement {
    private static final NameValuePair[] EMPTY_NAME_VALUE_PAIR_ARRAY = {};

    private final String name;
    private final String value;
    private final NameValuePair[] parameters;

    public BasicHeaderElement(final String name, final String value, final NameValuePair[] parameters) {
        super();
        this.name = Args.notNull(name, "Name");
        this.value = value;
        if (parameters != null)
            this.parameters = parameters;
        else
            this.parameters = EMPTY_NAME_VALUE_PAIR_ARRAY;
    }

    public BasicHeaderElement(final String name, final String value) {
        this(name, value, null);
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
    public NameValuePair[] getParameters() {
        return this.parameters.clone();
    }

    @Override
    public int getParameterCount() {
        return this.parameters.length;
    }

    @Override
    public NameValuePair getParameter(final int index) {
        return this.parameters[index];
    }

    @Override
    public NameValuePair getParameterByName(final String name) {
        Args.notNull(name, "Name");
        NameValuePair found = null;
        for (final NameValuePair current : this.parameters) {
            if (current.getName().equalsIgnoreCase(name)) {
                found = current;
                break;
            }
        }
        return found;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.name);
        if (this.value != null) {
            buffer.append("=");
            buffer.append(this.value);
        }
        for (final NameValuePair parameter : this.parameters) {
            buffer.append("; ");
            buffer.append(parameter);
        }
        return buffer.toString();
    }
}
