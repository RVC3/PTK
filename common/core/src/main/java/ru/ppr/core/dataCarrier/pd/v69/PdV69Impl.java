package ru.ppr.core.dataCarrier.pd.v69;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePd;
import ru.ppr.core.dataCarrier.pd.v64.PdV64;

/**
 * ПД v.69.
 *
 * @author isedoi
 */
public class PdV69Impl extends BasePd implements PdV69 {

    public PdV69Impl() {
        super(PdVersion.V64, PdV69Structure.PD_SIZE);
    }

}
