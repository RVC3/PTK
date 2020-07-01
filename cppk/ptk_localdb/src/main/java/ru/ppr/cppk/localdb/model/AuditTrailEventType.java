package ru.ppr.cppk.localdb.model;

import android.support.annotation.NonNull;

/**
 * Тип события контрольного журнала.
 *
 * @author Aleksandr Brazhkin
 */
public enum AuditTrailEventType {
    /**
     * Неизвестный тип
     */
    UNKNOWN(0),
    /**
     * Продажа ПД
     */
    SALE(1),
    /**
     * Продажа ПД по доплате
     */
    SALE_WITH_ADD_PAYMENT(2),
    /**
     * Печать пробного ПД
     */
    PRINT_TEST_PD(3),
    /**
     * Аннулирование ПД
     */
    RETURN(4),
    /**
     * Печать отчета
     */
    PRINT_REPORT(5),
    /**
     * Оформление услуги
     */
    SERVICE_SALE(6),
    /**
     * Оформление штрафа
     */
    FINE_SALE(7),
    /**
     * Продажа трансфера
     */
    TRANSFER_SALE(8);

    private int code;

    AuditTrailEventType(int code) {
        this.code = code;
    }

    @NonNull
    public static AuditTrailEventType valueOf(int code) {
        for (AuditTrailEventType item : AuditTrailEventType.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }
}
