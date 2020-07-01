package ru.ppr.cppk.systembar;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;

/**
 * You have to make a clone of the file DigitalClock.java to use in your
 * application, modify in the following manner:- private final static String m12
 * = "h:mm aa"; private final static String m24 = "k:mm";
 */

public class CustomDigitalClock extends TextView {

    private static final SimpleDateFormat DATA_FORMATTER = new SimpleDateFormat("dd MMM yyyy  -  HH:mm");

    Calendar mCalendar;

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;

    String mFormat;

    Globals globals;
    static boolean msgWasShown = false;

    public CustomDigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public void setWhiteThemeEnable(boolean enable) {
        setTextColor(getResources().getColor((enable) ? R.color.statusBarTextColorWhite : R.color.statusBarTextColor));
    }

    public CustomDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        globals = (Globals) ((Activity) context).getApplication();
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped)
                    return;
                // mCalendar.setTimeInMillis(System.currentTimeMillis());
                // setText(DateFormat.format(mFormat, mCalendar));

                setText(DATA_FORMATTER.format(System.currentTimeMillis()));
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                //checkShiftTime(globals);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    /**
     * Pulls 12/24 mode from system settings
     */
    private boolean get24HourMode() {
        return android.text.format.DateFormat.is24HourFormat(getContext());
    }

}