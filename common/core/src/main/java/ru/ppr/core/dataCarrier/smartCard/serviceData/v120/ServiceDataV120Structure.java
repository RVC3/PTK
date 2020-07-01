package ru.ppr.core.dataCarrier.smartCard.serviceData.v120;

/**
 * Структура служебных данных v.120.
 *
 * @author Aleksandr Brazhkin
 */
class ServiceDataV120Structure {
    // Размер служебных данных в байтах.
    static final int SERVICE_DATA_SIZE = 16;
    // Флаг "Обязательность проверки документов"
    static final int MANDATORY_OF_DOC_VERIFICATION_BYTE_INDEX = 1;
    static final int MANDATORY_OF_DOC_VERIFICATION_BIT_INDEX = 3;
    // Флаг "Должность"
    static final int POST_EXISTING_FLAG_BYTE_INDEX = 1;
    static final int POST_EXISTING_FLAG_BIT_INDEX = 2;
    // Флаг "Персонифицированная"
    static final int PERSONALIZED_FLAG_BYTE_INDEX = 1;
    static final int PERSONALIZED_FLAG_BIT_INDEX = 1;
    // Тип служебной карты
    static final int CARD_TYPE_BYTE_INDEX = 1;
    static final int CARD_TYPE_BIT_INDEX = 0;
    // Порядковый номер
    static final int ORDER_NUMBER_BYTE_INDEX = 2;
    static final int ORDER_NUMBER_BYTE_LENGTH = 2;
    // Дата и время инициализации
    static final int INIT_DATE_BYTE_INDEX = 4;
    static final int INIT_DATE_BYTE_LENGTH = 4;
    // Срок действия
    static final int VALIDITY_TIME_BYTE_INDEX = 8;
    static final int VALIDITY_TIME_BYTE_LENGTH = 2;
    // Номер ключа ЭЦП
    static final int EDS_KEY_NUMBER_BYTE_INDEX = 12;
    static final int EDS_KEY_NUMBER_BYTE_LENGTH = 4;
}
