package ru.ppr.core.ui.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import ru.ppr.logger.Logger;

/**
 * Кнопка, которую можно нажимать не чаще чем раз в 300мс
 *
 * @author Grigoriy Kashka 10.10.2016.
 */
public class SingleClickButton extends Button {

    private static String TAG = Logger.makeLogTag(SingleClickButton.class);
    private long lastOnClickEvent = 0;
    private long blockedTimeMs = 300;

    public SingleClickButton(Context context) {
        super(context);
    }

    public SingleClickButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleClickButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(v -> {
            long diff = SystemClock.elapsedRealtime() - lastOnClickEvent;
            if (diff > blockedTimeMs) {
                lastOnClickEvent = SystemClock.elapsedRealtime();
                Logger.info(TAG, "Пользователь нажал на кнопку: \"" + getText() + "\"");
                l.onClick(v);
            } else {
                Log.w(TAG, "onClick ignored diff=" + diff + "mc");
            }
        });
    }
}
