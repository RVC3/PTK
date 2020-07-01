package ru.ppr.core.manager.network;

/**
 * Мененджер работы с сетью.
 *
 * @author Aleksandr Brazhkin
 */
public interface NetworkManager {

    /**
     * Проверяет, есть ли подключение к сети.
     *
     * @return {@code true}, если есть {@code false} иначе
     */
    boolean isEnabled();

    /**
     * Устанавливает подключение к сети синхронно для устройства.
     *
     * @param deviceId Id устройства
     * @return {@code true}, если удалось подключить {@code false} иначе
     */
    boolean enableInternetForExternalDevice(Object deviceId);

    /**
     * Отключает подключение к сети синхронно для устройства.
     *
     * @param deviceId Id устройства
     * @return {@code true}, если удалось отключить {@code false} иначе
     */
    boolean disableInternetForExternalDevice(Object deviceId, boolean rightNow);

    /**
     * Устанавливает подключение к сети.
     *
     * @param stateChangedListener колбек, куда будет передан результат операции
     */
    void enable(StateChangedListener stateChangedListener);

    /**
     * Отключает подключение к сети.
     *
     * @param stateChangedListener колбек, куда будет передан результат операции
     */
    void disable(StateChangedListener stateChangedListener);

    /**
     * Устанавливает подключение к сети синхронно.
     *
     * @return {@code true}, если удалось подключить {@code false} иначе
     */
    boolean enableSync();

    /**
     * Отключает подключение к сети синхронно.
     *
     * @return {@code true}, если удалось отключить {@code false} иначе
     */
    boolean disableSync();

    /**
     * Колбек с результатом выполнения функций {@link #enable(StateChangedListener)}, {@link #disable(StateChangedListener)}.
     */
    interface StateChangedListener {
        /**
         * Срабатывает при завершении операции.
         *
         * @param enabled {@code true}, если удалось включить {@code false} иначе
         */
        void onStateChanged(boolean enabled);
    }
}
