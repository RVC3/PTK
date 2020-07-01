package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

import java.util.Date;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;

/**
 * Базовый класс для служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseServiceData implements ServiceData {

    /**
     * Версия служебных данных
     */
    private final ServiceDataVersion version;
    /**
     * Размер служебных данных в байтах
     */
    private final int size;
    /**
     * Время продажи билета
     */
    private Date initDateTime;
    /**
     * Номер ключа ЭЦП
     */
    private long edsKeyNumber;

    public BaseServiceData(ServiceDataVersion version, int size) {
        this.version = version;
        this.size = size;
    }

    @Override
    public ServiceDataVersion getVersion() {
        return version;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Date getInitDateTime() {
        return initDateTime;
    }

    public void setInitDateTime(Date initDateTime) {
        this.initDateTime = initDateTime;
    }

    @Override
    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }
}
