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
 * Обработчик комбинации клавиш для входа в рут-меню.
 *
 * @author Dmitry Nevolin
 */
public class RootAccessRequestConditionsChecker {

    private static final String TAG = Logger.makeLogTag(RootAccessRequestConditionsChecker.class);

    public interface Callback {
        void onConditionsMet();

        void onConditionsNotMet();
    }

    boolean volumeDownHeld = false;
    boolean blueRightHeld = false;
    int blueLeftCounter = 0;

    Subscription firstCondition;
    Subscription secondCondition;

    boolean started = false;
    Callback callback;

    public RootAccessRequestConditionsChecker(@NonNull Callback callback) {
        this.callback = callback;
    }

    public boolean onKey(int keyCode, @NonNull KeyEvent event) {
        if (isStarted()) {
            boolean actionDown = event.getAction() == KeyEvent.ACTION_DOWN;
            boolean actionUp = event.getAction() == KeyEvent.ACTION_UP;
            boolean actionDownOrUp = actionDown || actionUp;

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == CoppernicKeyEvent.getRfidKeyCode()) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    volumeDownHeld = actionDownOrUp && actionDown;
                } else {
                    blueRightHeld = actionDownOrUp && actionDown;
                }

                if (volumeDownHeld && blueRightHeld) {
                    //проверка на случай, если мы 2 раза подряд зажали и отжали кнопки ум. звука и правую стинюю,
                    //важно только первое срабатывание, следующие не учитываем
                    if (firstCondition != null) {
                        firstCondition.unsubscribe();
                        firstCondition = null;

                        secondCondition = Observable
                                .timer(2, TimeUnit.SECONDS, SchedulersCPPK.background())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe(aLong -> {
                                    stop();
                                    Logger.info(TAG, "Не удалось попасть на окно доступа к рут меню, не были выполнены необходимые условия");
                                    callback.onConditionsNotMet();
                                });
                    }
                }

                return true;
            } else if (keyCode == CoppernicKeyEvent.getBarcodeKeyCode()) {//левая
                if (actionDownOrUp && actionUp && volumeDownHeld && blueRightHeld) {
                    if (++blueLeftCounter == 3) {
                        //проверка на случай, если мы 2 раза подряд нажали 5 раз левую синюю кнопку,
                        //важно только первое срабатывание, следующие не учитываем
                        if (secondCondition != null) {
                            secondCondition.unsubscribe();
                            secondCondition = null;

                            stop();
                            Logger.info(TAG, "Зарегистрировано попадание на окно доступа к рут меню");
                            callback.onConditionsMet();
                        }
                    }

                    Logger.info(TAG, "Попытка попасть на окно доступа к рут меню");
                }

                return true;
            }
            //если пришло другое событие - значит нажали другие кнопки
            //в этом случае всё обнуляем, чтобы не было возможности
            //выполнить условия просто нажав всё сразу
            volumeDownHeld = false;
            blueRightHeld = false;
            blueLeftCounter = 0;
        }

        return false;
    }

    public void start() {
        volumeDownHeld = false;
        blueRightHeld = false;
        blueLeftCounter = 0;

        firstCondition = Observable
                .timer(2, TimeUnit.SECONDS, SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    stop();
                    callback.onConditionsNotMet();
                });

        started = true;
    }

    public void stop() {
        volumeDownHeld = false;
        blueRightHeld = false;
        blueLeftCounter = 0;
        if (firstCondition != null) {
            firstCondition.unsubscribe();
            firstCondition = null;
        }
        if (secondCondition != null) {
            secondCondition.unsubscribe();
            secondCondition = null;
        }

        started = false;
    }

    public boolean isStarted() {
        return started;
    }

}
