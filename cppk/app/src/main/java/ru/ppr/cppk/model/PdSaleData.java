package ru.ppr.cppk.model;

import android.util.Pair;

import java.util.List;

import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSaleData {
    /**
     * Станция отправления
     */
    private Station departureStation;
    /**
     * Станция назначения
     */
    private Station destinationStation;
    /**
     * Тарифный план
     */
    private TariffPlan tariffPlan;
    /**
     * Тип билета
     */
    private TicketType ticketType;
    /**
     * Список тарифов "Туда".
     */
    private List<Tariff> tariffsThere;
    /**
     * Список тарифов "Обратно"
     */
    private List<Tariff> tariffsBack;
    /**
     * Тип платежа
     */
    private PaymentType paymentType;
    /**
     * Информация о льготах для всех тарифов
     */
    List<Pair<ExemptionForEvent,Exemption>> exemptions;
    /**
     * Направление
     */
    private TicketWayType direction;
    /**
     * Сбор
     */
    private ProcessingFee processingFee;
    /**
     * Флаг "Взимать сбор"
     */
    private boolean includeFee = true;
    /**
     * Данные для отправки электронного билета
     */
    private ETicketDataParams eTicketDataParams;
    /**
     * Количество ПД
     */
    private int pdCount;
    /**
     * Событие считывания талона ТППД
     */
    private CouponReadEvent couponReadEvent;
    /**
     * Доп. информация по ЭТТ
     */
    private AdditionalInfoForEtt additionalInfoForEtt;

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

    public TariffPlan getTariffPlan() {
        return tariffPlan;
    }

    public void setTariffPlan(TariffPlan tariffPlan) {
        this.tariffPlan = tariffPlan;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
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

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public List<Pair<ExemptionForEvent, Exemption>> getExemptions() {
        return exemptions;
    }

    public void setExemptions(List<Pair<ExemptionForEvent, Exemption>> exemptions) {
        this.exemptions = exemptions;
    }

    public TicketWayType getDirection() {
        return direction;
    }

    public void setDirection(TicketWayType direction) {
        this.direction = direction;
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

    public ETicketDataParams geteTicketDataParams() {
        return eTicketDataParams;
    }

    public void seteTicketDataParams(ETicketDataParams eTicketDataParams) {
        this.eTicketDataParams = eTicketDataParams;
    }

    public int getPdCount() {
        return pdCount;
    }

    public void setPdCount(int pdCount) {
        this.pdCount = pdCount;
    }

    public CouponReadEvent getCouponReadEvent() {
        return couponReadEvent;
    }

    public void setCouponReadEvent(CouponReadEvent couponReadEvent) {
        this.couponReadEvent = couponReadEvent;
    }

    public AdditionalInfoForEtt getAdditionalInfoForEtt() {
        return additionalInfoForEtt;
    }

    public void setAdditionalInfoForEtt(AdditionalInfoForEtt additionalInfoForEtt) {
        this.additionalInfoForEtt = additionalInfoForEtt;
    }
}
