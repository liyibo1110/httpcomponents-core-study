package com.github.liyibo1110.hc.core5.http.config;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.util.Args;

import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * HTTP/1.1的字符编码配置相关。
 * @author liyibo
 * @date 2026-04-08 14:45
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class CharCodingConfig {

    public static final CharCodingConfig DEFAULT = new Builder().build();

    private final Charset charset;

    /**
     * CodingErrorAction是JDK自带的组件，是一个用于编码错误处理的类型安全枚举。
     * 该类的实例用于指定：字符集解码器和编码器应如何处理格式错误的输入和无法映射的字符。
     */
    private final CodingErrorAction malformedInputAction;
    private final CodingErrorAction unmappableInputAction;

    CharCodingConfig(final Charset charset, final CodingErrorAction malformedInputAction, final CodingErrorAction unmappableInputAction) {
        super();
        this.charset = charset;
        this.malformedInputAction = malformedInputAction;
        this.unmappableInputAction = unmappableInputAction;
    }

    public Charset getCharset() {
        return charset;
    }

    public CodingErrorAction getMalformedInputAction() {
        return malformedInputAction;
    }

    public CodingErrorAction getUnmappableInputAction() {
        return unmappableInputAction;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[charset=").append(this.charset)
                .append(", malformedInputAction=").append(this.malformedInputAction)
                .append(", unmappableInputAction=").append(this.unmappableInputAction)
                .append("]");
        return builder.toString();
    }

    public static CharCodingConfig.Builder custom() {
        return new Builder();
    }

    public static CharCodingConfig.Builder copy(final CharCodingConfig config) {
        Args.notNull(config, "Config");
        return new Builder()
                .setCharset(config.getCharset())
                .setMalformedInputAction(config.getMalformedInputAction())
                .setUnmappableInputAction(config.getUnmappableInputAction());
    }

    public static class Builder {
        private Charset charset;
        private CodingErrorAction malformedInputAction;
        private CodingErrorAction unmappableInputAction;

        Builder() {}

        public Builder setCharset(final Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder setMalformedInputAction(final CodingErrorAction malformedInputAction) {
            this.malformedInputAction = malformedInputAction;
            if (malformedInputAction != null && this.charset == null)
                this.charset = StandardCharsets.US_ASCII;
            return this;
        }

        public Builder setUnmappableInputAction(final CodingErrorAction unmappableInputAction) {
            this.unmappableInputAction = unmappableInputAction;
            if (unmappableInputAction != null && this.charset == null)
                this.charset = StandardCharsets.US_ASCII;
            return this;
        }

        public CharCodingConfig build() {
            Charset charsetCopy = charset;
            if (charsetCopy == null && (malformedInputAction != null || unmappableInputAction != null))
                charsetCopy = StandardCharsets.US_ASCII;
            return new CharCodingConfig(charsetCopy, malformedInputAction, unmappableInputAction);
        }
    }
}
