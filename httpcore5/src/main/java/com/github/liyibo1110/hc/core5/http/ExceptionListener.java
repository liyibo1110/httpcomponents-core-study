package com.github.liyibo1110.hc.core5.http;

import com.github.liyibo1110.hc.core5.annotation.Contract;
import com.github.liyibo1110.hc.core5.annotation.ThreadingBehavior;

/**
 * 可监听特定异常，并执行特定回调。
 * @author liyibo
 * @date 2026-04-03 14:59
 */
@Contract(threading = ThreadingBehavior.STATELESS)
public interface ExceptionListener {

    ExceptionListener NO_OP = new ExceptionListener() {
        @Override
        public void onError(final Exception ex) {}

        @Override
        public void onError(final HttpConnection connection, final Exception ex) {}
    };

    ExceptionListener STD_ERR = new ExceptionListener() {
        @Override
        public void onError(final Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onError(final HttpConnection connection, final Exception ex) {
            ex.printStackTrace();
        }
    };

    void onError(Exception ex);

    void onError(HttpConnection connection, Exception ex);
}
