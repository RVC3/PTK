package ru.ppr.core.dataCarrier.pd.v6;

import ru.ppr.core.dataCarrier.pd.base.PdForDays;
import ru.ppr.core.dataCarrier.pd.base.PdWithExemption;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.6.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV6 extends PdWithoutPlace, PdWithPaymentType, PdWithExemption, PdForDays {
}
