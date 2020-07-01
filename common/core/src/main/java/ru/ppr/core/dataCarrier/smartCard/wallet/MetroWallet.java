package ru.ppr.core.dataCarrier.smartCard.wallet;

import java.math.BigDecimal;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Кошелек для тройки.
 *
 * @author isedoi
 */
public interface MetroWallet extends PassageMark {
    /**
     * Количество оставшихся единиц
     */
    BigDecimal getUnitsLeft();
    /**
     * Срок годности,timestamp ms
     */
    long getDateTimeEnd();
    /**
     * Срок годности,dd.MM.yyyy HH:mm
     */
    String getDateTimeEndFormat();


    boolean isValidUnitsData();
}
