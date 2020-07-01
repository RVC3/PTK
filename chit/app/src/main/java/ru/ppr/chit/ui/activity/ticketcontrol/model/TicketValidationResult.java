package ru.ppr.chit.ui.activity.ticketcontrol.model;

/**
 * Результат проверки ПД.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketValidationResult {
    /**
     * Успешно
     */
    SUCCESS,
    /**
     * ПД предположительно валиден, посадка разрешена по усмотрению проводника
     */
    PROBABLY_SUCCESS_BY_DATE,
    /**
     * Неверные реквизиты билета
     */
    INVALID_DATA,
    /**
     * Неверная ЭЦП
     */
    INVALID_EDS_KEY,
    /**
     * Отозван ключ ЭЦП
     */
    REVOKED_EDS_KEY,
    /**
     * ПД аннулирован
     */
    CANCELLED,
    /**
     * ПД возвращен
     */
    RETURNED
}