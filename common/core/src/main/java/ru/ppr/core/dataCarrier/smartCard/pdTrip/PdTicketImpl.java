package ru.ppr.core.dataCarrier.smartCard.pdTrip;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePd;
import ru.ppr.logger.Logger;

/**
 *
 * Комплексные и единые билеты
 *
 * @author Sergey Kolesnikov
 */

public class PdTicketImpl extends BasePd implements TicketMetroPd {

    private static final String TAG = Logger.makeLogTag(PdTicketImpl.class);

    /**
     *   Дата и время окончания действия услуги (минут от даты кодирования)
     */
    private int serviceExpDateTime;

    /**
     *   Число оставшихся / совершенных поездок TRIP_COUNT
     */
    private int numberRematingPerformedTrips;

    /**
     *   Активный тип билета
     */
    private int activeTicketType;


    PdTicketImpl() {
        super(PdVersion.V0, PdCommonTicketStructure.PD_SIZE);
    }


    public void setServiceExpDateTime(int serviceExpDateTime) {
        this.serviceExpDateTime = serviceExpDateTime;
    }

    @Override
    public int getServiceExpDateTime() {
        return serviceExpDateTime;
    }

    @Override
    public int getNumberRematingPerformedTrips() {
        return numberRematingPerformedTrips;
    }

    @Override
    public int getActiveTicketType() {
        return activeTicketType;
    }


    public void setNumberRematingPerformedTrips(int numberRematingPerformedTrips) {
        this.numberRematingPerformedTrips = numberRematingPerformedTrips;
    }

    public void setActiveTicketType(int activeTicketType) {
        this.activeTicketType = activeTicketType;
    }


}
