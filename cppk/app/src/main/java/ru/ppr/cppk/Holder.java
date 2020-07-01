package ru.ppr.cppk;

/**
 * Created by Александр on 28.09.2016.
 */
public interface Holder<T> {

    T get();

    void set(T value);
}
