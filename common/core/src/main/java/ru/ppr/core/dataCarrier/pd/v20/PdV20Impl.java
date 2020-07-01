package ru.ppr.core.dataCarrier.pd.v20;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV19V20V23V24;

/**
 * ПД v.20.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV20Impl extends BasePdV19V20V23V24 implements PdV20 {

    public PdV20Impl() {
        super(PdVersion.V20, PdV20Structure.PD_SIZE);
    }
}
