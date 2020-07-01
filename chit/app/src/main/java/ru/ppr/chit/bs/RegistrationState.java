package ru.ppr.chit.bs;

/**
 * Состояние регистрации на базовой станции.
 *
 * @author Aleksandr Brazhkin
 */
public enum RegistrationState {
    /**
     * Не готов к работе (нет базы ключей и нси)
     */
    NOT_PREPARED,
    /**
     * Зарегистрирован на БС и имеет валидный токен авторизации
     */
    REGISTERED,
    /**
     * Был зарегистрирован на БС, но токен авторизации уже не валиден
     */
    REGISTERED_BROKEN,
    /**
     * Нет регистрирации на БС, нет токена авторизации
     */
    NOT_REGISTERED;
}
