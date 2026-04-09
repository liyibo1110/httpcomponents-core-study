package com.github.liyibo1110.hc.core5.http.ssl;

import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.http.ProtocolVersion;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * 支持的TLS协议版本。
 * @author liyibo
 * @date 2026-04-08 13:04
 */
public enum TLS {
    V_1_0("TLSv1",   new ProtocolVersion("TLS", 1, 0)),
    V_1_1("TLSv1.1", new ProtocolVersion("TLS", 1, 1)),
    V_1_2("TLSv1.2", new ProtocolVersion("TLS", 1, 2)),
    V_1_3("TLSv1.3", new ProtocolVersion("TLS", 1, 3));

    public final String id;
    public final ProtocolVersion version;

    TLS(final String id, final ProtocolVersion version) {
        this.id = id;
        this.version = version;
    }

    public boolean isSame(final ProtocolVersion protocolVersion) {
        return version.equals(protocolVersion);
    }

    public boolean isComparable(final ProtocolVersion protocolVersion) {
        return version.isComparable(protocolVersion);
    }

    public String getId() {
        return id;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public boolean greaterEquals(final ProtocolVersion protocolVersion) {
        return version.greaterEquals(protocolVersion);
    }

    public boolean lessEquals(final ProtocolVersion protocolVersion) {
        return version.lessEquals(protocolVersion);
    }

    public static ProtocolVersion parse(final String s) throws ParseException {
        if (s == null)
            return null;
        final Tokenizer.Cursor cursor = new Tokenizer.Cursor(0, s.length());
        return TlsVersionParser.INSTANCE.parse(s, cursor, null);
    }

    public static String[] excludeWeak(final String... protocols) {
        if (protocols == null)
            return null;

        final List<String> enabledProtocols = new ArrayList<>();
        for (final String protocol : protocols) {
            if (isSecure(protocol))
                enabledProtocols.add(protocol);
        }
        if (enabledProtocols.isEmpty())
            enabledProtocols.add(V_1_2.id);

        return enabledProtocols.toArray(new String[0]);
    }

    public static boolean isSecure(final String protocol) {
        return !protocol.startsWith("SSL") && !protocol.equals(V_1_0.id) && !protocol.equals(V_1_1.id);
    }
}
