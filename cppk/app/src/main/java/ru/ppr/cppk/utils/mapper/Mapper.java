package ru.ppr.cppk.utils.mapper;

import android.support.annotation.NonNull;

/**
 * Created by Артем on 26.02.2016.
 */
public interface Mapper <T,V> {
    @NonNull
    V mapTo(@NonNull T t);
}
