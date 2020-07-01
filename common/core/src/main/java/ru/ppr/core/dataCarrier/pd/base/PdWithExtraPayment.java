package ru.ppr.core.dataCarrier.pd.base;

import java.util.Date;

/**
 * ПД по доплате.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithExtraPayment extends Pd {
    /**
     * Метод для получения порядкого номера исходного ПД.
     *
     * @return порядковый номер исходного ПД.
     */
    int getSourceOrderNumber();

    /**
     * Метод для получения даты и времени продажи исходного ПД.
     *
     * @return дата и время продажи исходного ПД.
     */
    Date getSourceSaleDateTime();

    /**
     * Метод для получения ID оборудования, сформировавшего исходный ПД.
     *
     * @return ID оборудования, сформировавшего исходный ПД.
     */
    long getSourceDeviceId();
}
