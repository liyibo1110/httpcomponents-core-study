package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.http.FormattedHeader;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.io.Serializable;

/**
 * 和BasicHeader不一样，BufferedHeader的Header特性是：整行原始文本 + value的起始位置（而不是name和value各用一个字段分别存）。
 * 例如原始header是：Content-Type: text/html; charset=UTF-8，则保存的字段是这样的：
 * buffer："Content-Type: text/html; charset=UTF-8"
 * name: "Content-Type"
 * valuePos：buffer中冒号后面的位置。
 * 当调用了getValue方法时，它才从buffer的valuePos开始截取和trim，最终得到特定的value。
 * 关键点：保留了原始的header line，而不是只保留了name和value这样拆好的结果。
 *
 * 实际价值：
 * 1、按需解析/延迟消费。
 * 2、保留了原始的格式，方便继续做底层解析。
 * 3、减少了中间字符串和重复copy。
 * @author liyibo
 * @date 2026-04-06 13:50
 */
public class BufferedHeader implements FormattedHeader, Serializable {
    private static final long serialVersionUID = -2768352615787625448L;

    private final String name;

    /** 用来保存整个header line */
    private final CharArrayBuffer buffer;

    private final int valuePos;

    public static BufferedHeader create(final CharArrayBuffer buffer) {
        try {
            return new BufferedHeader(buffer);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public BufferedHeader(final CharArrayBuffer buffer) throws ParseException {
        this(buffer, true);
    }

    BufferedHeader(final CharArrayBuffer buffer, final boolean strict) throws ParseException {
        super();
        Args.notNull(buffer, "Char array buffer");
        final int colon = buffer.indexOf(':');
        if (colon <= 0)
            throw new ParseException("Invalid header", buffer, 0, buffer.length());
        if (strict && Tokenizer.isWhitespace(buffer.charAt(colon - 1)))
            throw new ParseException("Invalid header", buffer, 0, buffer.length(), colon - 1);
        final String s = buffer.substringTrimmed(0, colon);
        if (s.isEmpty())
            throw new ParseException("Invalid header", buffer, 0, buffer.length(), colon);

        this.buffer = buffer;
        this.name = s;
        this.valuePos = colon + 1;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.buffer.substringTrimmed(this.valuePos, this.buffer.length());
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public int getValuePos() {
        return this.valuePos;
    }

    @Override
    public CharArrayBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    public String toString() {
        return this.buffer.toString();
    }
}
