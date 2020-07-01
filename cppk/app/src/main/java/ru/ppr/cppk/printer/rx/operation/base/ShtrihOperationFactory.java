package ru.ppr.cppk.printer.rx.operation.base;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.adjustingTable.PrinterPrintAdjustingTableOperation;
import ru.ppr.cppk.printer.rx.operation.adjustingTable.ShtrihPrintAdjustingTableOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.AuditTrailTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.ShtrihPrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.ShtrihPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.barcode.PrintBarcodeOperation;
import ru.ppr.cppk.printer.rx.operation.barcode.ShtrihPrintBarcodeOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.BtMonthReportTpl;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.ShtrihPrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.ShtrihCloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintRepeatFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.ShtrihPrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.ShtrihPrintRepeatFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.openShift.OpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.openShift.ShtrihOpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.PrintZReportOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.ShtrihPrintZReportOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.ShtrihPrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.ShtrihPrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.PrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.ShtrihPrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.testTicket.PrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.testTicket.ShtrihPrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.PrintTapeStartReportOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.ShtrihPrintTapeStartReportOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.TapeStartReportTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Фабрика операций для принтера Штрих.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihOperationFactory extends OperationFactory {

    public ShtrihOperationFactory(IPrinter printer, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintSaleCheckOperation getPrinterPrintSaleCheck(PrintSaleCheckOperation.Params params) {
        return new ShtrihPrintSaleCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepealCheckOperation getPrintRepealCheckOperation(PrintRepealCheckOperation.Params params) {
        return new ShtrihPrintRepealCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepeatRepealCheckOperation getPrintRepeatRepealCheckOperation(ShtrihPrintRepeatRepealCheckOperation.Params params) {
        return new ShtrihPrintRepeatRepealCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintTestTicketOperation getPrintTestTicketOperation(PrintTestTicketOperation.Params params) {
        return new ShtrihPrintTestTicketOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintFineCheckOperation getPrintFineCheckOperation(PrintFineCheckOperation.Params params) {
        return new ShtrihPrintFineCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepeatFineCheckOperation getPrintRepeatFineCheckOperation(PrintRepeatFineCheckOperation.Params params) {
        return new ShtrihPrintRepeatFineCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public OpenShiftOperation getOpenShiftOperation(OpenShiftOperation.Params params) {
        return new ShtrihOpenShiftOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintBarcodeOperation getPrintBarcodeOperation(byte[] data) {
        return new ShtrihPrintBarcodeOperation(printer, data, printerResourcesManager);
    }

    @Override
    public PrintAuditTrailOperation getPrintAuditTrailOperation(AuditTrailTpl.Params params) {
        return new ShtrihPrintAuditTrailOperation(printer, textFormatter, params, printerResourcesManager);
    }

    @Override
    public PrintBtMonthReportOperation getPrintBtMonthReportOperation(BtMonthReportTpl.Params params) {
        return new ShtrihPrintBtMonthReportOperation(printer, textFormatter, params, printerResourcesManager);
    }

    @Override
    public PrintZReportOperation getPrintZReportOperation(PrintZReportOperation.Params params) {
        return new ShtrihPrintZReportOperation(printer, printerResourcesManager);
    }

    @Override
    public CloseShiftOperation getCloseShiftOperation(CloseShiftOperation.Params params) {
        return new ShtrihCloseShiftOperation(printer, printerResourcesManager, textFormatter, params);
    }

    @Override
    public PrinterPrintAdjustingTableOperation getPrintAdjustingTableOperation() {
        return new ShtrihPrintAdjustingTableOperation(printer, printerResourcesManager);
    }

    @Override
    public PrinterPrintBankSlipOperation getPrintBankSlipOperation(PrinterPrintBankSlipOperation.Params params) {
        return new ShtrihPrintBankSlipOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintTapeStartReportOperation getPrintTapeStartReportOperation(TapeStartReportTpl.Params params) {
        return new ShtrihPrintTapeStartReportOperation(printer, params, textFormatter, printerResourcesManager);
    }

}
