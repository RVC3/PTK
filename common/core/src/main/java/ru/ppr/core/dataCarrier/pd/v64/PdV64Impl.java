package ru.ppr.core.dataCarrier.pd.v64;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePd;

/**
 * ПД v.64.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV64Impl extends BasePd implements PdV64 {

    public PdV64Impl() {
        super(PdVersion.V64, PdV64Structure.PD_SIZE);
    }

}
