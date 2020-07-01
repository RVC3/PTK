package ru.ppr.core.dataCarrier.smartCard.serviceData.v121;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithFlags;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithOrderNumber;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithValidityTime;

/**
 * Служебные данные v.121.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataV121 extends ServiceData,
        ServiceDataWithOrderNumber,
        ServiceDataWithValidityTime,
        ServiceDataWithFlags {
}
