package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.SeasonTicket;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.Price;

/**
 * Created by Артем on 16.12.2015.
 */
public class TicketSaleReturnEventBaseGenerator extends AbstractGenerator
        implements Generator<TicketSaleReturnEventBase> {

    private Boolean isTicketWritten;

    private AdditionalInfoForEtt additionalInfoForEtt = null;

    private ParentTicketInfo parentTicket;

    private LegalEntity legalEntity;

    private ExemptionForEvent exemption = null;

    private TicketKind kind;

    private TrainInfo trainInfo = null;

    private SeasonTicket seasonTicket;

    private Boolean isOneTimeTicket;

    private Price fullPrice;

    private Fee fee;

    private PaymentType paymentMethod;

    private BankTransactionEvent bankTransactionEvent;

    private TicketEventBase ticketEventBase;

    public TicketSaleReturnEventBaseGenerator setTicketWritten(boolean ticketWritten) {
        isTicketWritten = ticketWritten;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setAdditionalInfoForEtt(AdditionalInfoForEtt additionalInfoForEtt) {
        this.additionalInfoForEtt = additionalInfoForEtt;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setParentTicket(ParentTicketInfo parentTicket) {
        this.parentTicket = parentTicket;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setLegalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setExemption(ExemptionForEvent exemption) {
        this.exemption = exemption;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setKind(TicketKind kind) {
        this.kind = kind;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setTrainInfo(TrainInfo trainInfo) {
        this.trainInfo = trainInfo;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setSeasonTicket(SeasonTicket seasonTicket) {
        this.seasonTicket = seasonTicket;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setOneTimeTicket(boolean oneTimeTicket) {
        isOneTimeTicket = oneTimeTicket;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setFullPrice(Price fullPrice) {
        this.fullPrice = fullPrice;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setFee(Fee fee) {
        this.fee = fee;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setPaymentMethod(PaymentType paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setBankTransactionEvent(BankTransactionEvent bankTransactionEvent) {
        this.bankTransactionEvent = bankTransactionEvent;
        return this;
    }

    public TicketSaleReturnEventBaseGenerator setTicketEventBase(TicketEventBase ticketEventBase) {
        this.ticketEventBase = ticketEventBase;
        return this;
    }

    @NonNull
    @Override
    public TicketSaleReturnEventBase build() {
        checkNotNull(kind, "TicketKind is null");
        checkNotNull(ticketEventBase, "TicketEventBase is null");
        checkNotNull(paymentMethod, "PaymentMethod is null");
        checkNotNull(isOneTimeTicket, "IsOnTimeTicket is null");
        checkNotNull(fullPrice, "FullPrice is null");
        checkNotNull(legalEntity, "LegalEntity is null");
        checkNotNull(isTicketWritten, "IsTicketWritten is null");
        checkNotNull(trainInfo, "TrainInfo is null");

        TicketSaleReturnEventBase ticketSaleReturnEventBase = new TicketSaleReturnEventBase();
        ticketSaleReturnEventBase.setKind(kind);
        ticketSaleReturnEventBase.setTicketEventBaseId(ticketEventBase.getId());
        ticketSaleReturnEventBase.setFullPriceId(fullPrice.getId());
        ticketSaleReturnEventBase.setLegalEntityId(legalEntity.getId());
        ticketSaleReturnEventBase.setPaymentMethod(paymentMethod);
        long feeId = -1;
        if (fee != null) {
            feeId = fee.getId() == null ? -1 : fee.getId();
        }
        ticketSaleReturnEventBase.setFeeId(feeId);
        ticketSaleReturnEventBase.setTicketWritten(isTicketWritten);
        ticketSaleReturnEventBase.setTrainInfoId(trainInfo.getId());
        ticketSaleReturnEventBase.setOneTimeTicket(isOneTimeTicket);

        ticketSaleReturnEventBase.setAdditionalInfoForEttId(additionalInfoForEtt == null ? -1 : additionalInfoForEtt.getId());
        ticketSaleReturnEventBase.setSeasonTicketId(seasonTicket == null ? -1 : seasonTicket.getId());
        ticketSaleReturnEventBase.setBankTransactionEventId(bankTransactionEvent == null ? -1 : bankTransactionEvent.getId());
        ticketSaleReturnEventBase.setParentTicketInfoId(parentTicket == null ? -1 : parentTicket.getId());
        ticketSaleReturnEventBase.setExemptionForEventId(exemption == null ? -1 : exemption.getId());

        return ticketSaleReturnEventBase;
    }
}
