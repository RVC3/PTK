package ru.ppr.core.dataCarrier.smartCard.wallet;

/**
 * Структура сектора кошелек.
 * @author isedoi
 */
class TroykaWalletStructure {
    // Размер метки прохода в байтах.
    static final int SECTOR_SIZE = 48;
    // Тип билета.
    static final int TICKET_TYPE_BLOCK = 10;
    static final int TICKET_TYPE_SIZE = 10;
    // Формат кодирования
    public static final int CODING_FORMAT_VALUE = 14;//стандартное значение
    static final int CODING_FORMAT_BLOCK = 52;
    static final int CODING_FORMAT_SIZE = 4;
    //  Расширение номера формата
   public static final int EXTEND_NUM_FORMAT_VALUE = 5;//стандартное значение
    static final int EXTEND_NUM_FORMAT_BLOCK = 56;
    static final int EXTEND_NUM_FORMAT_SIZE = 5;
    // Срок годности, дней от 31.12.2018.
    static final int END_TIME_BLOCK = 61;
    static final int END_TIME_SIZE = 13;
    // Счетчик пополнений.
    static final int FILL_COUNTER_BLOCK = 107;
    static final int FILL_COUNTER_SIZE = 10;
    // Время начала поездки, минут с 00:00 31.12.2018
    static final int TRIP_START_TIME_BLOCK = 128;
    static final int TRIP_START_TIME_SIZE = 23;
    // Сколько прошло минут с начала прохода.
    static final int AFTER_ENTER_TIME_BLOCK = 158;
    static final int AFTER_ENTER_TIME_SIZE = 7;
    //Осталось единиц 0.00 с копейками
    static final int UNITS_LEFT_BLOCK = 165;
    static final int UNITS_LEFT_SIZE = 21;
}
