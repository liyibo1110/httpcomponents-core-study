package com.github.liyibo1110.hc.core5.pool;

/**
 * 连接池并发策略。
 * @author liyibo
 * @date 2026-04-12 14:15
 */
public enum PoolConcurrencyPolicy {

    /** 更高的并发量，但对最大连接数限制的保障较为宽松。 */
    LAX,

    /** 严格的连接数上限保证。 */
    STRICT
}
