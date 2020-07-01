package ru.ppr.core.dataCarrier.pd.v15;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV4V12V14V15;

/**
 * ПД v.15.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV15Impl extends BasePdV4V12V14V15 implements PdV15 {

    public PdV15Impl() {
        super(PdVersion.V15, PdV15Structure.PD_SIZE);
    }

    /**
     * Возвращает порядковый номер чека восстановления
     *
     * @return порядковый номер чека восстановления
     */
    @Override
    public int getOrderNumber() {
        return super.getOrderNumber();
    }

    /**
     * Возвращает восстановленную дату и время оформления исходного ПД
     *
     * @return восстановленную дату и время оформления исходного ПД
     */
    @Override
    public Date getSaleDateTime() {
        return super.getSaleDateTime();
    }

}
