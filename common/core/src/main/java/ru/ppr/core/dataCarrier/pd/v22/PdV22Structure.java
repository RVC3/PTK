package ru.ppr.core.dataCarrier.pd.v22;

/**
 * Структура ПД v.22.
 *
 * @author Aleksandr Brazhkin
 */
class PdV22Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 88;
    // Размер данных ПД в байтах
    static final int PD_DATA_SIZE = 20;
    // Порядковый номер
    static final int ORDER_NUMBER_BYTE_INDEX = 1;
    static final int ORDER_NUMBER_BYTE_LENGTH = 3;
    static final int ORDER_NUMBER_BIT_INDEX = 6;
    static final int ORDER_NUMBER_BIT_LENGTH = 18;
    // Направление
    static final int DIRECTION_BYTE_INDEX = 3;
    static final int DIRECTION_BIT_INDEX = 0;
    // Способ оплаты
    static final int PAYMENT_TYPE_BYTE_INDEX = 3;
    static final int PAYMENT_TYPE_BIT_INDEX = 1;
    // Признак - требуется проверка входа на другой станции при выходе на текущей станции (1 - да, 0 - нет)
    static final int PASSAGE_TO_STATION_CHECK_REQUIRED_BYTE_INDEX = 3;
    static final int PASSAGE_TO_STATION_CHECK_REQUIRED_BIT_INDEX = 2;
    // Признак - требуется активация билета (с помощью считывания специального ШК на ИТ на станции отправления, 1 - да, 0 - нет)
    static final int ACTIVATION_REQUIRED_BYTE_INDEX = 3;
    static final int ACTIVATION_REQUIRED_BIT_INDEX = 3;
    // Номер телефона
    static final int PHONE_NUMBER_BYTE_INDEX = 4;
    static final int PHONE_NUMBER_BYTE_LENGTH = 5;
    // Сроки
    static final int START_DAY_OFFSET_BYTE_INDEX = 9;
    static final int START_DAY_OFFSET_BYTE_LENGTH = 1;
    // Код льготы
    static final int EXEMPTION_BYTE_INDEX = 10;
    static final int EXEMPTION_BYTE_LENGTH = 2;
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 12;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Тариф
    static final int TARIFF_BYTE_INDEX = 16;
    static final int TARIFF_BYTE_LENGTH = 4;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 20;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
    // ЭЦП
    static final int EDS_BYTE_INDEX = 24;
    static final int EDS_BYTE_LENGTH = 64;
}
