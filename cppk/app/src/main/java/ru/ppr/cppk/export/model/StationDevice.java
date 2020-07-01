package ru.ppr.cppk.export.model;

/**
 * @author Grigoriy Kashka
 */
public class StationDevice {

    public String id;

    /**
     * Текстовое представление модели (например, "FPrint-55K")
     */
    public String model;

    /**
     * Серийный номер устройства
     */
    public String serialNumber;

    /**
     * Код типа терминального устройства
     * DeviceType
     * 3 - ПТК
     */
    public String type;

    /**
     * Код участка
     */
    public int productionSectionCode;

    @Override
    public String toString() {
        return "StationDevice{" +
                "id='" + id + '\'' +
                ", model='" + model + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", type=" + type +
                ", productionSectionCode=" + productionSectionCode +
                '}';
    }
}
