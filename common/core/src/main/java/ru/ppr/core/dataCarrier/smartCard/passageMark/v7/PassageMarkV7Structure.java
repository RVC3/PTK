package ru.ppr.core.dataCarrier.smartCard.passageMark.v7;

/**
 * Структура метки прохода v.7.
 *
 * @author Aleksandr Brazhkin
 */
class PassageMarkV7Structure {
    // Размер метки прохода в байтах.
    static final int PASSAGE_MARK_SIZE = 16;
    // Счетчик использования карты
    static final int COUNTER_VALUE_BYTE_INDEX = 1;
    static final int COUNTER_VALUE_BYTE_LENGTH = 2;
    // Номер турникета
    static final int TURNSTILE_NUMBER_BYTE_INDEX = 3;
    // Станция прохода
    static final int PASSAGE_STATION_BYTE_INDEX = 4;
    static final int PASSAGE_STATION_BYTE_LENGTH = 4;
    // Дата и время прохода
    static final int PASSAGE_TIME_BYTE_INDEX = 8;
    static final int PASSAGE_TIME_BYTE_LENGTH = 4;
    static final int PASSAGE_TIME_BIT_INDEX = 0;
    static final int PASSAGE_TIME_BIT_LENGTH = 26;
    // Направление прохода
    static final int PASSAGE_TYPE_BYTE_INDEX = 8;
    static final int PASSAGE_TYPE_BIT_INDEX = 7;
    // Номер зоны
    static final int COVERAGE_AREA_NUMBER_BYTE_INDEX = 8;
    static final int COVERAGE_AREA_NUMBER_BYTE_LENGTH = 1;
    static final int COVERAGE_AREA_NUMBER_BIT_INDEX = 2;
    static final int COVERAGE_AREA_NUMBER_BIT_LENGTH = 5;
}
