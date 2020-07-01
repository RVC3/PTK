package ru.ppr.cppk;

/**
 * Created by Александр on 28.09.2016.
 */
public class HolderDefault<T> implements Holder<T> {

    private T value;

    public HolderDefault() {

    }

    public HolderDefault(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }
}
