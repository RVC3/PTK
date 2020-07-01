package ru.ppr.core.dataCarrier.smartCard.serviceData.v119;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.BaseServiceData;

/**
 * Служебные данные v.119.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV119Impl extends BaseServiceData implements ServiceDataV119 {

    public ServiceDataV119Impl() {
        super(ServiceDataVersion.V119, ServiceDataV119Structure.SERVICE_DATA_SIZE);
    }
}
