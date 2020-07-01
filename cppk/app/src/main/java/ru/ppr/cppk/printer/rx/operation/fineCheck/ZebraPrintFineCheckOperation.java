package ru.ppr.cppk.printer.rx.operation.fineCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ZebraFiscalHeaderSetter;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.cppk.utils.Decimals;
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
public class ZebraPrintFineCheckOperation extends PrintFineCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintFineCheckOperation.class);

    public ZebraPrintFineCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    /**
     * Метод печати
     *
     * @return
     */
    @Override
    public Single<Result> call() {
        return wrap(() -> {
            new ZebraFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.SALE);

            new FineCheckTpl(tplPrinter, tplTextFormatter, params.fineCheckTplParams).printToDriver();

            printer.printTextInFiscalMode(textFormatter.asProductItem("ЦЕНА", params.amount));

            BigDecimal totalVatValue = params.vatValue;
            if (Decimals.moreThanZero(totalVatValue)) {
                printer.printTextInFiscalMode(textFormatter.asProductItem("ВКЛ. НДС", totalVatValue));
            }

            printer.addItem("#", params.amount, BigDecimal.ZERO);
            printer.printTotal(params.amount, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.SALE);

            return null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2))
                .toSingle();
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    private final FineCheckTpl.Printer tplPrinter = new FineCheckTpl.Printer() {
        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInFiscalMode(text, textStyle);
        }
    };

    private final FineCheckTpl.TextFormatter tplTextFormatter = new FineCheckTpl.TextFormatter() {
        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String alignCenter(String text, TextStyle textStyle) {
            return textFormatter.alignCenterFiscalText(text);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }
    };
}
