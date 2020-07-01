package ru.ppr.core.dataCarrier.pd.v10;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdWithPlace;

/**
 * ПД v.10.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV10Impl extends BasePdWithPlace implements PdV10 {

    /**
     * Даты действия ПД. Каждому биту соответствует один из дней.
     */
    private int forDays;

    public PdV10Impl() {
        super(PdVersion.V10, PdV10Structure.PD_SIZE);
    }

    @Override
    public int getForDays() {
        return forDays;
    }

    public void setForDays(int forDays) {
        this.forDays = forDays;
    }
}
