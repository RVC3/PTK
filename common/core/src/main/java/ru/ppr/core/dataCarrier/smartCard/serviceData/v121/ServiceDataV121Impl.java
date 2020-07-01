package ru.ppr.core.dataCarrier.smartCard.serviceData.v121;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.BaseServiceDataV120V121;

/**
 * Служебные данные v.121.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV121Impl extends BaseServiceDataV120V121 implements ServiceDataV121 {

    public ServiceDataV121Impl() {
        super(ServiceDataVersion.V121, ServiceDataV121Structure.SERVICE_DATA_SIZE);
    }
}
