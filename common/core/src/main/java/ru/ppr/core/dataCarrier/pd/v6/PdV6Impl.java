package ru.ppr.core.dataCarrier.pd.v6;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV6V25;

/**
 * ПД v.6.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV6Impl extends BasePdV6V25 implements PdV6 {

    public PdV6Impl() {
        super(PdVersion.V6, PdV6Structure.PD_SIZE);
    }

}