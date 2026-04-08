package com.github.liyibo1110.hc.core5.http.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 一个用于构建由唯一类实例组成的链表（链）的构建器类。
 * 列表中每个类只能有一个实例。该类适用于构建协议拦截器列表。
 * @author liyibo
 * @date 2026-04-07 17:16
 */
final class ChainBuilder<E> {

    private final LinkedList<E> list;

    /** 辅助排重用 */
    private final Map<Class<?>, E> uniqueClasses;

    public ChainBuilder() {
        this.list = new LinkedList<>();
        this.uniqueClasses = new HashMap<>();
    }

    /**
     * 同步协调list和uniqueClasses里面的特定元素。
     */
    private void ensureUnique(final E e) {
        final E previous = this.uniqueClasses.remove(e.getClass());
        if (previous != null)
            this.list.remove(previous);
        this.uniqueClasses.put(e.getClass(), e);
    }

    /**
     * 向list添加新元素，如果已存在则先移除旧的，再写入新的。
     */
    public ChainBuilder<E> addFirst(final E e) {
        if (e == null)
            return this;
        ensureUnique(e);
        this.list.addFirst(e);
        return this;
    }

    public ChainBuilder<E> addLast(final E e) {
        if (e == null)
            return this;
        ensureUnique(e);
        this.list.addLast(e);
        return this;
    }

    public ChainBuilder<E> addAllFirst(final Collection<E> c) {
        if (c == null)
            return this;
        for (final E e: c)
            addFirst(e);
        return this;
    }

    @SafeVarargs
    public final ChainBuilder<E> addAllFirst(final E... c) {
        if (c == null)
            return this;
        for (final E e: c)
            addFirst(e);
        return this;
    }

    public ChainBuilder<E> addAllLast(final Collection<E> c) {
        if (c == null)
            return this;
        for (final E e: c)
            addLast(e);
        return this;
    }

    @SafeVarargs
    public final ChainBuilder<E> addAllLast(final E... c) {
        if (c == null)
            return this;
        for (final E e: c)
            addLast(e);
        return this;
    }

    public LinkedList<E> build() {
        return new LinkedList<>(this.list);
    }
}
