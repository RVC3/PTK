package ru.ppr.cppk.pd;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.cppk.entity.utils.builders.events.FeeBuilder;
import ru.ppr.cppk.entity.utils.builders.events.PriceBuilder;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;

/**
 * Created by Dmitry Nevolin on 12.01.2016.
 */
public class DataSalePD {

    /*
     * Смещение даты начала действия ПД с момента продажи (в днях)
     * По умолчанию билет действует с сегодняшнего дня
     */
    private int term = 0;
    /**
     * Тип платежа
     */
    private PaymentType paymentType = PaymentType.INDIVIDUAL_CASH;
    /**
     * Информация о льготе для события
     */
    private ExemptionForEvent exemptionForEvent;
    /**
     * Инфомрация о льготе из НСИ
     */
    private Exemption exemption;
    /**
     * Направление
     */
    private TicketWayType direction = TicketWayType.OneWay;
    /**
     * Станция отправления
     */
    private Station departureStation;
    /**
     * Станция назначения
     */
    private Station destinationStation;
    /**
     * Тариф "Туда"
     */
    private Tariff tariffThere;
    /**
     * Тариф "Обратно"
     */
    private Tariff tariffBack;
    /**
     * Флаг, добавить сбор
     */
    private boolean includeFee = true;
    /**
     * Тарифный план
     */
    private TariffPlan tariffPlan;
    /**
     * Тип билета
     */
    private TicketType ticketType;
    /**
     * Категория поезда
     */
    private TrainCategory trainCategory;
    /**
     * Информация о билете, по которому выполняется доплата
     */
    private ParentTicketInfo parentTicketInfo;
    /**
     * Доп. информация по ЭТТ (в случае чтения с карты)
     */
    private AdditionalInfoForEtt additionalInfoForEttFromCard;
    /**
     * Доп. информация по ЭТТ (в случае ручного ввода льготы и даты выпуска)
     */
    private AdditionalInfoForEtt additionalInfoForEttManualEntryDateIssue;
    /**
     * Сбор
     */
    private ProcessingFee processingFee;
    /**
     * Смарт-карта, на которую записывается билет
     */
    private SmartCard smartCard;
    /**
     * Банковская транзакция
     */
    private BankTransactionEvent bankTransactionEvent;
    /**
     * Время продажи билета
     */
    private Date saleDateTime;
    /**
     * Тип носителя билета
     */
    private TicketStorageType ticketStorageType;
    /**
     * Номер документа
     */
    private int PDNumber;
    /**
     * Стоимость билета
     */
    private BigDecimal ticketCostValueWithoutDiscount = BigDecimal.ZERO;
    /**
     * Налоговая ставка на стоиость билета
     */
    private int ticketCostVatValue;
    /**
     * Тип билета (трансфер, доплата)
     */
    private ConnectionType connectionType;

    /**
     * Печатаем билет, либо пишем на карту.
     * Этот параметр нужен, т.к. возможна ситуация что при записи на бск оказалось что карта
     * переполнена и нужно напечатать билет.
     */
    private boolean isTicketWritten;

    /**
     * Ошибка, которая произошла во время записи на бск. Может быть null если ошибок не было
     */
    private WritePdToBscError writeError;
    /**
     * Событие считывания талона ТППД
     */
    private CouponReadEvent couponReadEvent;
    /**
     * Данные для отправки электронного билета
     */
    private ETicketDataParams ETicketDataParams;
    /**
     * Дата начала действия ПД
     */
    public Date startDate;
    /**
     * Дата окончания действия ПД
     */
    public Date endDate;

    public ETicketDataParams getETicketDataParams() {
        return ETicketDataParams;
    }

    public void setETicketDataParams(ETicketDataParams ETicketDataParams) {
        this.ETicketDataParams = ETicketDataParams;
    }

    public boolean isTicketWritten() {
        return isTicketWritten;
    }

    public void setTicketWritten(boolean ticketWritten) {
        isTicketWritten = ticketWritten;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public ExemptionForEvent getExemptionForEvent() {
        return exemptionForEvent;
    }

    public void setExemptionForEvent(ExemptionForEvent exemptionForEvent) {
        this.exemptionForEvent = exemptionForEvent;
    }

    public Exemption getExemption() {
        return exemption;
    }

    public void setExemption(Exemption exemption) {
        this.exemption = exemption;
    }

    public TicketWayType getDirection() {
        return direction;
    }

    public void setDirection(TicketWayType direction) {
        this.direction = direction;
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

    public Tariff getTariffThere() {
        return tariffThere;
    }

    public void setTariffThere(Tariff tariffThere) {
        this.tariffThere = tariffThere;
    }

    public Tariff getTariffBack() {
        return tariffBack;
    }

    public void setTariffBack(Tariff tariffBack) {
        this.tariffBack = tariffBack;
    }

    public boolean isIncludeFee() {
        return includeFee;
    }

    public void setIncludeFee(boolean includeFee) {
        this.includeFee = includeFee;
    }

    public TariffPlan getTariffPlan() {
        return tariffPlan;
    }

    public void setTariffPlan(TariffPlan tariffPlan) {
        this.tariffPlan = tariffPlan;
    }

    public int getPDNumber() {
        return PDNumber;
    }

    public void setPDNumber(int PDNumber) {
        this.PDNumber = PDNumber;
    }

    public SmartCard getSmartCard() {
        return smartCard;
    }

    public void setSmartCard(SmartCard smartCard) {
        this.smartCard = smartCard;
    }

    public BankTransactionEvent getBankTransactionEvent() {
        return bankTransactionEvent;
    }

    public void setBankTransactionEvent(BankTransactionEvent bankTransactionEvent) {
        this.bankTransactionEvent = bankTransactionEvent;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public TrainCategory getTrainCategory() {
        return trainCategory;
    }

    public void setTrainCategory(TrainCategory trainCategory) {
        this.trainCategory = trainCategory;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public ParentTicketInfo getParentTicketInfo() {
        return parentTicketInfo;
    }

    public void setParentTicketInfo(ParentTicketInfo parentTicketInfo) {
        this.parentTicketInfo = parentTicketInfo;
    }

    public AdditionalInfoForEtt getAdditionalInfoForEttFromCard() {
        return additionalInfoForEttFromCard;
    }

    public void setAdditionalInfoForEttFromCard(AdditionalInfoForEtt additionalInfoForEtt) {
        this.additionalInfoForEttFromCard = additionalInfoForEtt;
    }

    public void setAdditionalInfoForEttManualEntryDateIssue(AdditionalInfoForEtt additionalInfoForEtt) {
        this.additionalInfoForEttManualEntryDateIssue = additionalInfoForEtt;
    }

    public AdditionalInfoForEtt getAdditionalInfoForEtt() {
        //http://agile.srvdev.ru/browse/CPPKPP-37471
        return additionalInfoForEttFromCard == null ? additionalInfoForEttManualEntryDateIssue : additionalInfoForEttFromCard;
    }

    public ProcessingFee getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(ProcessingFee processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getPaymentSum() {
        return getTotalCostValueWithDiscount();
    }

    /**
     * Стоимость билета по тарифу без сбора, без учёта скидки
     *
     * @return
     */
    public BigDecimal getTicketCostValueWithoutDiscount() {
        return ticketCostValueWithoutDiscount;
    }

    public void setTicketCostValueWithoutDiscount(BigDecimal ticketCostValueWithoutDiscount) {
        this.ticketCostValueWithoutDiscount = ticketCostValueWithoutDiscount;
    }

    public BigDecimal getSumForReturn() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getTicketCostVatRate() {
        BigDecimal rate = BigDecimal.ZERO;

        if (ticketType != null && ticketType.getTax() != null)
            rate = ticketType.getTax();

        return rate;
    }

    public BigDecimal getTicketCostVatValue() {
        return Decimals.getVATValueIncludedFromRate(getTicketCostValueWithDiscount(), getTicketCostVatRate(), Decimals.RoundMode.HUNDREDTH);
    }

    public BigDecimal getTicketCostLossSum() {
        return getTicketCostValueWithoutDiscount().subtract(getTicketCostValueWithDiscount());
    }

    public BigDecimal getFeeValue() {
        return includeFee && processingFee != null ? processingFee.getTariff() : BigDecimal.ZERO;
    }

    public FeeType getFeeType() {
        return includeFee && processingFee != null ? processingFee.getFeeType() : null;
    }

    public BigDecimal getFeeVatValue() {
        return includeFee && processingFee != null ? processingFee.getTax() : BigDecimal.ZERO;
    }

    public BigDecimal getFeeVatRate() {
        return Decimals.getVATRateIncludedFromValue(getFeeValue(), getFeeVatValue(), Decimals.RoundMode.WITHOUT);
    }

    /**
     * Стоимость билета по тарифу без сбора, с учётом скидки
     *
     * @return
     */
    public BigDecimal getTicketCostValueWithDiscount() {
        int percentage = 0;

        if (exemption != null)
            percentage = exemption.getPercentage();

        //округляем в соответствии с "Сначала высчитываем стоимость билета со скидкой, округляем (15.25 -> округляем -> 15.30)"
        //взято из таска https://aj.srvdev.ru/browse/CPPKPP-24054
        return ticketCostValueWithoutDiscount == null ?
                BigDecimal.ZERO :
                Decimals.round(
                        ticketCostValueWithoutDiscount.subtract(Decimals.percentage(new BigDecimal(percentage), ticketCostValueWithoutDiscount, Decimals.RoundMode.WITHOUT)),
                        Decimals.RoundMode.TENTH);
    }

    /**
     * Полная стоимость билета с тарифом и сбором, но без скидки
     *
     * @return
     */
    public BigDecimal getTotalCostValueWithoutDiscount() {
        return getFeeValue().add(getTicketCostValueWithoutDiscount());
    }

    /**
     * Полная стоимость билета с тарифом, сбором и скидкой
     *
     * @return
     */
    public BigDecimal getTotalCostValueWithDiscount() {
        return getFeeValue().add(getTicketCostValueWithDiscount());
    }

    /**
     * Признак платного билета
     *
     * @return
     */
    public boolean isMustTakeMoney() {
        return getTotalCostValueWithDiscount().compareTo(BigDecimal.ZERO) != 0;
    }

    public Price getPrice() {

        BigDecimal payed = getPaymentSum(); //сколько всего денег положили на ФР
        BigDecimal full = payed; //сколько всего денег получили от покупателя
        BigDecimal sumForReturn = full.subtract(payed); //сдача
        BigDecimal nds = getTicketCostVatValue().add(getFeeVatValue()); //НДС от (суммы билета - льгота) + НДС от сбора

        Price price = new PriceBuilder()
                .setFull(full)
                .setPayed(payed)
                .setNds(Decimals.round(nds, Decimals.RoundMode.HUNDREDTH))
                .setSumForReturn(sumForReturn)
                .build();

        return price;
    }

    public Fee getFee() {
        Fee fee = null;
        if (isIncludeFee() && getProcessingFee() != null) {
            fee = new FeeBuilder()
                    .setTotal(getFeeValue())
                    .setNds(getFeeVatValue())
                    .setFeeType(getFeeType())
                    .build();
        }
        return fee;
    }

    /**
     * Стоимость билета по тарифу юез сбора и без скидок.
     *
     * @return
     */
    public BigDecimal getFullTicketPrice() {
        return getTicketCostValueWithoutDiscount();
    }


    public void setWriteError(WritePdToBscError writeError) {
        this.writeError = writeError;
    }

    public WritePdToBscError getWriteError() {
        return writeError;
    }

    public CouponReadEvent getCouponReadEvent() {
        return couponReadEvent;
    }

    public void setCouponReadEvent(CouponReadEvent couponReadEvent) {
        this.couponReadEvent = couponReadEvent;
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

    @Override
    public String toString() {
        return "DataSalePD{" +
                "term=" + term +
                ",\npaymentType=" + paymentType +
                ",\nexemptionForEvent=" + exemptionForEvent +
                ",\nexemption=" + exemption +
                ",\ndirection=" + direction +
                ",\ndepartureStation=" + departureStation +
                ",\ndestinationStation=" + destinationStation +
                ",\ntariffThere=" + tariffThere +
                ",\ntariffBack=" + tariffBack +
                ",\nincludeFee=" + includeFee +
                ",\ntariffPlan=" + tariffPlan +
                ",\nticketType=" + ticketType +
                ",\ntrainCategory=" + trainCategory +
                ",\nparentTicketInfo=" + parentTicketInfo +
                ",\nadditionalInfoForEttFromCard=" + additionalInfoForEttFromCard +
                ",\nadditionalInfoForEttManualEntryDateIssue=" + additionalInfoForEttManualEntryDateIssue +
                ",\nprocessingFee=" + processingFee +
                ",\nsmartCard=" + smartCard +
                ",\nbankTransactionEvent=" + bankTransactionEvent +
                ",\nsaleDateTime=" + saleDateTime +
                ",\nticketStorageType=" + ticketStorageType +
                ",\nPDNumber=" + PDNumber +
                ",\nticketCostValueWithoutDiscount=" + ticketCostValueWithoutDiscount +
                ",\nticketCostVatValue=" + ticketCostVatValue +
                ",\nconnectionType=" + connectionType +
                ",\nisTicketWritten=" + isTicketWritten +
                ",\nwriteError=" + writeError +
                ",\ncouponReadEvent=" + couponReadEvent +
                ",\nETicketDataParams=" + ETicketDataParams +
                ",\nstartDate=" + startDate +
                ",\nendDate=" + endDate +
                '}';
    }
}
