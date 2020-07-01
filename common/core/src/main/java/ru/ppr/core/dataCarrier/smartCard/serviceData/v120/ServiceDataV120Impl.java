package ru.ppr.core.dataCarrier.smartCard.serviceData.v120;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.BaseServiceDataV120V121;

/**
 * Служебные данные v.120.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV120Impl extends BaseServiceDataV120V121 implements ServiceDataV120 {

    public ServiceDataV120Impl() {
        super(ServiceDataVersion.V120, ServiceDataV120Structure.SERVICE_DATA_SIZE);
    }
}
