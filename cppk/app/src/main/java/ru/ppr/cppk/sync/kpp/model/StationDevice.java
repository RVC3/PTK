package ru.ppr.cppk.sync.kpp.model;

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
    public Integer type;

    /**
     * Код участка
     */
    public int productionSectionCode;

}
