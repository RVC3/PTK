package ru.ppr.core.dataCarrier.pd.v11;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV3V11;

/**
 * ПД v.11.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV11Impl extends BasePdV3V11 implements PdV11 {

    public PdV11Impl() {
        super(PdVersion.V11, PdV11Structure.PD_SIZE);
    }
}
