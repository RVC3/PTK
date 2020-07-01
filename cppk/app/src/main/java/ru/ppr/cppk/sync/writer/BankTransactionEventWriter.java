package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.BankTransactionEvent;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class BankTransactionEventWriter extends BaseWriter<BankTransactionEvent> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;

    public BankTransactionEventWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
    }

    @Override
    public void writeProperties(BankTransactionEvent bankTransactionEvent, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(bankTransactionEvent, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(bankTransactionEvent, writer);

        //From BankTransactionEvent
        writer.name("Ern").value(bankTransactionEvent.ern);
        writer.name("TerminalNumber").value(bankTransactionEvent.terminalNumber);
        writer.name("PointOfSaleNumber").value(bankTransactionEvent.pointOfSaleNumber);
        writer.name("MerchantId").value(bankTransactionEvent.merchantId);
        writer.name("BankCode").value(bankTransactionEvent.bankCode);
        writer.name("OperationType").value(bankTransactionEvent.operationType);
        writer.name("OperationResult").value(bankTransactionEvent.operationResult);
        writer.name("Rrn").value(bankTransactionEvent.rrn);
        writer.name("AuthorizationCode").value(bankTransactionEvent.authorizationCode);
        writer.name("SmartCardApplicationName").value(bankTransactionEvent.smartCardApplicationName);
        writer.name("CardPan").value(bankTransactionEvent.cardPan);
        writer.name("CardEmitentName").value(bankTransactionEvent.cardEmitentName);
        writer.name("BankCheckNumber").value(bankTransactionEvent.bankCheckNumber);
        writer.name("TransactionDateTime").value(dateFormatter.formatDateForExport(bankTransactionEvent.transactionDateTime));
        writer.name("Total").value(bankTransactionEvent.total);
        writer.name("CurrencyCode").value(bankTransactionEvent.currencyCode);
    }
}
