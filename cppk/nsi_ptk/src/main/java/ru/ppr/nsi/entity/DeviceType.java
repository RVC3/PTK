package ru.ppr.nsi.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Тип устройства на полигоне. 2015-07-17 был переименован из StationDeviceType
 * по согласованию с Горбушиным, в соответствии с 26й версией датаконтрактов.
 */
public enum DeviceType {

    Unknown(0, "Неизвестен") {
    },

    /**
     * Стационарная касса
     */
    @SerializedName("1")
    Sk(1, "Стационарная касса") {
    },

    /**
     * Билетопечатающий автомат
     */
    @SerializedName("2")
    Bpa(2, "БПА") {
    },

    /**
     * ПТК
     */
    @SerializedName("3")
    Ptk(3, "ПТК") {
    },

    /**
     * Турникет
     */
    @SerializedName("4")
    Turnstile(4, "Турникет") {
    },

    /**
     * Валидатор
     */
    @SerializedName("5")
    Validator(5, "Валидатор") {
    };

    /**
     * Числовой код (id)
     */
    private int code;

    /**
     * Описание
     */
    private String description;

    public int getCode() {
        return code;
    }

    static public DeviceType getType(int code) {
        for (DeviceType type : DeviceType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return DeviceType.Unknown;
    }

    DeviceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * description of DeviceType
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

}
