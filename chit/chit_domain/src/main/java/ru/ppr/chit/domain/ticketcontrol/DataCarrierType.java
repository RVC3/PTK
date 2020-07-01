package ru.ppr.chit.domain.ticketcontrol;

import android.support.annotation.Nullable;

/**
 * Тип источника данных.
 *
 * @author Aleksandr Brazhkin
 */
public enum DataCarrierType {
    /**
     * Оформлен в виде штрихкода
     */
    BARCODE(1),
    /**
     * Оформлен на БСК
     */
    SMART_CARD(2),
    /**
     * Выбран из списка с базовой станции
     */
    TICKET_LIST(3);

    private final int code;

    DataCarrierType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static DataCarrierType valueOf(int code) {
        for (DataCarrierType dataCarrierType : DataCarrierType.values()) {
            if (dataCarrierType.getCode() == code) {
                return dataCarrierType;
            }
        }
        return null;
    }
}