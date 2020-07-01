package ru.ppr.core.dataCarrier.pd.v9;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdWithPlace;

/**
 * ПД v.9.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV9Impl extends BasePdWithPlace implements PdV9 {

    /**
     * ЭЦП
     */
    private byte[] eds;

    public PdV9Impl() {
        super(PdVersion.V9, PdV9Structure.PD_SIZE);
    }

    @Override
    public byte[] getEds() {
        return eds;
    }

    /**
     * Метод для установки ЭЦП.
     *
     * @param eds ЭЦП
     */
    public void setEds(byte[] eds) {
        this.eds = eds;
    }
}
