package ru.ppr.core.dataCarrier.pd.v3;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV3V11;

/**
 * ПД v.3.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV3Impl extends BasePdV3V11 implements PdV3 {

    public PdV3Impl() {
        super(PdVersion.V3, PdV3Structure.PD_SIZE);
    }
}
