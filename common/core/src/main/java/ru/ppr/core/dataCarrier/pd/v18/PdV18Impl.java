package ru.ppr.core.dataCarrier.pd.v18;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV7V18;

/**
 * ПД v.18.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV18Impl extends BasePdV7V18 implements PdV18 {

    public PdV18Impl() {
        super(PdVersion.V18, PdV18Structure.PD_SIZE);
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
