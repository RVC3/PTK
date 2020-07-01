package ru.ppr.core.dataCarrier.pd.v69;

/**
 * Структура ПД v.69.
 *
 * @author isedoi
 */
 class PdV69Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 48;//три блока
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 1;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 5;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
}
