package ru.ppr.cppk.sync.writer.baseEntities;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.baseEntities.TicketSaleReturnEventBase;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.model.AdditionalInfoForEttWriter;
import ru.ppr.cppk.sync.writer.model.BankCardPaymentWriter;
import ru.ppr.cppk.sync.writer.model.CheckWriter;
import ru.ppr.cppk.sync.writer.model.ExemptionWriter;
import ru.ppr.cppk.sync.writer.model.FeeWriter;
import ru.ppr.cppk.sync.writer.model.LegalEntityWriter;
import ru.ppr.cppk.sync.writer.model.ParentTicketInfoWriter;
import ru.ppr.cppk.sync.writer.model.PriceWriter;
import ru.ppr.cppk.sync.writer.model.SeasonTicketWriter;
import ru.ppr.cppk.sync.writer.model.TrainInfoWriter;

/**
 * @author Grigoriy Kashka
 */
public class TicketSaleReturnEventBaseWriter extends BaseWriter<TicketSaleReturnEventBase> {

    private final ParentTicketInfoWriter parentTicketInfoWriter;
    private final LegalEntityWriter legalEntityWriter;
    private final ExemptionWriter exemptionWriter;
    private final CheckWriter checkWriter;
    private final TrainInfoWriter trainInfoWriter;
    private final SeasonTicketWriter seasonTicketWriter;
    private final AdditionalInfoForEttWriter additionalInfoForEttWriter;
    private final PriceWriter priceWriter;
    private final FeeWriter feeWriter;
    private final BankCardPaymentWriter bankCardPaymentWriter;

    public TicketSaleReturnEventBaseWriter(
            ParentTicketInfoWriter parentTicketInfoWriter,
            LegalEntityWriter legalEntityWriter,
            ExemptionWriter exemptionWriter,
            CheckWriter checkWriter,
            TrainInfoWriter trainInfoWriter,
            SeasonTicketWriter seasonTicketWriter,
            AdditionalInfoForEttWriter additionalInfoForEttWriter,
            PriceWriter priceWriter,
            FeeWriter feeWriter,
            BankCardPaymentWriter bankCardPaymentWriter) {
        this.parentTicketInfoWriter = parentTicketInfoWriter;
        this.legalEntityWriter = legalEntityWriter;
        this.exemptionWriter = exemptionWriter;
        this.checkWriter = checkWriter;
        this.trainInfoWriter = trainInfoWriter;
        this.seasonTicketWriter = seasonTicketWriter;
        this.additionalInfoForEttWriter = additionalInfoForEttWriter;
        this.priceWriter = priceWriter;
        this.feeWriter = feeWriter;
        this.bankCardPaymentWriter = bankCardPaymentWriter;
    }

    @Override
    public void writeProperties(TicketSaleReturnEventBase ticketSaleReturnEventBase, ExportJsonWriter writer) throws IOException {
        parentTicketInfoWriter.writeField("ParentTicket", ticketSaleReturnEventBase.ParentTicket, writer);
        legalEntityWriter.writeField("Carrier", ticketSaleReturnEventBase.Carrier, writer);
        exemptionWriter.writeField("Exemption", ticketSaleReturnEventBase.Exemption, writer);
        checkWriter.writeField("Check", ticketSaleReturnEventBase.Check, writer);
        writer.name("Kind").value(ticketSaleReturnEventBase.Kind);
        trainInfoWriter.writeField("TrainInfo", ticketSaleReturnEventBase.TrainInfo, writer);
        seasonTicketWriter.writeField("SeasonTicket", ticketSaleReturnEventBase.SeasonTicket, writer);
        writer.name("IsOneTimeTicket").value(ticketSaleReturnEventBase.IsOneTimeTicket);
        additionalInfoForEttWriter.writeField("AdditionalInfoForETT", ticketSaleReturnEventBase.AdditionalInfoForETT, writer);
        priceWriter.writeField("FullPrice", ticketSaleReturnEventBase.FullPrice, writer);
        feeWriter.writeField("Fee", ticketSaleReturnEventBase.Fee, writer);
        writer.name("PaymentMethod").value(ticketSaleReturnEventBase.PaymentMethod);
        bankCardPaymentWriter.writeField("BankCardPayment", ticketSaleReturnEventBase.BankCardPayment, writer);
    }
}
