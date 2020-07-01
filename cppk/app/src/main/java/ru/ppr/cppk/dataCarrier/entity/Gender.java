package ru.ppr.cppk.dataCarrier.entity;

/**
 * Пол в персональных данных, считанных со смарт-карты.
 */
public enum Gender {
    UNKNOWN("U", "Неизвестен"),
    MALE("F", "Мужской"),
    FEMALE("M", "Женский");

    private final String code;
    private final String description;

    Gender(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Gender getByCode(String code) {

        for (Gender gender : Gender.values()) {
            if (gender.getCode().equalsIgnoreCase(code))
                return gender;
        }

        return UNKNOWN;
    }
}
