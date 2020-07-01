package ru.ppr.core.dataCarrier.pd.v5;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV5V13V16V17;

/**
 * ПД v.5.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV5Impl extends BasePdV5V13V16V17 implements PdV5 {

    public PdV5Impl() {
        super(PdVersion.V5, PdV5Structure.PD_SIZE);
    }
}
