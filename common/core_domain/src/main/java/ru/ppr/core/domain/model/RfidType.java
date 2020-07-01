package ru.ppr.core.domain.model;

import android.support.annotation.Nullable;

/**
 * @author Dmitry Nevolin
 */
public enum RfidType {

    /**
     *
     */
    FILE(1),
    /**
     *
     */
    REAL(2);

    private final int code;

    RfidType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static RfidType valueOf(int code) {
        for (RfidType rfidType : values()) {
            if (rfidType.getCode() == code) {
                return rfidType;
            }
        }
        return null;
    }

}
