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

    void onError(Exception ex);

    void onError(HttpConnection connection, Exception ex);
}
