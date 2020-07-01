package ru.ppr.core.dataCarrier.smartCard.passageMark;

/**
 * Версии меток прохода.
 *
 * @author Aleksandr Brazhkin
 */
public enum PassageMarkVersion {

    V0(0),
    V4(4),
    V5(5),
    V6(6),
    V7(7),
    V8(8);

    private final int code;

    PassageMarkVersion(int code) {
        this.code = code;
    }

    /**
     * Вовращает код метки прохода
     * @return код метки прохода
     */
    public int getCode() {
        return code;
    }

    /**
     * Возвращает объект метки прохода по коду или null если код незвестен
     * @param code код метки прохода
     * @return объект метки прохода по коду или null если код незвестен
     */
    public static PassageMarkVersion getByCode(int code) {
        for (PassageMarkVersion passageMarkVersion : PassageMarkVersion.values()) {
            if (passageMarkVersion.getCode() == code) {
                return passageMarkVersion;
            }
        }
        return null;
    }
}
