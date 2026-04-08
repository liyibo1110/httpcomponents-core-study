package com.github.liyibo1110.hc.core5.http.protocol;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.TimeZone;

/**
 * 生成符合HTTP协议要求的日期格式。
 * @author liyibo
 * @date 2026-04-07 15:55
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class HttpDateGenerator {

    private static final int GRANULARITY_MILLIS = 1000;

    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Deprecated
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static final ZoneId GMT_ID = ZoneId.of("GMT");

    public static final HttpDateGenerator INSTANCE = new HttpDateGenerator(PATTERN_RFC1123, GMT_ID);

    private final DateTimeFormatter dateTimeFormatter;
    private long dateAsMillis;
    private String dateAsText;
    private ZoneId zoneId;

    HttpDateGenerator() {
        dateTimeFormatter =new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern(PATTERN_RFC1123)
                .toFormatter();
        zoneId = GMT_ID;
    }

    private HttpDateGenerator(final String pattern, final ZoneId zoneId) {
        dateTimeFormatter = new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter();
        this.zoneId = zoneId;
    }

    public synchronized String getCurrentDate() {
        final long now = System.currentTimeMillis();
        if (now - this.dateAsMillis > GRANULARITY_MILLIS) {
            // Generate new date string
            dateAsText = dateTimeFormatter.format(Instant.now().atZone(zoneId));
            dateAsMillis = now;
        }
        return dateAsText;
    }
}
