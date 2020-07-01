package ru.ppr.cppk.helpers;

import android.support.annotation.NonNull;
import android.view.KeyEvent;

import java.util.concurrent.TimeUnit;

import ru.ppr.core.ui.helper.CoppernicKeyEvent;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Обработчик комбинации клавиш для создания резервной копии.
 *
 * @author Dmitry Vinogradov
 */
public class BackupAccessRequestConditionsChecker {

    private static final String TAG = Logger.makeLogTag(BackupAccessRequestConditionsChecker.class);

    public interface Callback {
        void onConditionsMet();

        void onConditionsNotMet();
    }

    boolean volumeDownHeld = false;
    boolean volumeUpHeld = false;
    int blackLeftCounter = 0;

    Subscription condition;

    boolean started = false;
    Callback callback;

    public BackupAccessRequestConditionsChecker(@NonNull BackupAccessRequestConditionsChecker.Callback callback) {
        this.callback = callback;
    }

    public boolean onKey(int keyCode, @NonNull KeyEvent event) {
        if (isStarted()) {
            boolean actionDown = event.getAction() == KeyEvent.ACTION_DOWN;
            boolean actionUp = event.getAction() == KeyEvent.ACTION_UP;
            boolean actionDownOrUp = actionDown || actionUp;

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP ) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    volumeDownHeld = actionDownOrUp && actionDown;
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    volumeUpHeld = actionDownOrUp && actionDown;
                }

                if (volumeDownHeld && volumeUpHeld) {
                    //проверка на случай, если мы 2 раза подряд зажали и отжали кнопки ум. звука и ув. звука,
                    //важно только первое срабатывание, следующие не учитываем

                    condition = Observable
                            .timer(2, TimeUnit.SECONDS, SchedulersCPPK.background())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> {
                                reset();
                                Logger.info(TAG, "Не удалось отобразить диалог создания бэкапа, не были выполнены необходимые условия");
                                callback.onConditionsNotMet();
                            });
                }

                return true;
            } else if (keyCode == CoppernicKeyEvent.getFeedbackKeyCode()) {//левая черная кнопка
                if (actionDownOrUp && actionUp && volumeDownHeld && volumeUpHeld) {

                    Logger.info(TAG, "Попытка попасть в диалог создания бэкапа");

                    if (++blackLeftCounter == 2) {
                        //проверка на случай, если мы 2 раза подряд нажали 2 раза левую черную кнопку,
                        //важно только первое срабатывание, следующие не учитываем
                        if (condition != null) {
                            condition.unsubscribe();
                            condition = null;

                            reset();
                            Logger.info(TAG, "Зарегистрировано попадание в диалог создания бэкапа");
                            callback.onConditionsMet();
                        }
                    }
                }
                return true;
            }

            //если пришло другое событие - значит нажали другие кнопки
            //в этом случае всё обнуляем, чтобы не было возможности
            //выполнить условия просто нажав всё сразу
            reset();
        }

        return false;
    }

    private void reset() {
        volumeDownHeld = false;
        volumeUpHeld = false;
        blackLeftCounter = 0;
    }

    public void start() {
        reset();
        started = true;
    }

    public void stop() {
        reset();

        if (condition != null) {
            condition.unsubscribe();
            condition = null;
        }

        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
