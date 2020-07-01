package ru.ppr.core.dataCarrier.smartCard.pdTrip;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;

/**
 * Декодер комплексные и единые билеты
 *
 * @author Sergey Kolesnikov
 */
public class PdTicketDecoder implements PdDecoder {

    @Nullable
    @Override
    public TicketMetroPd decode(@NonNull byte[] data) {

        if (data.length < PdCommonTicketStructure.PD_DATA_SIZE)
            return null;

        int serviceExpDateTime = DataCarrierUtils.getValue(data, PdCommonTicketStructure.SERVICE_EXP_DATE_TIME_INDEX, PdCommonTicketStructure.SERVICE_EXP_DATE_TIME_LEGHT);
        int numberRematingPerformedTrips = DataCarrierUtils.getValue(data, PdCommonTicketStructure.NUMBER_REMAINING_PERFORMED_TRIPS_INDEX, PdCommonTicketStructure.NUMBER_REMAINING_PERFORMED_TRIPS_LEGHT);
        int activeTicketType = DataCarrierUtils.getValue(data, PdCommonTicketStructure.ACTIVE_TICKET_TYPE_INDEX, PdCommonTicketStructure.ACTIVE_TICKET_TYPE_LEGHT);

        PdTicketImpl pdTicketImpl = new PdTicketImpl();
        pdTicketImpl.setServiceExpDateTime(serviceExpDateTime);
        pdTicketImpl.setNumberRematingPerformedTrips(numberRematingPerformedTrips);
        pdTicketImpl.setActiveTicketType(activeTicketType);
        return pdTicketImpl;
    }

}
