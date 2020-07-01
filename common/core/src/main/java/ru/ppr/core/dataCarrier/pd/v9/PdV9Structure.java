package ru.ppr.core.dataCarrier.pd.v9;

/**
 * Структура ПД v.9.
 *
 * @author Aleksandr Brazhkin
 */
class PdV9Structure {
    // Размер ПД в байтах
    static final int PD_SIZE = 124;
    static final int PD_DATA_SIZE = 56;
    // Порядковый номер
    static final int ORDER_NUMBER_BYTE_INDEX = 1;
    static final int ORDER_NUMBER_BYTE_LENGTH = 3;
    static final int ORDER_NUMBER_BIT_INDEX = 6;
    static final int ORDER_NUMBER_BIT_LENGTH = 18;
    // Номер поезда
    static final int TRAIN_NUMBER_BYTE_INDEX = 3;
    static final int TRAIN_NUMBER_BYTE_LENGTH = 2;
    static final int TRAIN_NUMBER_BIT_INDEX = 0;
    static final int TRAIN_NUMBER_BIT_LENGTH = 14;
    // Литера поезда
    static final int TRAIN_LETTER_BYTE_INDEX = 5;
    // Место
    static final int PLACE_NUMBER_BYTE_INDEX = 6;
    // Литера места
    static final int PLACE_LETTER_BYTE_INDEX = 7;
    // Блок вагона, даты и времени отправления
    static final int CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_INDEX = 8;
    static final int CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_LENGTH = 3;
    // Вагон
    static final int CARRIAGE_NUMBER_BIT_SHIFT = 18;
    // Дата отправления
    static final int DEPARTURE_DAY_OFFSET_BIT_SHIFT = 11;
    static final int DEPARTURE_DAY_OFFSET_BIT_MASK = 0x7F;
    // Время отправления
    static final int DEPARTURE_TIME_BIT_MASK = 0x7FF;
    // Тип документа
    static final int DOCUMENT_TYPE_CODE_BYTE_INDEX = 11;
    // Номер документа
    static final int DOCUMENT_NUMBER_BYTE_INDEX = 12;
    static final int DOCUMENT_NUMBER_BYTE_LENGTH = 4;
    // Станция отправления
    static final int DEPARTURE_STATION_BYTE_INDEX = 16;
    static final int DEPARTURE_STATION_BYTE_LENGTH = 3;
    // Станция прибытия
    static final int DESTINATION_STATION_BYTE_INDEX = 19;
    static final int DESTINATION_STATION_BYTE_LENGTH = 3;
    // Вид билета
    static final int TICKET_TYPE_BYTE_INDEX = 22;
    static final int TICKET_TYPE_BYTE_LENGTH = 2;
    // Фамилия
    static final int LAST_NAME_BYTE_INDEX = 24;
    static final int LAST_NAME_BYTE_LENGTH = 24;
    // Инициал "Имя"
    static final int FIRST_NAME_INITIAL_BYTE_INDEX = 48;
    // Инициал "Отчество"
    static final int SECOND_NAME_INITIAL_BYTE_INDEX = 49;
    // Код льготы
    static final int EXEMPTION_BYTE_INDEX = 50;
    static final int EXEMPTION_BYTE_LENGTH = 2;
    // Дата и время продажи
    static final int SALE_DATE_TIME_BYTE_INDEX = 52;
    static final int SALE_DATE_TIME_BYTE_LENGTH = 4;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 56;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
    // Ключ ЭЦП
    static final int EDS_KEY_BYTE_INDEX = 60;
    static final int EDS_KEY_BYTE_LENGTH = 64;
}
