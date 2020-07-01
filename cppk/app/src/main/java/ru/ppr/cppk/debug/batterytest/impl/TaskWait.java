package ru.ppr.cppk.debug.batterytest.impl;

import ru.ppr.cppk.debug.batterytest.core.Task;

/**
 * Created by nevolin on 11.07.2016.
 */
public class TaskWait implements Task {

    public static class Builder implements Task.Builder {

        private long millis;

        public Builder setMillis(long millis) {
            this.millis = millis;

            return this;
        }

        @Override
        public Task build() {
            return new TaskWait(this);
        }

    }

    private final long millis;

    private TaskWait(Builder builder) {
        millis = builder.millis;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

}
