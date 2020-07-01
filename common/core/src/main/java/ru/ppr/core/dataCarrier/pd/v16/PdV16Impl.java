package ru.ppr.core.dataCarrier.pd.v16;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV5V13V16V17;

/**
 * ПД v.16.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV16Impl extends BasePdV5V13V16V17 implements PdV16 {

    public PdV16Impl() {
        super(PdVersion.V16, PdV16Structure.PD_SIZE);
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
