package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

/**
 * Created by Артем on 16.12.2015.
 */
public interface Generator<T> {


    @NonNull T build();
}
