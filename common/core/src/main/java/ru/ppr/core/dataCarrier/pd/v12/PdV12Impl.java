package ru.ppr.core.dataCarrier.pd.v12;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV4V12V14V15;

/**
 * ПД v.12.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV12Impl extends BasePdV4V12V14V15 implements PdV12 {

    public PdV12Impl() {
        super(PdVersion.V12, PdV12Structure.PD_SIZE);
    }
}
