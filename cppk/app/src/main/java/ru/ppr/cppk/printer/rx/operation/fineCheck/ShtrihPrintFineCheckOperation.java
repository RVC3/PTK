package ru.ppr.cppk.printer.rx.operation.fineCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ShtrihFiscalHeaderSetter;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Single;

/**
 * Операция печати чека взимания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintFineCheckOperation extends PrintFineCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintFineCheckOperation.class);

    public ShtrihPrintFineCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Single<Result> call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.SALE);

            if (params.customerPhoneNumber != null) {
                printer.setCustomerPhoneNumber(params.customerPhoneNumber);
            }
            if (params.customerEmail != null) {
                printer.setCustomerEmail(params.customerEmail);
            }

            printer.addItem("ЦЕНА", params.amount, new BigDecimal(params.vatRate));

            BigDecimal total = params.amount;
            printer.printTotal(total, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.SALE);
            return (Void) null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2))
                .flatMap(result -> wrap(() -> {
                    params.fineCheckTplParams.printCheckDateTime = result.getOperationTime();
                    new FineCheckTpl(tplPrinter, tplTextFormatter, params.fineCheckTplParams).printToDriver();
                    //добавим пустых строк на отрыв
                    printer.printTextInNormalMode("");
                    printer.printTextInNormalMode("");
                    printer.printTextInNormalMode("");
                    printer.printTextInNormalMode("");
                    printer.waitPendingOperations();
                    return result;
                }))
                .toSingle();
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    private final FineCheckTpl.Printer tplPrinter = new FineCheckTpl.Printer() {
        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInNormalMode(text, textStyle);
        }
    };

    private final FineCheckTpl.TextFormatter tplTextFormatter = new FineCheckTpl.TextFormatter() {
        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String alignCenter(String text, TextStyle textStyle) {
            return textFormatter.alignCenterText(text, textStyle);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }
    };
}
