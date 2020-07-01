package ru.ppr.core.dataCarrier.pd.v19;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV19V20V23V24;

/**
 * ПД v.19.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV19Impl extends BasePdV19V20V23V24 implements PdV19 {

    public PdV19Impl() {
        super(PdVersion.V19, PdV19Structure.PD_SIZE);
    }
}
