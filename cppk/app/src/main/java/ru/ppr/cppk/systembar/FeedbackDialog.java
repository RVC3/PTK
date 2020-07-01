package ru.ppr.cppk.systembar;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

/**
 * Кастомный Dialog, который умеет следить за кнопкой отправки обратной связи
 *
 * @author Grigoriy Kashka
 * @see ru.ppr.cppk.systembar.FeedbackActivityDelegate
 * @see ru.ppr.cppk.helpers.FeedbackHelper
 */
public class FeedbackDialog extends Dialog {

    /**
     * UI handler
     */
    private Handler handler;

    /**
     * Данный флаг разрешает вызывать окносоздания отчета об ошибке
     */
    private boolean canUseReportButton = true;

    private Context context;

    public FeedbackDialog(@NonNull Context context) {
        super(context);
        this.context = context;
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
            startFeedbackDialog();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public void startFeedbackDialog() {
        handler.post(() -> FeedbackActivityDelegate.getInstance(context).showDialog());
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
