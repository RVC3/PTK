package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.FinePaidEvent;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.model.BankCardPaymentWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.CheckWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class FinePaidEventWriter extends BaseWriter<FinePaidEvent> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;
    private final CheckWriter checkWriter;
    private final BankCardPaymentWriter bankCardPaymentWriter;

    public FinePaidEventWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
        checkWriter = new CheckWriter(dateFormatter);
        bankCardPaymentWriter = new BankCardPaymentWriter(dateFormatter);
    }

    @Override
    public void writeProperties(FinePaidEvent finePaidEvent, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(finePaidEvent, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(finePaidEvent, writer);

        //From FinePaidEvent
        writer.name("FineCode").value(finePaidEvent.fineCode);
        writer.name("OperationDateTime").value(dateFormatter.formatDateForExport(finePaidEvent.operationDateTime));
        writer.name("DocNumber").value(finePaidEvent.docNumber);
        checkWriter.writeField("Check", finePaidEvent.check, writer);
        writer.name("Amount").value(finePaidEvent.amount);
        writer.name("NdsPercent").value(finePaidEvent.ndsPercent);
        writer.name("Nds").value(finePaidEvent.nds);
        writer.name("PaymentType").value(finePaidEvent.paymentType);
        bankCardPaymentWriter.writeField("BankCardPayment", finePaidEvent.bankCardPayment, writer);
    }


}
