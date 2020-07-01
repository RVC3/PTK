package ru.ppr.cppk.debug.batterytest.impl;

import ru.ppr.cppk.debug.batterytest.core.Task;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.rfid.IRfid;

/**
 * Created by nevolin on 12.07.2016.
 */
public class TaskSmartCardRead implements Task {

    public static class Builder implements Task.Builder {

        private long millis;

        public Builder setMillis(long millis) {
            this.millis = millis;

            return this;
        }

        @Override
        public Task build() {
            return new TaskSmartCardRead(this);
        }

    }

    private final long millis;

    private TaskSmartCardRead(Builder builder) {
        millis = builder.millis;
    }

    @Override
    public void execute() {
        final IRfid iRfid = Dagger.appComponent().rfid();
        final long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < millis) {
            iRfid.getRfidAtr();
        }
    }

}
