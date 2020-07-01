package ru.ppr.cppk.entity.utils.builders.events;

import com.google.common.base.Preconditions;

/**
 * Created by Артем on 16.12.2015.
 */
public abstract class AbstractGenerator {

    protected void checkNotNull(Object object, String message){
        Preconditions.checkNotNull(object, message);
    }

}
