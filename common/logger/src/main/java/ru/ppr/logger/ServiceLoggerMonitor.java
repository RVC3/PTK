package ru.ppr.logger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dmitry Nevolin on 18.04.2016.
 */
public class ServiceLoggerMonitor extends Service {

    private static final int WAKE_UP_PERIOD = 1000 * 5;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, ServiceLoggerMonitor.class);
    }

    private static Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer("Timer-ServiceLoggerMonitor");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                taskImpl();
            }
        }, WAKE_UP_PERIOD, WAKE_UP_PERIOD);
    }



    @Override
    public void onDestroy() {
        timer.cancel();
        timer = null;

        super.onDestroy();
    }

    private static void taskImpl() {
        Logger.flushQueueAsync();
    }

}
