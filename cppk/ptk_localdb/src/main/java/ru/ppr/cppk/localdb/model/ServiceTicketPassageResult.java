package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Результат прохода по ПД (контроля ПД).
 *
 * @author Aleksandr Brazhkin
 */
public enum ServiceTicketPassageResult {
    /**
     * Причина не известна
     */
    UNKNOWN(-1),
    /**
     * Билет прошел проверку
     */
    SUCCESS_PASSAGE(0),
    /**
     * Карта находится в стоп-листе
     */
    BANNED_BY_STOP_LIST_CARDS(10),
    /**
     * Неизвестная карта
     */
    UNKNOWN_CARD(50),
    /**
     * Срок действия истек
     */
    TOO_LATE(60),
    /**
     * Неверная ЭЦП
     */
    INVALID_SIGN(70),
    /**
     * Метка прохода устарела (для ПТК)
     */
    PASSMARK_OUT_OF_DATE(80),
    /**
     * Ключ ЭЦП отозван
     */
    SIGN_KEY_REVOKED(90),
    /**
     * Не предъявлен документ
     */
    DOCUMENT_NOT_PRESENT(100),
    /**
     * На служебной карте нет действительной для станции зоны
     */
    NO_VALID_ZONE(110),
    /**
     * Ошибка записи метки прохода
     */
    FAILED_TO_WRITE_PASS_MARK(120);

    private final int code;

    ServiceTicketPassageResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static ServiceTicketPassageResult valueOf(int code) {
        for (ServiceTicketPassageResult serviceTicketPassageResult : ServiceTicketPassageResult.values()) {
            if (serviceTicketPassageResult.getCode() == code) {
                return serviceTicketPassageResult;
            }
        }
        return null;
    }
}
