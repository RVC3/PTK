package ru.ppr.core.dataCarrier.pd.v23;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV19V20V23V24;

/**
 * ПД v.23.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV23Impl extends BasePdV19V20V23V24 implements PdV23 {

    public PdV23Impl() {
        super(PdVersion.V23, PdV23Structure.PD_SIZE);
    }
}
