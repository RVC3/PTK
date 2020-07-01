package ru.ppr.core.dataCarrier.smartCard.passageMark.v8;

/**
 * Структура метки прохода v.8.
 *
 * @author Aleksandr Brazhkin
 */
class PassageMarkV8Structure {
    // Размер метки прохода в байтах.
    static final int PASSAGE_MARK_SIZE = 16;
    // Счетчик использования карты
    static final int COUNTER_VALUE_BYTE_INDEX = 1;
    static final int COUNTER_VALUE_BYTE_LENGTH = 2;
    // Номер турникета ПД1
    static final int PD1_TURNSTILE_NUMBER_BYTE_INDEX = 3;
    // Номер турникета ПД2
    static final int PD2_TURNSTILE_NUMBER_BYTE_INDEX = 4;
    // Станция прохода
    static final int PASSAGE_STATION_BYTE_INDEX = 5;
    static final int PASSAGE_STATION_BYTE_LENGTH = 3;
    // Направление прохода по ПД №1
    static final int PASSAGE_TYPE_FOR_PD1_BYTE_INDEX = 8;
    static final int PASSAGE_TYPE_FOR_PD1_BIT_INDEX = 7;
    // Проход по ПД1
    static final int PASSAGE_STATUS_FOR_PD1_BYTE_INDEX = 8;
    static final int PASSAGE_STATUS_FOR_PD1_BIT_INDEX = 6;
    // Дата и время прохода по ПД №1
    static final int PD1_PASSAGE_TIME_BYTE_INDEX = 8;
    static final int PD1_PASSAGE_TIME_BYTE_LENGTH = 4;
    static final int PD1_PASSAGE_TIME_BIT_INDEX = 0;
    static final int PD1_PASSAGE_TIME_BIT_LENGTH = 26;
    // Направление прохода по ПД №2
    static final int PASSAGE_TYPE_FOR_PD2_BYTE_INDEX = 12;
    static final int PASSAGE_TYPE_FOR_PD2_BIT_INDEX = 7;
    // Проход по ПД2
    static final int PASSAGE_STATUS_FOR_PD2_BYTE_INDEX = 12;
    static final int PASSAGE_STATUS_FOR_PD2_BIT_INDEX = 6;
    // Дата и время прохода по ПД №2
    static final int PD2_PASSAGE_TIME_BYTE_INDEX = 12;
    static final int PD2_PASSAGE_TIME_BYTE_LENGTH = 4;
    static final int PD2_PASSAGE_TIME_BIT_INDEX = 0;
    static final int PD2_PASSAGE_TIME_BIT_LENGTH = 26;
    // Привязка карты
    static final int BOUND_TO_PASSENGER_BYTE_INDEX = 8;
    static final int BOUND_TO_PASSENGER_BIT_INDEX = 5;
}
