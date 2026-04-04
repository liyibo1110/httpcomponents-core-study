package com.github.liyibo1110.hc.core5.http;

import java.io.IOException;

/**
 * 代表违反了Message约束的异常。
 * @author liyibo
 * @date 2026-04-03 14:23
 */
public class MessageConstraintException extends IOException {
    private static final long serialVersionUID = 6077207720446368695L;

    public MessageConstraintException(final String message) {
        super(HttpException.clean(message));
    }
}
