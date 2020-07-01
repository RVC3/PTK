package ru.ppr.chit.domain.model.local;

import android.support.annotation.Nullable;

/**
 * Пол человека.
 *
 * @author Aleksandr Brazhkin
 */
public enum Gender {
    /**
     * Мужской
     */
    MALE(0),
    /**
     * Женский
     */
    FEMALE(1);

    private final int code;

    Gender(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static Gender valueOf(int code) {
        for (Gender gender : Gender.values()) {
            if (gender.getCode() == code) {
                return gender;
            }
        }
        return null;
    }
}
