package ru.ppr.core.dataCarrier.smartCard.passageMark.troyka;

/**
 * Структура метки прохода v.6.
 *
 * @author isedoi
 */
public class PassageMarkTroykaStructure {
    // Размер метки прохода в байтах.
    static final int PASSAGE_MARK_SIZE = 16;
    // Экспресс-3 код станции отправления (последние пять цифр)
    static final int PASSAGE_STATION_INCOME_BYTE_INDEX = 16;
    static final int PASSAGE_STATION_INCOME_BYTE_LENGTH = 20;
    // Экспресс-3 код станции назначения (последние пять цифр)
    static final int PASSAGE_STATION_OUTCOME_BYTE_INDEX = 36;
    static final int PASSAGE_STATION_OUTCOME_BYTE_LENGTH = 20;
    // Тип поезда
    static final int PASSAGE_TRAIN_TYPE_BYTE_INDEX = 0;
    static final int PASSAGE_TRAIN_TYPE_BYTE_LENGTH = 4;
    // Тип билета
    static final int PASSAGE_TICKET_TYPE_BYTE_INDEX = 4;
    static final int PASSAGE_TICKET_TYPE_BYTE_LENGTH = 4;
    // Тип метки
    static final int PASSAGE_MARK_TYPE_BYTE_INDEX = 8;
    static final int PASSAGE_MARK_TYPE_BYTE_LENGTH = 8;
    // Количество минут с начала года проход через турникет
    static final int PASSAGE_TIME_INTERSECT_BYTE_INDEX = 56;
    static final int PASSAGE_TIME_INTERSECT_BYTE_LENGTH = 24;
    // Номер устройства
    static final int PASSAGE_TURNIKET_NUMBER_BYTE_INDEX = 80;
    static final int PASSAGE_TURNIKET_NUMBER_BYTE_LENGTH = 16;
    // Имитовставка
    static final int PASSAGE_IMITOVSTAVKA_BYTE_INDEX = 96;
    static final int PASSAGE_IMITOVSTAVKA_BYTE_LENGTH = 32;
}
