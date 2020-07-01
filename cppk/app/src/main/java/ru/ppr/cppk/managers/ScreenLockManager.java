package ru.ppr.cppk.managers;

/**
 * Уведомляет о необходимости блокировки экрана.
 * <p>
 * Created by Александр on 05.10.2016.
 */
public interface ScreenLockManager {

    /**
     * Освобождает ресурсы, связанные с объектом
     */
    void destroy();

    /**
     * Обновляет время последней активности пользователя
     */
    void updateLastActionTimestamp();

    /**
     * Экран должен быть заблокирован
     * @return
     */
    boolean isScreenShouldBeLocked();

    /**
     * Устанаваливает время до блокировки
     *
     * @param lockDelay Время до блокировки, в секундах
     */
    public void setLockDelay(int lockDelay);

    /**
     * Сбрасывает таймер, запускает новое ожидание блокировки
     */
    void reset();

    /**
     * Регистрирует внешнего слушателя состояния экрана
     *
     * @param screenLockListener слушатель
     */
    void addScreenLockListener(ScreenLockListener screenLockListener);

    /**
     * Удаляет слушателя из списка
     *
     * @param screenLockListener слушатель
     */
    void removeScreenLockListener(ScreenLockListener screenLockListener);

    interface ScreenLockListener {
        void onScreenShouldBeLocked(boolean screenShouldBeLocked);
    }
}
