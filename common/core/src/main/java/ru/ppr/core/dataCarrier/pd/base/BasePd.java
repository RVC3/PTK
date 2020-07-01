package ru.ppr.core.dataCarrier.pd.base;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базовый класс для ПД.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePd implements Pd {

    /**
     * Версия ПД
     */
    private final PdVersion version;
    /**
     * Размер ПД в байтах
     */
    private final int size;
    /**
     * Время продажи билета
     */
    private Date saleDateTime;
    /**
     * Номер ключа ЭЦП
     */
    private long edsKeyNumber;

    public BasePd(PdVersion version, int size) {
        this.version = version;
        this.size = size;
    }

    @Override
    public PdVersion getVersion() {
        return version;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    @Override
    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }
}
