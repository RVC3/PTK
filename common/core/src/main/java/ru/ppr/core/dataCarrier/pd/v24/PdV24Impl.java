package ru.ppr.core.dataCarrier.pd.v24;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV19V20V23V24;

/**
 * ПД v.24.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV24Impl extends BasePdV19V20V23V24 implements PdV24 {

    public PdV24Impl() {
        super(PdVersion.V24, PdV24Structure.PD_SIZE);
    }
}
