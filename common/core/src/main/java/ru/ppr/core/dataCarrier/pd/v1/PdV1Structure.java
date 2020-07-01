package ru.ppr.core.dataCarrier.pd.v1;

/**
 * Структура ПД v.1.
 *
 * @author Aleksandr Brazhkin
 */
class PdV1Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 82;
    // Размер данных ПД в байтах
    static final int PD_DATA_SIZE = 14;
    // Порядковый номер
    static final int ORDER_NUMBER_BYTE_INDEX = 1;
    static final int ORDER_NUMBER_BYTE_LENGTH = 3;
    static final int ORDER_NUMBER_BIT_INDEX = 6;
    static final int ORDER_NUMBER_BIT_LENGTH = 18;
    // Сроки
    static final int START_DAY_OFFSET_BYTE_INDEX = 3;
    static final int START_DAY_OFFSET_BIT_INDEX = 2;
    static final int START_DAY_OFFSET_BIT_LENGTH = 4;
    // Направление
    static final int DIRECTION_BYTE_INDEX = 3;
    static final int DIRECTION_BIT_INDEX = 0;
    // Способ оплаты
    static final int PAYMENT_TYPE_BYTE_INDEX = 3;
    static final int PAYMENT_TYPE_BIT_INDEX = 1;
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 4;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Тариф
    static final int TARIFF_BYTE_INDEX = 8;
    static final int TARIFF_BYTE_LENGTH = 4;
    // Код льготы
    static final int EXEMPTION_BYTE_INDEX = 12;
    static final int EXEMPTION_BYTE_LENGTH = 2;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 14;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
    // ЭЦП
    static final int EDS_BYTE_INDEX = 18;
    static final int EDS_BYTE_LENGTH = 64;
}
