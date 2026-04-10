package com.github.liyibo1110.hc.core5.http.impl;

import com.github.liyibo1110.hc.core5.http.config.CharCodingConfig;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * CharCoding相关辅助方法。
 * @author liyibo
 * @date 2026-04-09 13:12
 */
public final class CharCodingSupport {

    private CharCodingSupport() {}

    /**
     * 根据config生成java.nio.charset.CharsetEncoder对象。
     */
    public static CharsetEncoder createEncoder(final CharCodingConfig cconfig) {
        if (cconfig == null)
            return null;
        final Charset charset = cconfig.getCharset();
        if(charset != null) {
            final CodingErrorAction malformed = cconfig.getMalformedInputAction();
            final CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
            return charset.newEncoder()
                    .onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT)
                    .onUnmappableCharacter(unmappable != null ? unmappable: CodingErrorAction.REPORT);
        }
        return null;
    }

    /**
     * 根据config生成java.nio.charset.CharsetDecoder对象。
     */
    public static CharsetDecoder createDecoder(final CharCodingConfig cconfig) {
        if (cconfig == null)
            return null;
        final Charset charset = cconfig.getCharset();
        final CodingErrorAction malformed = cconfig.getMalformedInputAction();
        final CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
        if (charset != null) {
            return charset.newDecoder()
                    .onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT)
                    .onUnmappableCharacter(unmappable != null ? unmappable: CodingErrorAction.REPORT);
        }
        return null;
    }
}
