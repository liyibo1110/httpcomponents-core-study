package com.github.liyibo1110.hc.core5.pool;

/**
 * 连接复用策略枚举。
 * @author liyibo
 * @date 2026-04-12 14:13
 */
public enum PoolReusePolicy {

    /** 尽可能少地复用连接，以便连接能够处于空闲状态并过期。 */
    LIFO,

    /** 平等地重复利用所有连接，以防止它们处于空闲状态并过期。 */
    FIFO
}
