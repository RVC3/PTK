package ru.ppr.core.dataCarrier.pd.v1;

import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithEds;
import ru.ppr.core.dataCarrier.pd.base.PdWithExemption;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.1.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV1 extends PdWithoutPlace, PdWithExemption, PdWithEds, PdWithPaymentType, PdWithDirection {
}
