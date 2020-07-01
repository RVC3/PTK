package ru.ppr.cppk.ui.activity.transfersale.model;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * Данные для процесса оформления трансфера.
 *
 * @author Dmitry Nevolin
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
     * Тарифный план
     */
    private TariffPlan tariffPlan;
    /**
     * Направление
     */
    private TicketWayType direction;
    /**
     * Список тарифов "Туда".
     */
    private List<Tariff> tariffsThere;
    /**
     * Список тарифов "Обратно"
     */
    private List<Tariff> tariffsBack;
    /**
     * Сбор
     */
    private ProcessingFee processingFee;
    /**
     * Флаг "Взимать сбор"
     */
    private boolean includeFee = true;
    /**
     * Тип платежа
     */
    private PaymentType paymentType;
    /**
     * Информация о родительском билете
     */
    private ParentTicketInfo parentTicketInfo;
    /**
     * Тип связи с родительским билетом (трансфер, доплата)
     */
    private ConnectionType connectionType;
    /**
     * Дата начала действия ПД
     */
    private Date startDate;
    /**
     * Дата окончания действия ПД
     */
    private Date endDate;
    /**
     * Смещение даты начала действия ПД с момента продажи (в днях)
     */
    private int startDayOffset;

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

    public TariffPlan getTariffPlan() {
        return tariffPlan;
    }

    public void setTariffPlan(TariffPlan tariffPlan) {
        this.tariffPlan = tariffPlan;
    }

    public TicketWayType getDirection() {
        return direction;
    }

    public void setDirection(TicketWayType direction) {
        this.direction = direction;
    }

    public List<Tariff> getTariffsThere() {
        return tariffsThere;
    }

    public void setTariffsThere(List<Tariff> tariffsThere) {
        this.tariffsThere = tariffsThere;
    }

    public List<Tariff> getTariffsBack() {
        return tariffsBack;
    }

    public void setTariffsBack(List<Tariff> tariffsBack) {
        this.tariffsBack = tariffsBack;
    }

    public ProcessingFee getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(ProcessingFee processingFee) {
        this.processingFee = processingFee;
    }

    public boolean isIncludeFee() {
        return includeFee;
    }

    public void setIncludeFee(boolean includeFee) {
        this.includeFee = includeFee;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public ParentTicketInfo getParentTicketInfo() {
        return parentTicketInfo;
    }

    public void setParentTicketInfo(ParentTicketInfo parentTicketInfo) {
        this.parentTicketInfo = parentTicketInfo;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(int startDayOffset) {
        this.startDayOffset = startDayOffset;
    }
}
