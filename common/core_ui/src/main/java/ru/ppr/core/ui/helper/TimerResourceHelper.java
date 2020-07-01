package ru.ppr.core.ui.helper;

import ru.ppr.core.ui.R;

/**
 * Попошник, выдающий ресурс цифры таймера в зависимости от значения
 *
 * @author Grigoriy Kashka
 */
public class TimerResourceHelper {

    public static int getTimerImageResource(int value) {
        if (value == 9) {
            return R.drawable.timer_9_sec;
        } else if (value == 8) {
            return R.drawable.timer_8_sec;
        } else if (value == 7) {
            return R.drawable.timer_7_sec;
        } else if (value == 6) {
            return R.drawable.timer_6_sec;
        } else if (value == 5) {
            return R.drawable.timer_5_sec;
        } else if (value == 4) {
            return R.drawable.timer_4_sec;
        } else if (value == 3) {
            return R.drawable.timer_3_sec;
        } else if (value == 2) {
            return R.drawable.timer_2_sec;
        } else if (value == 1) {
            return R.drawable.timer_1_sec;
        }
        return R.drawable.timer_0_sec;
    }
}
