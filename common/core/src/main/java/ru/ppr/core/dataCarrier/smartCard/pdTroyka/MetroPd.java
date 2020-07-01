package ru.ppr.core.dataCarrier.smartCard.pdTroyka;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Кошелек для тройки.
 *
 * @author Kolesnikov Sergey
 */
public interface MetroPd extends Pd {
    /**
     *   Тип билета №1 CRDCODE
     */
    int getTypeTicket1();

    /**
     *   Тип билета №2 CRDCODE
     */
    int getTypeTicket2();


    /**
     *   Дата и время последнего кодирования
     * DateTime.Now
     */
    int getDateTimeNow();

    /**
     * Метка валидности форматов данных
     */
    boolean isValidFormatData();

}