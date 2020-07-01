package ru.ppr.core.dataCarrier.pd.v18;

import ru.ppr.core.dataCarrier.pd.base.PdWithCounter;
import ru.ppr.core.dataCarrier.pd.base.PdWithCrc;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.18.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV18 extends PdWithoutPlace, PdWithPaymentType, PdWithCounter, PdWithCrc {
}
