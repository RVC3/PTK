package ru.ppr.core.dataCarrier.pd.v7;

import ru.ppr.core.dataCarrier.pd.base.PdWithCounter;
import ru.ppr.core.dataCarrier.pd.base.PdWithCrc;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.7.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV7 extends PdWithoutPlace, PdWithPaymentType, PdWithCounter, PdWithCrc {
}
