package ru.ppr.cppk.debug.batterytest.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.ppr.cppk.debug.batterytest.core.Task;

/**
 * Created by nevolin on 12.07.2016.
 */
public class TaskCpuLoad implements Task {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static class Builder implements Task.Builder {

        private long millis;

        public Builder setMillis(long millis) {
            this.millis = millis;

            return this;
        }

        @Override
        public Task build() {
            return new TaskCpuLoad(this);
        }

    }

    private final long millis;

    private TaskCpuLoad(Builder builder) {
        millis = builder.millis;
    }

    @Override
    public void execute() {
        Runnable numberCruncher = () -> {
            final long start = System.currentTimeMillis();

            int number = Integer.MAX_VALUE;
            BigInteger sum = BigInteger.ZERO;

            while(number > 0 && System.currentTimeMillis() - start < millis) {
                sum = sum.add(new BigInteger(String.valueOf(number--)));
            }
        };

        List<Callable<Object>> calls = new ArrayList<>();

        calls.add(Executors.callable(numberCruncher));
        calls.add(Executors.callable(numberCruncher));
        calls.add(Executors.callable(numberCruncher));
        calls.add(Executors.callable(numberCruncher));

        try {
            executor.invokeAll(calls);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

}
