package ru.ppr.cppk.sync.kpp.model;

/**
 * Кассовый аппарат
 * @author Grigoriy Kashka
 */
public class CashRegister {

    /**
     * Текстовое представление модели (например, "FPrint-55K") 
     */
    public String model;

    /**
     * Серийный номер устройства
     */
    public String serialNumber;

    /**
     * Номер ИНН (если устройство фискализированно)
     */
    public String inn;

    /**
     * Серийный номер ЭКЛЗ
     */
    public String eklzNumber;

    /**
     * Номер ФН
     */
    public String fnSerial;
}
