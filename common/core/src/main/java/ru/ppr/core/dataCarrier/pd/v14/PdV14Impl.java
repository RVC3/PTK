package ru.ppr.core.dataCarrier.pd.v14;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV4V12V14V15;

/**
 * ПД v.14.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV14Impl extends BasePdV4V12V14V15 implements PdV14 {

    public PdV14Impl() {
        super(PdVersion.V14, PdV14Structure.PD_SIZE);
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
