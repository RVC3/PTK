package ru.ppr.chit.domain.model.nsi;

/**
 * Тип устройства на полигоне. 2015-07-17 был переименован из StationDeviceType
 * по согласованию с Горбушиным, в соответствии с 26й версией датаконтрактов.
 */
public enum DeviceType {

    /**
     * Стационарная касса
     */
    SK(1),
    /**
     * Билетопечатающий автомат
     */
    BPA(2),
    /**
     * ПТК
     */
    PTK(3),
    /**
     * Турникет
     */
    TURNSTILE(4),
    /**
     * Валидатор
     */
    VALIDATOR(5);

    private final int code;

    DeviceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DeviceType valueOf(int code) {
        for (DeviceType type : DeviceType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}
