package com.github.liyibo1110.hc.core5.concurrent;

/**
 * 该接口表示依赖于另一个正在进行的进程或操作的可取消对象。
 * @author liyibo
 * @date 2026-04-14 17:48
 */
public interface CancellableDependency extends Cancellable {

    /**
     * 设置对另一个正在进行的进程或操作的可取消依赖关系，该进程或操作由Cancellable表示。
     */
    void setDependency(Cancellable cancellable);

    /**
     * 确定该进程或操作是否已被取消。
     */
    boolean isCancelled();
}
