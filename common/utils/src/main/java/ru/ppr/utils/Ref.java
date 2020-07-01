package ru.ppr.utils;

import android.support.annotation.Nullable;

/**
 * Класс-холдер для nullable объекта.
 * Используется в основном для RxJava2, т.к. там запрещено
 * возвращение null-объектов в цепочке.
 *
 * @author Dmitry Nevolin
 */
public class Ref<T> {

    private final T target;

    public Ref(@Nullable T target) {
        this.target = target;
    }

    @Nullable
    public T get() {
        return target;
    }

}
