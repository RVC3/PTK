package ru.ppr.core.domain.model;

import android.support.annotation.Nullable;

/**
 * Поддерживаемые типы ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public enum EdsType {
    /**
     * Стабовый контроллер ЭЦП
     */
    STUB(1),
    /**
     * Контроллер ЭЦП, основанный на SFT
     */
    SFT(2);

    private final int code;

    EdsType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static EdsType valueOf(int code) {
        for (EdsType edsType : values()) {
            if (edsType.getCode() == code) {
                return edsType;
            }
        }
        return null;
    }

}
