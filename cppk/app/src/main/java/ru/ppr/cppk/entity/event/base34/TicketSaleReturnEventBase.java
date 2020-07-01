package ru.ppr.cppk.entity.event.base34;

import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.localdb.model.PaymentType;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class TicketSaleReturnEventBase {

    /**
     * У всех необязательных полей ид представлен в виде объекта, т.к. объект по умолчанию == null,
     * в то время как примитивный тип == 0, а локальной БД ид начинаются с 0, и поэтому
     * возникает ситуация что ид == 0, но объекта в БД еще нет,
     * либо он не имеет отношения к данной записи.
     */

    /**
     * локальный id текущего события TicketSaleReturnEventBase
     */
    private long id = -1;

    /**
     * Первичный ключ для таблицы TicketEventBase.
     */
    private long ticketEventBaseId = -1;

    //true - если билет записан на БСК
    private boolean isTicketWritten;

    /**
     * Дополнительная информация по ЭТТ локальный идентификатор
     */
    private long additionalInfoForEttId = -1;

    //По умолчанию ид должен быть -1, т.к. ид в БД начинаются с 0
    private long parentTicketInfoId = -1;

    private long legalEntityId = -1;

    /**
     * Локальный идентификалор в БД, для льготы в событии.
     */
    private long exemptionForEventId = -1;

    /**
     * Первичный ключ для таблицы Check
     * (Сменное событие, в рамках которого случилось это событие)
     * По умолчанию ид должен быть -1, т.к. ид в БД начинаются с 0
     */
    private long checkId = -1;

    ///     Тип билета (полный, льготный, детский)
    private TicketKind kind;

    /**
     * Первичный ключ для таблицы TrainInfo.
     * Информация о поезде
     */
    private long trainInfoId = -1;

    /**
     * Первичный ключ для таблицы SeasonTicket.
     */
    private long seasonTicketId = -1;

    private boolean isOneTimeTicket;

    /**
     * Первичный ключ для таблицы Price (Полная стоимость ПД)
     */
    private long fullPriceId;

    /**
     * Первичный ключ для таблицы Fee (Информация о сборе в случае его взимания)
     */
    private long feeId = -1;

    /// Код способа оплаты
    /// Соответствует коду способа оплаты в справочнике НСИ «Способы оплаты».
    private PaymentType paymentMethod;

    /**
     * Локальный идентификатор события транзакции на терминале
     */
    private long bankTransactionEventId = -1;

    public long getTicketEventBaseId() {
        return ticketEventBaseId;
    }

    public void setTicketEventBaseId(long ticketEventBaseId) {
        this.ticketEventBaseId = ticketEventBaseId;
    }

    public Long getParentTicketInfoId() {
        return parentTicketInfoId;
    }

    public void setParentTicketInfoId(long parentTicketInfoId) {
        this.parentTicketInfoId = parentTicketInfoId;
    }

    public boolean isTicketWritten() {
        return isTicketWritten;
    }

    public void setTicketWritten(boolean ticketWritten) {
        isTicketWritten = ticketWritten;
    }

    public TicketKind getKind() {
        return kind;
    }

    public void setKind(TicketKind kind) {
        this.kind = kind;
    }

    public boolean isOneTimeTicket() {
        return isOneTimeTicket;
    }

    public void setOneTimeTicket(boolean oneTimeTicket) {
        isOneTimeTicket = oneTimeTicket;
    }

    public PaymentType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getFullPriceId() {
        return fullPriceId;
    }

    public void setFullPriceId(long fullPriceId) {
        this.fullPriceId = fullPriceId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getAdditionalInfoForEttId() {
        return additionalInfoForEttId;
    }

    public void setAdditionalInfoForEttId(long additionalInfoForEttId) {
        this.additionalInfoForEttId = additionalInfoForEttId;
    }

    public long getLegalEntityId() {
        return legalEntityId;
    }

    public void setLegalEntityId(long legalEntityId) {
        this.legalEntityId = legalEntityId;
    }

    public Long getExemptionForEventId() {
        return exemptionForEventId;
    }

    public void setExemptionForEventId(long exemptionForEventId) {
        this.exemptionForEventId = exemptionForEventId;
    }

    public long getCheckId() {
        return checkId;
    }

    public void setCheckId(long checkId) {
        this.checkId = checkId;
    }

    public Long getSeasonTicketId() {
        return seasonTicketId;
    }

    public void setSeasonTicketId(long seasonTicketId) {
        this.seasonTicketId = seasonTicketId;
    }

    public long getTrainInfoId() {
        return trainInfoId;
    }

    public void setTrainInfoId(long trainInfoId) {
        this.trainInfoId = trainInfoId;
    }

    public long getFeeId() {
        return feeId;
    }

    public void setFeeId(long feeId) {
        this.feeId = feeId;
    }

    public Long getBankTransactionEventId() {
        return bankTransactionEventId;
    }

    public void setBankTransactionEventId(long bankTransactionEventId) {
        this.bankTransactionEventId = bankTransactionEventId;
    }
}
