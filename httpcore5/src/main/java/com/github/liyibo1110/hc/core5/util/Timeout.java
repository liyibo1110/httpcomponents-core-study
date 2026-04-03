package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 表示一个超时值，该值由一个非负的long时间值和TimeUnit组成。
 * 和Deadline相比，这个Timeout内部存储的是相对时间Duration。
 * @author liyibo
 * @date 2026-04-02 15:05
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class Timeout extends TimeValue {

    public static final Timeout ZERO_MILLISECONDS = Timeout.of(0, TimeUnit.MILLISECONDS);

    public static final Timeout ONE_MILLISECOND = Timeout.of(1, TimeUnit.MILLISECONDS);

    public static final Timeout DISABLED = ZERO_MILLISECONDS;

    public static Timeout defaultsToDisabled(final Timeout timeout) {
        return defaultsTo(timeout, DISABLED);
    }

    /**
     * 和TimeValue实现过程是一样的
     */
    public static Timeout of(final Duration duration) {
        final long seconds = duration.getSeconds();
        final long nanoOfSecond = duration.getNano();
        if (seconds == 0) {
            // no conversion
            return of(nanoOfSecond, TimeUnit.NANOSECONDS);
        } else if (nanoOfSecond == 0) {
            // no conversion
            return of(seconds, TimeUnit.SECONDS);
        }
        // conversion attempts
        try {
            return of(duration.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ArithmeticException e) {
            try {
                return of(duration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (final ArithmeticException e1) {
                // backstop
                return of(seconds, TimeUnit.SECONDS);
            }
        }
    }

    public static Timeout of(final long duration, final TimeUnit timeUnit) {
        return new Timeout(duration, timeUnit);
    }

    public static Timeout ofDays(final long days) {
        return of(days, TimeUnit.DAYS);
    }

    public static Timeout ofHours(final long hours) {
        return of(hours, TimeUnit.HOURS);
    }

    public static Timeout ofMicroseconds(final long microseconds) {
        return of(microseconds, TimeUnit.MICROSECONDS);
    }

    public static Timeout ofMilliseconds(final long milliseconds) {
        return of(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static Timeout ofMinutes(final long minutes) {
        return of(minutes, TimeUnit.MINUTES);
    }

    public static Timeout ofNanoseconds(final long nanoseconds) {
        return of(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static Timeout ofSeconds(final long seconds) {
        return of(seconds, TimeUnit.SECONDS);
    }

    public static Timeout parse(final String value) throws ParseException {
        return TimeValue.parse(value).toTimeout();
    }

    Timeout(final long duration, final TimeUnit timeUnit) {
        super(Args.notNegative(duration, "duration"), Args.notNull(timeUnit, "timeUnit"));
    }

    public boolean isDisabled() {
        return getDuration() == 0;
    }

    public boolean isEnabled() {
        return !isDisabled();
    }
}
