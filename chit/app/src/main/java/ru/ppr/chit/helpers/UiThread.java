package ru.ppr.chit.helpers;

import android.os.Handler;

import javax.inject.Inject;

/**
 * UI-поток.
 *
 * @author Aleksandr Brazhkin
 */
public class UiThread {

    private final Handler handler;

    @Inject
    public UiThread(Handler handler) {
        this.handler = handler;
    }

    public final boolean post(Runnable runnable) {
        return handler.post(runnable);
    }

}
