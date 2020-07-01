package ru.ppr.cppk.ui.activity.decrementtrip.model;

/**
 * Ошибка списания поездки.
 *
 * @author Aleksandr Brazhkin
 */
public enum DecrementTripError {
    /**
     * Неверные входные данные
     */
    INVALID_PARAMS,
    /**
     * Карта не найдена
     */
    CARD_NOT_FOUND,
    /**
     * Приложена другая картв
     */
    OTHER_CARD,
    /**
     * Ошибка чтения значения счетчика
     */
    COUNTER_VALUE_NOT_READ,
    /**
     * Некорректное значение счетчика
     */
    INVALID_COUNTER_VALUE,
    /**
     * Ошибка инкремента значения счтетчика
     */
    COUNTER_VALUE_NOT_INCREMENTED,
    /**
     * Ошибка чтения метки прохода
     */
    PASSAGE_MARK_NOT_READ,
    /**
     * Некорректное значение метки прохода
     */
    INVALID_PASSAGE_MARK,
    /**
     * Ошибка записи метки прохода
     */
    PASSAGE_MARK_NOT_WRITTEN,
}
