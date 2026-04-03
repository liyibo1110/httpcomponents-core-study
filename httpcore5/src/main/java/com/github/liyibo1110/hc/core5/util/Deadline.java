package com.github.liyibo1110.hc.core5.util;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.concurrent.TimeUnit;

/**
 * 基于UNIX时间的截止时间，即自1970年1月1日星期四00:00:00协调世界时（UTC）起经过的时间。
 * 和Timeout相比，这个Deadline内部存储的是绝对时间戳。
 * @author liyibo
 * @date 2026-04-02 14:29
 */
public class Deadline {

    /** 用于解析和格式化日期的格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /** 一个特殊的内部值，用于标记截止日期为最长可能的期限。 */
    private static final long INTERNAL_MAX_VALUE = Long.MAX_VALUE;

    private static final long INTERNAL_MIN_VALUE = 0;

    public static Deadline MAX_VALUE = new Deadline(INTERNAL_MAX_VALUE);

    public static Deadline MIN_VALUE = new Deadline(INTERNAL_MIN_VALUE);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseLenient()
            .parseCaseInsensitive()
            .appendPattern(DATE_FORMAT)
            .toFormatter();

    /**
     * 根据给定的毫秒数加上指定的时间值，计算出截止时间。非正数的时间值表示无截止时间的无限期超时
     * @param timeMillis 通常为当前时间戳
     * @param timeValue 要追加的超时时间
     */
    public static Deadline calculate(final long timeMillis, final TimeValue timeValue) {
        if(TimeValue.isPositive(timeValue)) {
            final long deadline = timeMillis + timeValue.toMilliseconds();
            return deadline < 0 ? Deadline.MAX_VALUE : Deadline.fromUnixMilliseconds(deadline);
        }
        // 到这里说明是无限期超时（永远等待）
        return Deadline.MAX_VALUE;
    }

    public static Deadline calculate(final TimeValue timeValue) {
        return calculate(System.currentTimeMillis(), timeValue);
    }

    public static Deadline fromUnixMilliseconds(final long value) {
        if (value == INTERNAL_MAX_VALUE)
            return MAX_VALUE;
        if (value == INTERNAL_MIN_VALUE)
            return MIN_VALUE;
        return new Deadline(value);
    }

    /**
     * 格式为：yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static Deadline parse(final String source) throws ParseException {
        if (source == null)
            return null;
        final Instant instant = Instant.from(DATE_TIME_FORMATTER.parse(source));
        return fromUnixMilliseconds(instant.toEpochMilli());
    }

    /** 如果是true，则lastCheck就不能再变了 */
    private volatile boolean frozen;

    /** 最后一次check时的时间戳 */
    private volatile long lastCheck;

    /** unix时间戳 */
    private final long value;

    private Deadline(final long deadlineMillis) {
        super();
        this.value = deadlineMillis;
        setLastCheck();
    }

    @Override
    public boolean equals(final Object obj) {
        // Only take into account the deadline value.
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Deadline other = (Deadline) obj;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        // Only take into account the deadline value.
        return Long.hashCode(value);
    }

    public String format(final TimeUnit overdueTimeUnit) {
        if (value == MAX_VALUE.value)
            return "No deadline (infinite)";
        else
            return String.format("Deadline: %s, %s overdue", formatTarget(), TimeValue.of(remaining(), overdueTimeUnit));
    }

    public String formatTarget() {
        if (value == MAX_VALUE.value)
            return "(infinite)";
        else
            return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(value).atOffset(ZoneOffset.UTC));
    }

    public Deadline freeze() {
        frozen = true;
        return this;
    }

    long getLastCheck() {
        return lastCheck;
    }

    public long getValue() {
        return value;
    }

    public boolean isBefore(final long millis) {
        return value < millis;
    }

    /**
     * 检测超时时间是否已到
     */
    public boolean isExpired() {
        setLastCheck();
        return value < this.lastCheck;
    }

    public boolean isMax() {
        return value == INTERNAL_MAX_VALUE;
    }

    public boolean isMin() {
        return value == INTERNAL_MIN_VALUE;
    }

    /**
     * 检测超时时间是否还未到
     */
    public boolean isNotExpired() {
        setLastCheck();
        return value >= this.lastCheck;
    }

    public Deadline min(final Deadline other) {
        return value <= other.value ? this : other;
    }

    /**
     * 计算并返回该超时还有多少毫秒到期。
     */
    public long remaining() {
        setLastCheck();
        return value - lastCheck;
    }

    public TimeValue remainingTimeValue() {
        return TimeValue.of(remaining(), TimeUnit.MILLISECONDS);
    }

    private void setLastCheck() {
        if (!frozen)
            this.lastCheck = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return formatTarget();
    }
}
