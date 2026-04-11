package com.github.liyibo1110.hc.core5.http.impl.bootstrap;

/**
 * HttpFilterHandler对象的外层封装了（多了position、name和existing字段）。
 * @author liyibo
 * @date 2026-04-10 17:35
 */
public class FilterEntry<T> {

    enum Position { BEFORE, AFTER, REPLACE, FIRST, LAST }

    final Position position;
    final String name;
    final T filterHandler;
    final String existing;

    FilterEntry(final Position position, final String name, final T filterHandler, final String existing) {
        this.position = position;
        this.name = name;
        this.filterHandler = filterHandler;
        this.existing = existing;
    }
}
