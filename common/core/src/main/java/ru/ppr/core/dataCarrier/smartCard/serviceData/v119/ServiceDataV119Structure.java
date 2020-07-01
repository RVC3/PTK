package ru.ppr.core.dataCarrier.smartCard.serviceData.v119;

/**
 * Структура служебных данных v.119.
 *
 * @author Aleksandr Brazhkin
 */
class ServiceDataV119Structure {
    // Размер служебных данных в байтах.
    static final int SERVICE_DATA_SIZE = 16;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_INDEX = 1;
    static final int EDS_KEY_NUMBER_LENGTH = 4;
    // Дата и время инициализации
    static final int INIT_DATE_INDEX = 5;
    static final int INIT_DATE_LENGTH = 4;
}
