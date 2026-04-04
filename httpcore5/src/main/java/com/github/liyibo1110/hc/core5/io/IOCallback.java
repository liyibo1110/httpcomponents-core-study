package com.github.liyibo1110.hc.core5.io;

import java.io.IOException;

/**
 * I/O callback
 * @author liyibo
 * @date 2026-04-03 15:03
 */
public interface IOCallback<T> {

    void execute(T object) throws IOException;
}
