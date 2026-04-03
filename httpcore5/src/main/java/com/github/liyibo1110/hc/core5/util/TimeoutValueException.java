package com.github.liyibo1110.hc.core5.util;

import java.util.concurrent.TimeoutException;

/**
 * TimeoutException的子类，携带了相关Timeout对象。
 * @author liyibo
 * @date 2026-04-02 15:25
 */
public class TimeoutValueException extends TimeoutException {
    private static final long serialVersionUID = 1L;

    public static TimeoutValueException fromMilliseconds(final long timeoutDeadline, final long timeoutActual) {
        return new TimeoutValueException(Timeout.ofMilliseconds(min0(timeoutDeadline)),
                Timeout.ofMilliseconds(min0(timeoutActual)));
    }

    private static long min0(final long value) {
        return value < 0 ? 0 : value;
    }

    /** 预期的超时时间 */
    private final Timeout actual;

    /** 实际等待了的超时时间 */
    private final Timeout deadline;

    public TimeoutValueException(final Timeout deadline, final Timeout actual) {
        super(String.format("Timeout deadline: %s, actual: %s", deadline, actual));
        this.actual = actual;
        this.deadline = deadline;
    }

    public Timeout getActual() {
        return actual;
    }

    public Timeout getDeadline() {
        return deadline;
    }
}
