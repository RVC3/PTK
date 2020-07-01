package ru.ppr.core.dataCarrier.pd.v20;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.20.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV20 extends PdWithoutPlace, PdWithPaymentType {


    /**
     * Возвращает порядковый номер чека восстановления
     *
     * @return порядковый номер чека восстановления
     */
    @Override
    int getOrderNumber();

    /**
     * Возвращает восстановленную дату и время оформления исходного ПД
     *
     * @return восстановленную дату и время оформления исходного ПД
     */
    @Override
    Date getSaleDateTime();
}
