package ru.ppr.core.dataCarrier.smartCard.pdTrip;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 *
 * @author Kolesnikov Sergey
 */
public interface TicketMetroPd extends Pd {
    /**
     *   Дата и время окончания действия услуги (минут от даты кодирования)
     */
    int getServiceExpDateTime();

    /**
     *   Число оставшихся / совершенных поездок TRIP_COUNT
     */
    int getNumberRematingPerformedTrips();

    /**
     *   Активный тип билета
     */
    int getActiveTicketType();



}