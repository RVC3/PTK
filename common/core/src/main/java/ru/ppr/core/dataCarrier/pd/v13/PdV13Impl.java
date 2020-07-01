package ru.ppr.core.dataCarrier.pd.v13;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV5V13V16V17;

/**
 * ПД v.13.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV13Impl extends BasePdV5V13V16V17 implements PdV13 {

    public PdV13Impl() {
        super(PdVersion.V13, PdV13Structure.PD_SIZE);
    }
}
