package ru.ppr.core.dataCarrier.pd.v4;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV4V12V14V15;

/**
 * ПД v.4.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV4Impl extends BasePdV4V12V14V15 implements PdV4 {

    public PdV4Impl() {
        super(PdVersion.V4, PdV4Structure.PD_SIZE);
    }
}
