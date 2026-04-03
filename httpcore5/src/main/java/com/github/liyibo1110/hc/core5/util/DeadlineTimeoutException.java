package com.github.liyibo1110.hc.core5.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * TimeoutException的子类，携带了相关Deadline对象。
 * @author liyibo
 * @date 2026-04-02 14:28
 */
public class DeadlineTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 1L;

    public static DeadlineTimeoutException from(final Deadline deadline) {
        return new DeadlineTimeoutException(deadline);
    }

    private final Deadline deadline;

    private DeadlineTimeoutException(final Deadline deadline) {
        super(deadline.format(TimeUnit.MILLISECONDS));
        this.deadline = deadline;
    }

    public Deadline getDeadline() {
        return deadline;
    }
}
