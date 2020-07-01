package ru.ppr.core.ui.helper;

import android.os.Build;
import android.view.KeyEvent;

/**
 * @author Dmitry Nevolin
 */
public class CoppernicKeyEvent extends KeyEvent {

    /**
     * Код левой голубой кнопки
     */
    private static final int KEYCODE_LEFT_BLUE;
    /**
     * Код правой голубой кнопки
     */
    private static final int KEYCODE_RIGHT_BLUE;
    /**
     * Код левой черной кнопки
     */
    private static final int KEYCODE_LEFT_BLACK;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            KEYCODE_LEFT_BLUE = KeyEvent.KEYCODE_BRIGHTNESS_DOWN;
            KEYCODE_RIGHT_BLUE = KeyEvent.KEYCODE_BRIGHTNESS_UP;
        } else {
            KEYCODE_LEFT_BLUE = 220;
            KEYCODE_RIGHT_BLUE = 221;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            KEYCODE_LEFT_BLACK = KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK;
        } else {
            KEYCODE_LEFT_BLACK = 222;
        }
    }

    public CoppernicKeyEvent(int action, int code) {
        super(action, code);
    }

    public static int getRfidKeyCode() {
        return KEYCODE_RIGHT_BLUE;
    }

    public static int getBarcodeKeyCode() {
        return KEYCODE_LEFT_BLUE;
    }

    public static int getFeedbackKeyCode() {
        return KEYCODE_LEFT_BLACK;
    }

}
