package ru.ppr.core.dataCarrier.smartCard.serviceData.v120;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithFlags;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithOrderNumber;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithValidityTime;

/**
 * Служебные данные v.120.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataV120 extends ServiceData,
        ServiceDataWithOrderNumber,
        ServiceDataWithValidityTime,
        ServiceDataWithFlags {
}
