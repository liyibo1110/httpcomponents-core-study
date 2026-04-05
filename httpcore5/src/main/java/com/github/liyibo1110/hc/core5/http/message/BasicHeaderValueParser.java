package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.HeaderElement;
import com.github.liyibo1110.hc.core5.http.NameValuePair;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.Tokenizer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * 默认的HeaderValueParser接口实现。
 *
 * value字符串形如：text/html; charset=UTF-8; q=0.9, application/json; q=0.8
 * 沿着字符串一路往右扫：
 * 1、逗号当作element分隔符。
 * 2、分号当作parameter分隔符。
 * 3、等号当作name/value分隔符。
 * @author liyibo
 * @date 2026-04-05 15:36
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicHeaderValueParser implements HeaderValueParser {

    public final static BasicHeaderValueParser INSTANCE = new BasicHeaderValueParser();

    private final static char PARAM_DELIMITER = ';';
    private final static char ELEM_DELIMITER = ',';

    private static final BitSet TOKEN_DELIMITER = Tokenizer.INIT_BITSET('=', PARAM_DELIMITER, ELEM_DELIMITER);
    private static final BitSet VALUE_DELIMITER = Tokenizer.INIT_BITSET(PARAM_DELIMITER, ELEM_DELIMITER);

    private final Tokenizer tokenizer;

    public BasicHeaderValueParser() {
        this.tokenizer = Tokenizer.INSTANCE;
    }

    private static final HeaderElement[] EMPTY_HEADER_ELEMENT_ARRAY = {};

    private static final NameValuePair[] EMPTY_NAME_VALUE_ARRAY = {};

    @Override
    public HeaderElement[] parseElements(final CharSequence buffer, final ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        final List<HeaderElement> elements = new ArrayList<>();
        while (!cursor.atEnd()) {
            final HeaderElement element = parseHeaderElement(buffer, cursor);
            if (!(element.getName().isEmpty() && element.getValue() == null))
                elements.add(element);
        }
        return elements.toArray(EMPTY_HEADER_ELEMENT_ARRAY);
    }

    @Override
    public HeaderElement parseHeaderElement(final CharSequence buffer, final ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        final NameValuePair nvp = parseNameValuePair(buffer, cursor);
        NameValuePair[] params = null;
        if (!cursor.atEnd()) {
            final char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch != ELEM_DELIMITER)
                params = parseParameters(buffer, cursor);
        }
        return new BasicHeaderElement(nvp.getName(), nvp.getValue(), params);
    }

    @Override
    public NameValuePair[] parseParameters(final CharSequence buffer, final ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        tokenizer.skipWhiteSpace(buffer, cursor);
        final List<NameValuePair> params = new ArrayList<>();
        while (!cursor.atEnd()) {
            final NameValuePair param = parseNameValuePair(buffer, cursor);
            params.add(param);
            final char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch == ELEM_DELIMITER)
                break;
        }
        return params.toArray(EMPTY_NAME_VALUE_ARRAY);
    }

    @Override
    public NameValuePair parseNameValuePair(final CharSequence buffer, final ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");

        final String name = tokenizer.parseToken(buffer, cursor, TOKEN_DELIMITER);
        if (cursor.atEnd())
            return new BasicNameValuePair(name, null);
        final int delim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim != '=')
            return new BasicNameValuePair(name, null);
        final String value = tokenizer.parseValue(buffer, cursor, VALUE_DELIMITER);
        if (!cursor.atEnd())
            cursor.updatePos(cursor.getPos() + 1);
        return new BasicNameValuePair(name, value);
    }
}
