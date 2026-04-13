package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.http.SocketModalCloseable;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.util.TimeValue;
import com.github.liyibo1110.hc.core5.util.Timeout;

/**
 * DisposalCallback接口的默认实现类。
 * @author liyibo
 * @date 2026-04-13 10:17
 */
@Internal
public final class DefaultDisposalCallback<T extends SocketModalCloseable> implements DisposalCallback<T> {

    private final static Timeout DEFAULT_CLOSE_TIMEOUT = Timeout.ofSeconds(1L);

    @Override
    public void execute(final SocketModalCloseable closeable, final CloseMode closeMode) {
        final Timeout socketTimeout = closeable.getSocketTimeout();
        if(socketTimeout == null
                || socketTimeout.compareTo(TimeValue.ZERO_MILLISECONDS) <= 0
                || socketTimeout.compareTo(DEFAULT_CLOSE_TIMEOUT) > 0) {
            closeable.setSocketTimeout(DEFAULT_CLOSE_TIMEOUT);
        }
        closeable.close(closeMode);
    }
}
