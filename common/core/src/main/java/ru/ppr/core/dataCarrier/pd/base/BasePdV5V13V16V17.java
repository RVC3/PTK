package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.5, v.13, v16, v17.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdV5V13V16V17 extends BasePdWithoutPlace implements PdWithDirection, PdWithExemption {

    /**
     * Направление
     */
    @PdWithDirection.Direction
    private int direction;
    /**
     * Код льготы
     */
    private int exemptionCode;

    public BasePdV5V13V16V17(PdVersion version, int size) {
        super(version, size);
    }

    @PdWithDirection.Direction
    @Override
    public int getDirection() {
        return direction;
    }

    public void setDirection(@PdWithDirection.Direction int direction) {
        this.direction = direction;
    }

    @Override
    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }
}
