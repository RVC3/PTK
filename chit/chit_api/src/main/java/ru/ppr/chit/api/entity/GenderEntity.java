package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Пол человека.
 *
 * @author Aleksandr Brazhkin
 */
public enum GenderEntity {

    /**
     * Женский
     */
    @SerializedName("1")
    FEMALE(1),
    /**
     * Мужской
     */
    @SerializedName("2")
    MALE(2);

    private final int code;

    GenderEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static GenderEntity valueOf(int code) {
        for (GenderEntity gender : GenderEntity.values()) {
            if (gender.getCode() == code) {
                return gender;
            }
        }
        return null;
    }
}
