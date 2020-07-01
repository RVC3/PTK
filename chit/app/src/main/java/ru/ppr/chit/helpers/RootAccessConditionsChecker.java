package ru.ppr.chit.helpers;

import android.support.annotation.NonNull;
import android.view.KeyEvent;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.core.ui.helper.CoppernicKeyEvent;
import ru.ppr.logger.Logger;

/**
 * Обработчик условий (комбинации клавишь) для входа в рут-меню.
 *
 * @author Dmitry Nevolin
 */
public class RootAccessConditionsChecker {

    private static final String TAG = Logger.makeLogTag(RootAccessConditionsChecker.class);

    private static final int BLUE_LEFT_COUNT_EXPECTED = 3;
    private static final int CONDITIONS_TIME = 2;

    private boolean volumeDownHeld = false;
    private boolean blueRightHeld = false;
    private int blueLeftCounter = 0;

    private boolean conditionsMet = false;
    private boolean checkAwaiting = false;

    private Disposable firstCondition = Disposables.disposed();
    private Disposable secondCondition = Disposables.disposed();
    private final PublishSubject<Boolean> conditionsMetPublisher = PublishSubject.create();

    @Inject
    RootAccessConditionsChecker() {

    }

    public void awaitCheck() {
        volumeDownHeld = false;
        blueRightHeld = false;
        blueLeftCounter = 0;
        conditionsMet = false;

        firstCondition = Observable
                .timer(CONDITIONS_TIME, TimeUnit.SECONDS, AppSchedulers.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    stopAwaiting();
                    conditionsMetPublisher.onNext(false);
                });

        checkAwaiting = true;
    }

    public boolean onKey(int keyCode, @NonNull KeyEvent event) {
        if (checkAwaiting) {
            boolean actionDown = event.getAction() == KeyEvent.ACTION_DOWN;
            boolean actionUp = event.getAction() == KeyEvent.ACTION_UP;

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDownHeld = actionDown;
                checkFirstCondition();
                return true;
            } else if (keyCode == CoppernicKeyEvent.getRfidKeyCode()) {
                blueRightHeld = actionDown;
                checkFirstCondition();
                return true;
            } else if (keyCode == CoppernicKeyEvent.getBarcodeKeyCode()) {
                if (actionUp && volumeDownHeld && blueRightHeld) {
                    if (++blueLeftCounter == BLUE_LEFT_COUNT_EXPECTED) {
                        checkSecondCondition();
                    } else {
                        Logger.info(TAG, "Попытка попасть на окно доступа к рут меню");
                    }
                }

                return true;
            }
            // если пришло другое событие - значит нажали другие кнопки
            // в этом случае всё обнуляем, чтобы не было возможности
            // выполнить условия просто нажав всё сразу
            volumeDownHeld = false;
            blueRightHeld = false;
            blueLeftCounter = 0;
            conditionsMet = false;
        }

        return false;
    }

    private void checkFirstCondition() {
        // Если disposed, значит попадаем сюда второй раз
        // после старта, защита от двойных срабатываний
        if (volumeDownHeld && blueRightHeld && !firstCondition.isDisposed() && !conditionsMet) {
            firstCondition.dispose();
            secondCondition = Observable
                    .timer(CONDITIONS_TIME, TimeUnit.SECONDS, AppSchedulers.background())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        stopAwaiting();
                        Logger.info(TAG, "Не удалось попасть на окно доступа к рут меню, не были выполнены необходимые условия");
                        conditionsMetPublisher.onNext(false);
                    });
        }
    }

    private void checkSecondCondition() {
        // Если disposed, значит попадаем сюда второй раз
        // после старта, защита от двойных срабатываний
        if (!secondCondition.isDisposed() && !conditionsMet) {
            conditionsMet = true;
            secondCondition.dispose();
            stopAwaiting();
            Logger.info(TAG, "Зарегистрировано попадание на окно доступа к рут меню");
            conditionsMetPublisher.onNext(true);
        }
    }

    private void stopAwaiting() {
        volumeDownHeld = false;
        blueRightHeld = false;
        blueLeftCounter = 0;
        conditionsMet = false;

        firstCondition.dispose();
        secondCondition.dispose();

        checkAwaiting = false;
    }

    public Observable<Boolean> getConditionsMetPublisher() {
        return conditionsMetPublisher;
    }

}
