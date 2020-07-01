package ru.ppr.cppk.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.HashSet;
import java.util.Set;

import ru.ppr.logger.Logger;

/**
 * Уведомляет о необходимости блокировки экрана.
 * <p>
 * Created by Александр on 05.10.2016.
 */
public final class ScreenLockManagerReal implements ScreenLockManager {

    private static final String TAG = Logger.makeLogTag(ScreenLockManagerReal.class);

    private final Context context;
    private Handler handler;
    /**
     * Время последней активности
     */
    private long lastActionTimestamp;
    /**
     * Должен ли экран сейчас быть залочен
     */
    private boolean screenShouldBeLocked;
    /**
     * Время до блокировки экрана после последнего действия, в мс
     */
    private int lockDelay;
    /**
     * Слушатели состояния
     */
    private final Set<ScreenLockListener> screenLockListeners = new HashSet<>();

    /**
     * Конструктор
     *
     * @param context   Контекст
     * @param lockDelay Время до блокировки, в секундах
     */
    public ScreenLockManagerReal(Context context, int lockDelay) {
        this.context = context.getApplicationContext();
        this.lockDelay = lockDelay * 1000;
        this.handler = new Handler();

        Logger.trace(TAG, "ScreenLockManager initialized, lock delay: " + String.valueOf(this.lockDelay) + " ms.");
        updateLastActionTimestamp();
        reset();
    }

    /**
     * Освобождает ресурсы, связанные с объектом
     */
    @Override
    public void destroy() {
        handler.removeCallbacks(delayedLockRunnable);
        Logger.trace(TAG, "ScreenLockManager destroyed");
    }

    /**
     * Обновляет время последней активности пользователя
     */
    @Override
    public void updateLastActionTimestamp() {
        lastActionTimestamp = SystemClock.elapsedRealtime();

        if (screenShouldBeLocked) {
            // На экране отключена блокировка, но активность регистрируется (Splash, например)
            // Нужно сбросить таймер, чтобы при открытии обычного окна сразу не включилась блокировка
            reset();
        }
    }

    @Override
    public boolean isScreenShouldBeLocked() {
        return screenShouldBeLocked;
    }

    /**
     * Устанаваливает время до блокировки
     *
     * @param lockDelay Время до блокировки, в секундах
     */
    @Override
    public void setLockDelay(int lockDelay) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("You must call it from UI Thread");
        }

        this.lockDelay = lockDelay * 1000;
        Logger.trace(TAG, "setLockDelay: lock delay = " + String.valueOf(this.lockDelay) + " ms.");

        if (screenShouldBeLocked) {
            // Не стоит что-то проверять и перезапускать,
            // если экран в данный момент уже должен быть залочен
            checkScreenShouldBeLocked();
        }
    }

    /**
     * Сбрасывает таймер, запускает новое ожидание блокировки
     */
    @Override
    public void reset() {
        Logger.trace(TAG, "reset");
        screenShouldBeLocked = false;
        handler.postDelayed(delayedLockRunnable, lockDelay);
    }

    /**
     * Регистрирует внешнего слушателя состояния экрана
     *
     * @param screenLockListener слушатель
     */
    @Override
    public void addScreenLockListener(ScreenLockListener screenLockListener) {
        screenLockListeners.add(screenLockListener);
        screenLockListener.onScreenShouldBeLocked(screenShouldBeLocked);
    }

    /**
     * Удаляет слушателя из списка
     *
     * @param screenLockListener слушатель
     */
    @Override
    public void removeScreenLockListener(ScreenLockListener screenLockListener) {
        screenLockListeners.remove(screenLockListener);
    }

    private void checkScreenShouldBeLocked() {
        long currentTimestamp = SystemClock.elapsedRealtime();
        long diff = lastActionTimestamp + lockDelay - currentTimestamp;

        final StringBuilder sb = new StringBuilder();
        sb.append("checkScreenShouldBeLocked: ");
        sb.append("current timestamp = ");
        sb.append(currentTimestamp);
        sb.append(", last action timestamp = ");
        sb.append(lastActionTimestamp);
        sb.append(", lockDelay = ");
        sb.append(lockDelay);
        sb.append(", diff = ");
        sb.append(diff);
        sb.trimToSize();
        Logger.trace(TAG, sb.toString());

        handler.removeCallbacks(delayedLockRunnable);

        if (diff <= 0) {
            onScreenShouldBeLocked(true);
        } else {
            handler.postDelayed(delayedLockRunnable, diff);
        }
    }

    private void onScreenShouldBeLocked(boolean screenShouldBeLocked) {
        Logger.trace(TAG, "onScreenShouldBeLocked: screenShouldBeLocked = " + screenShouldBeLocked);
        this.screenShouldBeLocked = screenShouldBeLocked;
        for (ScreenLockListener screenLockListener : screenLockListeners) {
            screenLockListener.onScreenShouldBeLocked(this.screenShouldBeLocked);
        }
    }

    private Runnable delayedLockRunnable = () -> checkScreenShouldBeLocked();
}
