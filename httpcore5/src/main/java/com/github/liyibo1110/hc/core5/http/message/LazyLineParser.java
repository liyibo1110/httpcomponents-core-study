package com.github.liyibo1110.hc.core5.http.message;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;
import com.github.liyibo1110.hc.core5.http.Header;
import com.github.liyibo1110.hc.core5.http.ParseException;
import com.github.liyibo1110.hc.core5.util.Args;
import com.github.liyibo1110.hc.core5.util.CharArrayBuffer;

/**
 * @author liyibo
 * @date 2026-04-06 13:50
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class LazyLineParser extends BasicLineParser {

    public final static LazyLineParser INSTANCE = new LazyLineParser();

    @Override
    public Header parseHeader(final CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        return new BufferedHeader(buffer, true);
    }
}
