package ru.ppr.cppk.helpers;


import android.os.Handler;

/**
 * UI-поток.
 *
 * @author Aleksandr Brazhkin
 */
public class UiThread {
    private final Handler mHandler;

    public UiThread(Handler handler) {
        this.mHandler = handler;
    }

    public final boolean post(Runnable r) {
        return mHandler.post(r);
    }
}
