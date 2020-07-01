package ru.ppr.cppk.entity.event.model34;

/**
 * Варианты ошибок, которые могут возникать при записи данных на бск
 * Коды больше 0 взяты из перечня ошибок и используются в отчетах
 * Коды меньше 0 придуманы, и используются в процессе работы птк
 * Created by Артем on 01.03.2016.
 */
public enum WritePdToBscError {
//    01 - Нет карты (при попытки чтения или записи карта не обнаружена в зоне действия считывателя);
//    02 - Неизвестный тип карты (не удалось прочитать серийный номер с карты, неизвестная структура карты);
//    03 - Не удалось авторизоваться на БСК (не подошел ни один из ключей доступа для БСК)
//    04 - Ошибка при записи данных (таймаут, ошибки в драйверах, общий сбой в устройстве)
//    05 - Ошибка при чтении данных (таймаут, ошибки в драйверах, общий сбой в устройстве)
//    06 - Нарушение целостности БСК: служебная информация на БСК повреждена, работа с БСК невозможна.

    TRANSFER_MUST_BE_WRITTEN_TO_CARD(-9),
    TRANSFER_MUST_BE_WRITTEN_TO_PASSENGER_BOUND_CARD(-8),
    WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE(-7),
    PRINT_PD_BLOCKED_FOR_EXEMPTION(-6),
    PD_WITH_REVOKED_SIGN_KEY(-5),
    PD_WITH_INVALID_SIGN(-4),
    CARD_IN_STOP_LIST(-3),
    DATA_ERROR(-2),
    UNKNOWN(-1),
    SUCCESS(0),
    CARD_NOT_FOUND(1),
    INCORRECT_CARD_TYPE(2),
    CARDS_NOT_MATCH(2),
    WRITE_ERROR(4),
    READ_ERROR(5),
    INCORRECT_BSC_INFO(6);

    final int code;

    WritePdToBscError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static WritePdToBscError createByCode(int code) {
        for (WritePdToBscError errors : values()) {
            if (errors.code == code)
                return errors;
        }
        return UNKNOWN;
    }
}
