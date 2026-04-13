package com.github.liyibo1110.hc.core5.pool;

import com.github.liyibo1110.hc.core5.io.ModalCloseable;

/**
 * ManagedConnPool同时实现了ConnPoolControl和AutoCloseable接口。
 * @author liyibo
 * @date 2026-04-13 10:21
 */
public interface ManagedConnPool<T, C extends ModalCloseable> extends ConnPool<T, C>, ConnPoolControl<T>, ModalCloseable {

}
