package ru.ppr.core.dataCarrier.pd.v25;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV6V25;

/**
 * ПД v.25.
 *
 * @author Grigoriy Kashka
 */
public class PdV25Impl extends BasePdV6V25 implements PdV25 {

    public PdV25Impl() {
        super(PdVersion.V25, PdV25Structure.PD_SIZE);
    }

}
