package ru.ppr.cppk.systembar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import ru.ppr.logger.Logger;

/**
 * Активити, которая умеет делать скриношот
 *
 * @autor Grigoriy Kashka
 * @see ru.ppr.cppk.systembar.FeedbackActivityDelegate
 * @see ru.ppr.cppk.helpers.FeedbackHelper
 */
public class FeedbackActivity extends Activity {

    private static final String TAG = Logger.makeLogTag(FeedbackActivity.class);

    /**
     * UI handler
     */
    private Handler handler;

    /**
     * Данный флаг разрешает вызывать окносоздания отчета об ошибке
     */
    private boolean canUseReportButton = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result;
        if (keyCode == ru.ppr.core.ui.helper.CoppernicKeyEvent.getFeedbackKeyCode()) {
            event.startTracking();
            result = true;
        } else
            result = super.onKeyDown(keyCode, event);
        return result;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == ru.ppr.core.ui.helper.CoppernicKeyEvent.getFeedbackKeyCode() && canUseReportButton) {
            //запустим асинхронно чтобы не словить ANR, если создание скриншота будет выполняться долго
            handler.post(() -> FeedbackActivityDelegate.getInstance(FeedbackActivity.this).showDialog());
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    /**
     * Запретить возможность сделать отчет об ошибке
     */
    public void disableReport() {
        canUseReportButton = false;
    }

    /**
     * Разрешает возможность содать отчет об ошибке
     */
    public void enableReport() {
        canUseReportButton = true;
    }

}
