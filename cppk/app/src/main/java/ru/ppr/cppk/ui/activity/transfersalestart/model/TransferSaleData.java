package ru.ppr.cppk.ui.activity.transfersalestart.model;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * Данные для процесса оформления трансфера.
 *
 * @author Aleksandr Brazhkin
 */
@ActivityScope
public class TransferSaleData {
    /**
     * Станция отправления
     */
    private Station departureStation;
    /**
     * Станция назначения
     */
    private Station destinationStation;
    /**
     * Тип билета
     */
    private TicketType ticketType;
    /**
     * Тарифные планы
     */
    private List<TariffPlan> tariffPlans;
    /**
     * Список тарифов "Туда".
     */
    private List<Tariff> tariffsThere;

    @Inject
    TransferSaleData() {

    }

    public Station getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public List<TariffPlan> getTariffPlans() {
        return tariffPlans;
    }

    public void setTariffPlans(List<TariffPlan> tariffPlans) {
        this.tariffPlans = tariffPlans;
    }

    public List<Tariff> getTariffsThere() {
        return tariffsThere;
    }

    public void setTariffsThere(List<Tariff> tariffsThere) {
        this.tariffsThere = tariffsThere;
    }
}
