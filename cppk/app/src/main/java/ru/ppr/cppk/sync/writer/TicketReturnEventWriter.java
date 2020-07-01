package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.CPPKTicketReturn;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.TicketEventBaseWriter;
import ru.ppr.cppk.sync.writer.baseEntities.TicketSaleReturnEventBaseWriter;
import ru.ppr.cppk.sync.writer.model.AdditionalInfoForEttWriter;
import ru.ppr.cppk.sync.writer.model.BankCardPaymentWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.CheckWriter;
import ru.ppr.cppk.sync.writer.model.ExemptionWriter;
import ru.ppr.cppk.sync.writer.model.FeeWriter;
import ru.ppr.cppk.sync.writer.model.LegalEntityWriter;
import ru.ppr.cppk.sync.writer.model.ParentTicketInfoWriter;
import ru.ppr.cppk.sync.writer.model.PriceWriter;
import ru.ppr.cppk.sync.writer.model.SeasonTicketWriter;
import ru.ppr.cppk.sync.writer.model.SmartCardWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.TariffWriter;
import ru.ppr.cppk.sync.writer.model.TrainInfoWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class TicketReturnEventWriter extends BaseWriter<CPPKTicketReturn> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;
    private final TicketSaleReturnEventBaseWriter ticketSaleReturnEventBaseWriter;
    private final TicketEventBaseWriter ticketEventBaseWriter;
    private final BankCardPaymentWriter bankCardPaymentWriter;

    public TicketReturnEventWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        SmartCardWriter smartCardWriter = new SmartCardWriter(dateFormatter);
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        TariffWriter tariffWriter = new TariffWriter();
        ParentTicketInfoWriter parentTicketInfoWriter = new ParentTicketInfoWriter(dateFormatter);
        LegalEntityWriter legalEntityWriter = new LegalEntityWriter();
        ExemptionWriter exemptionWriter = new ExemptionWriter(dateFormatter);
        CheckWriter checkWriter = new CheckWriter(dateFormatter);
        TrainInfoWriter trainInfoWriter = new TrainInfoWriter();
        SeasonTicketWriter seasonTicketWriter = new SeasonTicketWriter();
        AdditionalInfoForEttWriter additionalInfoForEttWriter = new AdditionalInfoForEttWriter(dateFormatter);
        PriceWriter priceWriter = new PriceWriter();
        FeeWriter feeWriter = new FeeWriter();
        bankCardPaymentWriter = new BankCardPaymentWriter(dateFormatter);
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
        ticketSaleReturnEventBaseWriter = new TicketSaleReturnEventBaseWriter(parentTicketInfoWriter, legalEntityWriter, exemptionWriter, checkWriter, trainInfoWriter, seasonTicketWriter, additionalInfoForEttWriter, priceWriter, feeWriter, bankCardPaymentWriter);
        ticketEventBaseWriter = new TicketEventBaseWriter(dateFormatter, smartCardWriter, stationWriter, tariffWriter);
    }

    @Override
    public void writeProperties(CPPKTicketReturn cppkTicketReturn, ExportJsonWriter writer) throws IOException {
        //Fields from Event
        eventWriter.writeProperties(cppkTicketReturn, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(cppkTicketReturn, writer);

        //Fields from TicketEventBase
        ticketEventBaseWriter.writeProperties(cppkTicketReturn, writer);

        //From TicketSaleReturnEventBaseWriter
        ticketSaleReturnEventBaseWriter.writeProperties(cppkTicketReturn, writer);

        //From CPPKTicketReturn
        writer.name("Operation").value(cppkTicketReturn.operation);
        writer.name("RecallDateTime").value(dateFormatter.formatDateForExport(cppkTicketReturn.recallDateTime));
        writer.name("RecallReason").value(cppkTicketReturn.recallReason);
        writer.name("RecallTicketNumber").value(cppkTicketReturn.recallTicketNumber);
        writer.name("ReturnPaymentMethod").value(cppkTicketReturn.returnPaymentMethod);
        bankCardPaymentWriter.writeField("ReturnBankCardPayment", cppkTicketReturn.returnBankCardPayment, writer);
        writer.name("FullTicketPrice").value(cppkTicketReturn.fullTicketPrice);
        writer.name("SumToReturn").value(cppkTicketReturn.sumToReturn);
    }

}
