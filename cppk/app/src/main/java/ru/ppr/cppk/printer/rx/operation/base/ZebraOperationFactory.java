package ru.ppr.cppk.printer.rx.operation.base;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.adjustingTable.PrinterPrintAdjustingTableOperation;
import ru.ppr.cppk.printer.rx.operation.adjustingTable.ZebraPrintAdjustingTableOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.AuditTrailTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.ZebraPrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.ZebraPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.barcode.PrintBarcodeOperation;
import ru.ppr.cppk.printer.rx.operation.barcode.ZebraPrintBarcodeOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.BtMonthReportTpl;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.ZebraPrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.ZebraCloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintRepeatFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.ZebraPrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.ZebraPrintRepeatFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.openShift.OpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.openShift.ZebraOpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.PrintZReportOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.ZebraPrintZReportOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.ZebraPrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.ZebraPrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.PrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.ZebraPrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.testTicket.PrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.testTicket.ZebraPrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.PrintTapeStartReportOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.TapeStartReportTpl;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.ZebraPrintTapeStartReportOperation;
import ru.ppr.ikkm.IPrinter;

/**
 * Фабрика операций для принтера Zebra.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraOperationFactory extends OperationFactory {

    public ZebraOperationFactory(IPrinter printer, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintSaleCheckOperation getPrinterPrintSaleCheck(PrintSaleCheckOperation.Params params) {
        return new ZebraPrintSaleCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepealCheckOperation getPrintRepealCheckOperation(PrintRepealCheckOperation.Params params) {
        return new ZebraPrintRepealCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepeatRepealCheckOperation getPrintRepeatRepealCheckOperation(PrintRepeatRepealCheckOperation.Params params) {
        return new ZebraPrintRepeatRepealCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintTestTicketOperation getPrintTestTicketOperation(PrintTestTicketOperation.Params params) {
        return new ZebraPrintTestTicketOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintFineCheckOperation getPrintFineCheckOperation(PrintFineCheckOperation.Params params) {
        return new ZebraPrintFineCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintRepeatFineCheckOperation getPrintRepeatFineCheckOperation(PrintRepeatFineCheckOperation.Params params) {
        return new ZebraPrintRepeatFineCheckOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public OpenShiftOperation getOpenShiftOperation(OpenShiftOperation.Params params) {
        return new ZebraOpenShiftOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintBarcodeOperation getPrintBarcodeOperation(byte[] data) {
        return new ZebraPrintBarcodeOperation(printer, data, printerResourcesManager);
    }

    @Override
    public PrintAuditTrailOperation getPrintAuditTrailOperation(AuditTrailTpl.Params params) {
        return new ZebraPrintAuditTrailOperation(printer, textFormatter, params, printerResourcesManager);
    }

    @Override
    public PrintBtMonthReportOperation getPrintBtMonthReportOperation(BtMonthReportTpl.Params params) {
        return new ZebraPrintBtMonthReportOperation(printer, textFormatter, params, printerResourcesManager);
    }

    @Override
    public PrintZReportOperation getPrintZReportOperation(PrintZReportOperation.Params params) {
        return new ZebraPrintZReportOperation(printer, printerResourcesManager, params);
    }

    @Override
    public CloseShiftOperation getCloseShiftOperation(CloseShiftOperation.Params params) {
        return new ZebraCloseShiftOperation(printer, printerResourcesManager, params);
    }

    @Override
    public PrinterPrintAdjustingTableOperation getPrintAdjustingTableOperation() {
        return new ZebraPrintAdjustingTableOperation(printer, printerResourcesManager);
    }

    @Override
    public PrinterPrintBankSlipOperation getPrintBankSlipOperation(PrinterPrintBankSlipOperation.Params params) {
        return new ZebraPrintBankSlipOperation(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public PrintTapeStartReportOperation getPrintTapeStartReportOperation(TapeStartReportTpl.Params params) {
        return new ZebraPrintTapeStartReportOperation(printer, params, textFormatter, printerResourcesManager);
    }

}
