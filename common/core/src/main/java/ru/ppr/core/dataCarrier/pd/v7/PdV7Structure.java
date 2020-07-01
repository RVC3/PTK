package ru.ppr.core.dataCarrier.pd.v7;

/**
 * Структура ПД v.7.
 *
 * @author Aleksandr Brazhkin
 */
class PdV7Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 22;
    // Размер данных ПД в байтах
    static final int PD_DATA_SIZE = 16;
    // Порядковый номер
    static final int ORDER_NUMBER_BYTE_INDEX = 1;
    static final int ORDER_NUMBER_BYTE_LENGTH = 3;
    static final int ORDER_NUMBER_BIT_INDEX = 6;
    static final int ORDER_NUMBER_BIT_LENGTH = 18;
    // Сроки
    static final int START_DAY_OFFSET_BYTE_INDEX = 3;
    static final int START_DAY_OFFSET_BIT_INDEX = 1;
    static final int START_DAY_OFFSET_BIT_LENGTH = 5;
    // Способ оплаты
    static final int PAYMENT_TYPE_BYTE_INDEX = 3;
    static final int PAYMENT_TYPE_BIT_INDEX = 0;
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 4;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Тариф
    static final int TARIFF_BYTE_INDEX = 8;
    static final int TARIFF_BYTE_LENGTH = 4;
    // Стартовое значение счетчика
    static final int START_COUNTER_VALUE_BYTE_INDEX = 12;
    static final int START_COUNTER_VALUE_BYTE_LENGTH = 2;
    // Конечное значение счетчика
    static final int END_COUNTER_VALUE_BYTE_INDEX = 14;
    static final int END_COUNTER_VALUE_BYTE_LENGTH = 2;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 16;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
    // Контрольная сумма
    static final int CRC_BYTE_INDEX = 20;
    static final int CRC_BYTE_LENGTH = 2;

}
