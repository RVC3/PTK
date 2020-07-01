package ru.ppr.core.dataCarrier.pd.v25;

import ru.ppr.core.dataCarrier.pd.base.PdForDays;
import ru.ppr.core.dataCarrier.pd.base.PdWithExemption;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.25.
 *
 * @author Grigoriy Kashka
 */
public interface PdV25 extends PdWithoutPlace, PdWithPaymentType, PdWithExemption, PdForDays {
}
