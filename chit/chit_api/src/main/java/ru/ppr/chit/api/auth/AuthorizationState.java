package ru.ppr.chit.api.auth;

/**
 * Состояние авторизации на БС.
 *
 * @author m.sidorov
 */
public enum AuthorizationState {
    /**
     * Не авторизован на базовой станции (нет токена авторизации)
     */
    NOT_AUTHORIZED,
    /**
     * Авторизован на базовой станции (есть валидный токен авторизации)
     */
    AUTHORIZED,
    /**
     * Не авторизован (битый токен авторизации)
     */
    AUTHORIZED_BROKEN
}
