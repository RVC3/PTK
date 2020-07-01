package ru.ppr.core.dataCarrier.pd.v2;

/**
 * Структура ПД v.2.
 *
 * @author Aleksandr Brazhkin
 */
class PdV2Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 91;
    // Размер данных ПД в байтах
    static final int PD_DATA_SIZE = 23;
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
    // Порядковый номер исходного ПД
    static final int SOURCE_ORDER_NUMBER_BYTE_INDEX = 4;
    static final int SOURCE_ORDER_NUMBER_BYTE_LENGTH = 3;
    static final int SOURCE_ORDER_NUMBER_BIT_INDEX = 6;
    static final int SOURCE_ORDER_NUMBER_BIT_LENGTH = 18;
    // Дата и время доплаты
    static final int SALE_DATE_TIME_BYTE_INDEX = 7;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Дата и время продажи исходного ПД
    static final int SOURCE_SALE_DATE_TIME_BYTE_INDEX = 11;
    static final int SOURCE_SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Тариф доплаты
    static final int TARIFF_BYTE_INDEX = 15;
    static final int TARIFF_BYTE_LENGTH = 4;
    // ID устройства исходного ПД
    static final int SOURCE_PD_DEVICE_ID_BYTE_INDEX = 19;
    static final int SOURCE_PD_DEVICE_ID_BYTE_LENGTH = 4;
    // Номер ключа ЭЦП доплаты
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 23;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
    // ЭЦП
    static final int EDS_BYTE_INDEX = 27;
    static final int EDS_BYTE_LENGTH = 64;
}
