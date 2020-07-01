package ru.ppr.cppk.printer.rx.operation.base;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.PrintDuplicateReceiptOperation;
import ru.ppr.cppk.printer.rx.operation.PrintMonthSheetFooterOperation;
import ru.ppr.cppk.printer.rx.operation.PrinterGetCashInFR;
import ru.ppr.cppk.printer.rx.operation.PrinterGetDate;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.cppk.printer.rx.operation.PrinterGetOdometerValue;
import ru.ppr.cppk.printer.rx.operation.PrinterGetOfdDocsState;
import ru.ppr.cppk.printer.rx.operation.PrinterGetOfdSettings;
import ru.ppr.cppk.printer.rx.operation.PrinterGetShiftsInfo;
import ru.ppr.cppk.printer.rx.operation.PrinterGetState;
import ru.ppr.cppk.printer.rx.operation.PrinterPrintLines;
import ru.ppr.cppk.printer.rx.operation.PrinterPrintNotSentDocsReport;
import ru.ppr.cppk.printer.rx.operation.PrinterSendDocsToOfd;
import ru.ppr.cppk.printer.rx.operation.PrinterSetOfdSettings;
import ru.ppr.cppk.printer.rx.operation.adjustingTable.PrinterPrintAdjustingTableOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.AuditTrailTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.barcode.PrintBarcodeOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.BtMonthReportTpl;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.ClearingSheetTpl;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.PrintClearingSheetOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.MonthSheetTpl;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.PrintDiscountMonthSheetOperation;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.PrintDiscountShiftSheetOperation;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.ShiftSheetTpl;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintRepeatFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.openShift.OpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.PrintZReportOperation;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrintSalesForEttLogOperation;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrinterTplSalesForEttLog;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.PrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ShiftOrMonthSheetTpl;
import ru.ppr.cppk.printer.rx.operation.tapeEndReport.PrintTapeEndReportOperation;
import ru.ppr.cppk.printer.rx.operation.tapeEndReport.TapeEndReportTpl;
import ru.ppr.cppk.printer.rx.operation.testTicket.PrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.PrintTapeStartReportOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.TapeStartReportTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.model.OfdSettings;

/**
 * Фабрика операций для принтера.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class OperationFactory {

    final IPrinter printer;
    final PrinterResourcesManager printerResourcesManager;
    final TextFormatter textFormatter;

    OperationFactory(IPrinter printer, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        this.printer = printer;
        this.printerResourcesManager = printerResourcesManager;
        this.textFormatter = textFormatter;
    }

    public abstract PrintSaleCheckOperation getPrinterPrintSaleCheck(PrintSaleCheckOperation.Params params);

    public abstract PrintRepealCheckOperation getPrintRepealCheckOperation(PrintRepealCheckOperation.Params params);

    public abstract PrintRepeatRepealCheckOperation getPrintRepeatRepealCheckOperation(PrintRepeatRepealCheckOperation.Params params);

    public abstract PrintTestTicketOperation getPrintTestTicketOperation(PrintTestTicketOperation.Params params);

    public abstract PrintFineCheckOperation getPrintFineCheckOperation(PrintFineCheckOperation.Params params);

    public abstract PrintRepeatFineCheckOperation getPrintRepeatFineCheckOperation(PrintRepeatFineCheckOperation.Params params);

    public abstract OpenShiftOperation getOpenShiftOperation(OpenShiftOperation.Params params);

    public abstract PrintBarcodeOperation getPrintBarcodeOperation(byte[] data);

    public abstract PrintAuditTrailOperation getPrintAuditTrailOperation(AuditTrailTpl.Params params);

    public abstract PrintBtMonthReportOperation getPrintBtMonthReportOperation(BtMonthReportTpl.Params params);

    public abstract PrintZReportOperation getPrintZReportOperation(PrintZReportOperation.Params params);

    public abstract CloseShiftOperation getCloseShiftOperation(CloseShiftOperation.Params params);

    public abstract PrinterPrintAdjustingTableOperation getPrintAdjustingTableOperation();

    public abstract PrinterPrintBankSlipOperation getPrintBankSlipOperation(PrinterPrintBankSlipOperation.Params params);

    public abstract PrintTapeStartReportOperation getPrintTapeStartReportOperation(TapeStartReportTpl.Params params);

    public PrintMonthSheetFooterOperation getPrintMonthSheetFooterOperation(PrintMonthSheetFooterOperation.Params params) {
        return new PrintMonthSheetFooterOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintSalesForEttLogOperation getPrintSalesForEttLogOperation(PrinterTplSalesForEttLog.Params params) {
        return new PrintSalesForEttLogOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintShiftOrMonthSheetOperation getPrintShiftOrMonthSheetOperation(ShiftOrMonthSheetTpl.Params params) {
        return new PrintShiftOrMonthSheetOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintDiscountMonthSheetOperation getPrintDiscountMonthSheetOperation(MonthSheetTpl.Params params) {
        return new PrintDiscountMonthSheetOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintTapeEndReportOperation getPrintTapeEndReportOperation(TapeEndReportTpl.Params params) {
        return new PrintTapeEndReportOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintDiscountShiftSheetOperation getPrintDiscountShiftSheetOperation(ShiftSheetTpl.Params params) {
        return new PrintDiscountShiftSheetOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrintClearingSheetOperation getPrintClearingSheetOperation(ClearingSheetTpl.Params params) {
        return new PrintClearingSheetOperation(printer, params, textFormatter, printerResourcesManager);
    }

    public PrinterPrintNotSentDocsReport getPrintNotSentDocsReport() {
        return new PrinterPrintNotSentDocsReport(printer, printerResourcesManager);
    }

    public PrinterPrintLines getPrintLinesOperation() {
        return new PrinterPrintLines(printer, printerResourcesManager);
    }

    public PrinterGetState getGetStateOperation() {
        return new PrinterGetState(printer, printerResourcesManager);
    }

    public PrinterGetShiftsInfo getGetShiftsInfo() {
        return new PrinterGetShiftsInfo(printer, printerResourcesManager);
    }

    public PrinterGetOdometerValue getGetOdometerValue() {
        return new PrinterGetOdometerValue(printer, printerResourcesManager);
    }

    public PrinterGetDate getGetDateOperation() {
        return new PrinterGetDate(printer, printerResourcesManager);
    }

    public PrinterGetCashInFR getGetCashInFrOperation() {
        return new PrinterGetCashInFR(printer, printerResourcesManager);
    }

    public PrinterGetLastDocumentInfo getGetLastDocumentInfoOperation() {
        return new PrinterGetLastDocumentInfo(printer, printerResourcesManager);
    }

    public PrinterGetOfdSettings getOfdSettingsOperation() {
        return new PrinterGetOfdSettings(printer, printerResourcesManager);
    }

    public PrinterSetOfdSettings setOfdSettingsOperation(OfdSettings ofdSettings) {
        return new PrinterSetOfdSettings(printer, ofdSettings, printerResourcesManager);
    }

    public PrinterGetOfdDocsState getOfdDocsStateOperation() {
        return new PrinterGetOfdDocsState(printer, printerResourcesManager);
    }

    public PrinterSendDocsToOfd getSendDocsToOfdOperation(int timeout) {
        return new PrinterSendDocsToOfd(printer, timeout, printerResourcesManager);
    }

    public PrintDuplicateReceiptOperation printDuplicateReceiptOperation() {
        return new PrintDuplicateReceiptOperation(printer, printerResourcesManager);
    }

}
