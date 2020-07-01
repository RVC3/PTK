package ru.ppr.cppk.debug.batterytest.impl;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import ru.ppr.cppk.debug.batterytest.core.Task;

/**
 * Created by nevolin on 11.07.2016.
 */
public class TaskScreenEnable implements Task {

    public static class Builder implements Task.Builder {

        private Context context;

        public Builder setContext(Context context) {
            this.context = context;

            return this;
        }

        @Override
        public Task build() {
            return new TaskScreenEnable(this);
        }

    }

    private final Context context;

    private TaskScreenEnable(Builder builder) {
        context = builder.context;
    }

    @Override
    public void execute() {
        final KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(TaskScreenEnable.class.getCanonicalName());
        final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TaskScreenEnable.class.getCanonicalName());

        keyguardLock.disableKeyguard();

        wakeLock.acquire();
        wakeLock.release();

        keyguardLock.reenableKeyguard();

    }

}
