package ru.ppr.cppk.entity;

import android.content.Context;

import ru.ppr.cppk.utils.ErrorFactory;

public enum AuthResult {
    /**
     * авторизация успешна
     */
    SUCCESS,
    /**
     * пользователь с такой учетной записью не найден
     */
    USER_NOT_FOUND,
    /**
     * карта с таким ид не найдена в бд
     */
    CARD_NOT_FOUND,
    /**
     * время действия роли истекло, либо еще не наступило
     */
    CARD_AND_DATE_EXPIRED,
    /**
     * пароль записаный на бск не совпадает с паролем записанным на карте
     */
    INVALID_PASSWORD,
    /**
     * не найдена роль для данного участка
     */
    INVALID_ROLE,
    /**
     * не найдена роль для данного участка, свежая карта, пользователя еще нет в БД
     */
    INVALID_ROLE_NEW_CARD,
    /**
     * ошибка проверки эцп
     */
    ECP_ERROR,
    /**
     * текущее время меньше времени записи карты
     */
    INVALID_TIME,
    /**
     * карта не активна
     */
    CARD_NOT_ACTIVE,
    /**
     * Авторизация для данной роли запрещена
     */
    AuthDasabledForThisRole;

    public String getDescription(Context c) {
        return ErrorFactory.getAuthError(c, this);
    }

    /**
     * Вернет флажек можно ли при такой ошибке разрешить пользователю ввести пин повторно
     * @return
     */
    public boolean isReEnterPinEnabled() {
        if(this==USER_NOT_FOUND) return true;
        return false;
    }
}
