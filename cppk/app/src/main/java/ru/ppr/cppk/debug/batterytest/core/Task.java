package ru.ppr.cppk.debug.batterytest.core;

/**
 * Created by nevolin on 11.07.2016.
 */
public interface Task {

    interface Builder {
        Task build();
    }

    void execute();

}
