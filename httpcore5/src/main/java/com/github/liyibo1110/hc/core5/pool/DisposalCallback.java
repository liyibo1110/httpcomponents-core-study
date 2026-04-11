package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.annotation.Internal;
import com.github.liyibo1110.hc.core5.io.CloseMode;
import com.github.liyibo1110.hc.core5.io.ModalCloseable;

/**
 * 可自定义的ModalCloseable实例销毁策略。
 * @author liyibo
 * @date 2026-04-10 18:05
 */
@Internal
public interface DisposalCallback<T extends ModalCloseable> {

    void execute(final T closeable, CloseMode closeMode);
}
