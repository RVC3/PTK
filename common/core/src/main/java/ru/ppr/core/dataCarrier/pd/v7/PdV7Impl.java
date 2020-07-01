package ru.ppr.core.dataCarrier.pd.v7;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdV7V18;

/**
 * ПД v.7.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV7Impl extends BasePdV7V18 implements PdV7 {

    public PdV7Impl() {
        super(PdVersion.V7, PdV7Structure.PD_SIZE);
    }

}
