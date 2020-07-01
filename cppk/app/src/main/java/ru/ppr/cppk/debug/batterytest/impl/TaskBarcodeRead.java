package ru.ppr.cppk.debug.batterytest.impl;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.cppk.debug.batterytest.core.Task;
import ru.ppr.cppk.di.Dagger;

/**
 * Created by nevolin on 12.07.2016.
 */
public class TaskBarcodeRead implements Task {

    public static class Builder implements Task.Builder {

        private long millis;

        public Builder setMillis(long millis) {
            this.millis = millis;

            return this;
        }

        @Override
        public Task build() {
            return new TaskBarcodeRead(this);
        }

    }

    private final long millis;

    private TaskBarcodeRead(Builder builder) {
        millis = builder.millis;
    }

    @Override
    public void execute() {
        final IBarcodeReader iBarcodeReader = Dagger.appComponent().barcodeReader();
        final long start = System.currentTimeMillis();

        while(System.currentTimeMillis() - start < millis) {
            iBarcodeReader.scan();
        }
    }

}
