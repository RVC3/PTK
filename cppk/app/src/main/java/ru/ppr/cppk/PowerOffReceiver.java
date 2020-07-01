package ru.ppr.cppk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.ppr.logger.Logger;

/**
 * Created by Dmitry Nevolin on 18.04.2016.
 */
public class PowerOffReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.trace(PowerOffReceiver.class, "Action: " + intent.getAction());

        Logger.flushQueueSync();
    }

}
