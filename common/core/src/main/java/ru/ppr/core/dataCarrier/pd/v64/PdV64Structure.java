package ru.ppr.core.dataCarrier.pd.v64;

/**
 * Структура ПД v.64.
 *
 * @author Aleksandr Brazhkin
 */
class PdV64Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 9;
    // Размер данных ПД в байтах
    static final int PD_DATA_SIZE = 5;
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 1;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 5;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
}
