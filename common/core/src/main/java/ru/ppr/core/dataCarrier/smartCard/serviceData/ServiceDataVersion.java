package ru.ppr.core.dataCarrier.smartCard.serviceData;

/**
 * Версии служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public enum ServiceDataVersion {

    V119(119),
    V120(120),
    V121(121);

    private final int code;

    ServiceDataVersion(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ServiceDataVersion getByCode(int code) {
        for (ServiceDataVersion serviceDataVersion : ServiceDataVersion.values()) {
            if (serviceDataVersion.getCode() == code) {
                return serviceDataVersion;
            }
        }
        return null;
    }
}
