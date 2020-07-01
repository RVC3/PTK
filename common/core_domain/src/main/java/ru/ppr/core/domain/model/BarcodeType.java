package ru.ppr.core.domain.model;

import android.support.annotation.Nullable;

/**
 * @author Dmitry Nevolin
 */
public enum BarcodeType {

    /**
     *
     */
    FILE(1),
    /**
     *
     */
    MDI3100(2);

    private final int code;

    BarcodeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static BarcodeType valueOf(int code) {
        for (BarcodeType barcodeType : values()) {
            if (barcodeType.getCode() == code) {
                return barcodeType;
            }
        }
        return null;
    }
}
