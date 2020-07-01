package ru.ppr.cppk.debug.batterytest.impl;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

import ru.ppr.cppk.debug.batterytest.core.Task;

/**
 * Created by nevolin on 11.07.2016.
 */
public class TaskScreenDisable implements Task {

    public static class Builder implements Task.Builder {

        private Context context;

        public Builder setContext(Context context) {
            this.context = context;

            return this;
        }

        @Override
        public Task build() {
            return new TaskScreenDisable(this);
        }

    }

    private final Context context;

    private TaskScreenDisable(Builder builder) {
        context = builder.context;
    }

    @Override
    public void execute() {
        ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
    }

}
