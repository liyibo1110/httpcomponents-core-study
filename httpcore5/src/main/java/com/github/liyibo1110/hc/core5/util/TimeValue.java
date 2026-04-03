package com.github.liyibo1110.hc.core5.util;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 表示一个时间值，由一个long时间值和一个TimeUnit组成。
 * @author liyibo
 * @date 2026-04-02 14:35
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class TimeValue implements Comparable<TimeValue> {

    static final int INT_UNDEFINED = -1;

    /** 最大天数 */
    public static final TimeValue MAX_VALUE = ofDays(Long.MAX_VALUE);

    public static final TimeValue NEG_ONE_MILLISECOND = TimeValue.of(INT_UNDEFINED, TimeUnit.MILLISECONDS);

    public static final TimeValue NEG_ONE_SECOND = TimeValue.of(INT_UNDEFINED, TimeUnit.SECONDS);

    public static final TimeValue ZERO_MILLISECONDS = TimeValue.of(0, TimeUnit.MILLISECONDS);

    /**
     * 将给定的long值转换为int类型，
     * 若long值的范围超出int类型范围，则分别返回Integer.MIN_VALUE和Integer.MAX_VALUE。
     */
    public static int asBoundInt(final long value) {
        if (value > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        else if (value < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return (int) value;
    }

    /**
     * 给定的timeValue如果为null，则返回给定的defaultValue，否则原样返回timeValue。
     */
    public static <T extends TimeValue> T defaultsTo(final T timeValue, final T defaultValue) {
        return timeValue != null ? timeValue : defaultValue;
    }

    public static TimeValue defaultsToNegativeOneMillisecond(final TimeValue timeValue) {
        return defaultsTo(timeValue, NEG_ONE_MILLISECOND);
    }

    public static TimeValue defaultsToNegativeOneSecond(final TimeValue timeValue) {
        return defaultsTo(timeValue, NEG_ONE_SECOND);
    }

    public static TimeValue defaultsToZeroMilliseconds(final TimeValue timeValue) {
        return defaultsTo(timeValue, ZERO_MILLISECONDS);
    }

    public static boolean isNonNegative(final TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() >= 0;
    }

    public static boolean isPositive(final TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() > 0;
    }

    public static TimeValue of(final long duration, final TimeUnit timeUnit) {
        return new TimeValue(duration, timeUnit);
    }

    /**
     * 根据给定的Duration（秒和纳秒），生成对应的TimeValue
     */
    public static TimeValue of(final Duration duration) {
        final long seconds = duration.getSeconds();
        final long nanoOfSecond = duration.getNano();
        if (seconds == 0)
            return of(nanoOfSecond, TimeUnit.NANOSECONDS);
        else if(nanoOfSecond == 0)
            return of(seconds, TimeUnit.SECONDS);

        // 下面会尝试转换纳秒值
        try {
            return of(duration.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final ArithmeticException e) {
            // 转换纳秒失败，再尝试转成毫秒
            try {
                return of(duration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (final ArithmeticException e1) {
                // 再失败就转成秒
                return of(seconds, TimeUnit.SECONDS);
            }
        }
    }

    public static TimeValue ofDays(final long days) {
        return of(days, TimeUnit.DAYS);
    }

    public static TimeValue ofHours(final long hours) {
        return of(hours, TimeUnit.HOURS);
    }

    public static TimeValue ofMicroseconds(final long microseconds) {
        return of(microseconds, TimeUnit.MICROSECONDS);
    }

    public static TimeValue ofMilliseconds(final long millis) {
        return of(millis, TimeUnit.MILLISECONDS);
    }

    public static TimeValue ofMinutes(final long minutes) {
        return of(minutes, TimeUnit.MINUTES);
    }

    public static TimeValue ofNanoseconds(final long nanoseconds) {
        return of(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static TimeValue ofSeconds(final long seconds) {
        return of(seconds, TimeUnit.SECONDS);
    }

    /**
     * TimeUnit -> ChronoUnit
     */
    static ChronoUnit toChronoUnit(final TimeUnit timeUnit) {
        switch (Objects.requireNonNull(timeUnit)) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalArgumentException(timeUnit.toString());
        }
    }

    /**
     * 解析格式为 <Long><空格><时间单位> 的 TimeValue，例如 “1200 MILLISECONDS”。
     * 支持解析：
     * “1200 MILLISECONDS”。
     * “1200 MILLISECONDS ”，空格将被忽略。
     * “1 MINUTE”，单数形式的时间单位。
     */
    public static TimeValue parse(final String value) throws ParseException {
        final String split[] = value.trim().split("\\s+");
        if (split.length < 2)
            throw new IllegalArgumentException(String.format("Expected format for <Long><SPACE><java.util.concurrent.TimeUnit>: %s", value));
        final String clean0 = split[0].trim();
        final String clean1 = split[1].trim().toUpperCase(Locale.ROOT);
        final String timeUnitStr = clean1.endsWith("S") ? clean1 : clean1 + "S";
        return TimeValue.of(Long.parseLong(clean0), TimeUnit.valueOf(timeUnitStr));
    }

    /** 主要字段1，时间值 */
    private final long duration;

    /** 主要字段2，对应的时间单位 */
    private final TimeUnit timeUnit;

    TimeValue(final long duration, final TimeUnit timeUnit) {
        super();
        this.duration = duration;
        this.timeUnit = Args.notNull(timeUnit, "timeUnit");
    }

    /**
     * 转换成目标TimeUnit的对应时间值。
     */
    public long convert(final TimeUnit targetTimeUnit) {
        Args.notNull(targetTimeUnit, "timeUnit");
        return targetTimeUnit.convert(duration, timeUnit);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof TimeValue) {
            final TimeValue that = (TimeValue) obj;
            final long thisDuration = this.convert(TimeUnit.NANOSECONDS);
            final long thatDuration = that.convert(TimeUnit.NANOSECONDS);
            return thisDuration == thatDuration;
        }
        return false;
    }

    public TimeValue divide(final long divisor) {
        final long newDuration = duration / divisor;
        return of(newDuration, timeUnit);
    }

    public TimeValue divide(final long divisor, final TimeUnit targetTimeUnit) {
        return of(convert(targetTimeUnit) / divisor, targetTimeUnit);
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.convert(TimeUnit.NANOSECONDS));
        return hash;
    }

    public TimeValue min(final TimeValue other) {
        return this.compareTo(other) > 0 ? other : this;
    }

    private TimeUnit min(final TimeUnit other) {
        return scale() > scale(other) ? other : getTimeUnit();
    }

    private int scale() {
        return scale(timeUnit);
    }

    /**
     * NANOSECONDS -> 1
     * MICROSECONDS -> 2
     * MILLISECONDS -> 3
     * SECONDS -> 4
     * MINUTES -> 5
     * HOURS -> 6
     * DAYS -> 7
     */
    private int scale(final TimeUnit tUnit) {
        switch (tUnit) {
            case NANOSECONDS:
                return 1;
            case MICROSECONDS:
                return 2;
            case MILLISECONDS:
                return 3;
            case SECONDS:
                return 4;
            case MINUTES:
                return 5;
            case HOURS:
                return 6;
            case DAYS:
                return 7;
            default:
                // Should never happens unless Java adds to the enum.
                throw new IllegalStateException();
        }
    }

    public void sleep() throws InterruptedException {
        timeUnit.sleep(duration);
    }

    public void timedJoin(final Thread thread) throws InterruptedException {
        timeUnit.timedJoin(thread, duration);
    }

    public void timedWait(final Object obj) throws InterruptedException {
        timeUnit.timedWait(obj, duration);
    }

    public long toDays() {
        return timeUnit.toDays(duration);
    }

    public Duration toDuration() {
        return duration == 0 ? Duration.ZERO : Duration.of(duration, toChronoUnit(timeUnit));
    }

    public long toHours() {
        return timeUnit.toHours(duration);
    }

    public long toMicroseconds() {
        return timeUnit.toMicros(duration);
    }

    public long toMilliseconds() {
        return timeUnit.toMillis(duration);
    }

    public int toMillisecondsIntBound() {
        return asBoundInt(toMilliseconds());
    }

    public long toMinutes() {
        return timeUnit.toMinutes(duration);
    }

    public long toNanoseconds() {
        return timeUnit.toNanos(duration);
    }

    public long toSeconds() {
        return timeUnit.toSeconds(duration);
    }

    public int toSecondsIntBound() {
        return asBoundInt(toSeconds());
    }

    @Override
    public int compareTo(final TimeValue other) {
        // 先转成相同的TimeUnit然后再比较
        final TimeUnit targetTimeUnit = min(other.getTimeUnit());
        return Long.compare(convert(targetTimeUnit), other.convert(targetTimeUnit));
    }

    @Override
    public String toString() {
        return String.format("%d %s", duration, timeUnit);
    }

    public Timeout toTimeout() {
        return Timeout.of(duration, timeUnit);
    }
}
