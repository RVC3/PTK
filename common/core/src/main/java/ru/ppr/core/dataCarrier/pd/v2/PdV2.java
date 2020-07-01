package ru.ppr.core.dataCarrier.pd.v2;

import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithEds;
import ru.ppr.core.dataCarrier.pd.base.PdWithExtraPayment;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.2.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV2 extends PdWithoutPlace, PdWithEds, PdWithPaymentType, PdWithDirection, PdWithExtraPayment {
}
