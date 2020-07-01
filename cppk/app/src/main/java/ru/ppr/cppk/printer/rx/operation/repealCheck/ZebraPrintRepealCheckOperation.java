package ru.ppr.cppk.printer.rx.operation.repealCheck;

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
 * Операция печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraPrintRepealCheckOperation extends PrintRepealCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintRepealCheckOperation.class);

    public ZebraPrintRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Single<Result> call() {
        return wrap(() -> {
            new ZebraFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.RETURN);

            new RepealCheckTpl(tplPrinter, tplTextFormatter, params.repealCheckTplParams).printToDriver();

            printer.printTextInFiscalMode(textFormatter.asProductItem("ПОЛНАЯ СТОИМ.", params.ticketCostValueWithoutDiscount));
            if (Decimals.moreThanZero(params.ticketCostValueWithDiscount)) {
                // Сказали убрать https://aj.srvdev.ru/browse/CPPKPP-24928
                // printer.printTextInFiscalMode(PrinterTextFormat.asProductItem("", params.ticketCostValueWithDiscount));
            }

            BigDecimal totalVatValue = params.ticketCostVatValue.add(params.feeVatValue);
            printer.printTextInFiscalMode(textFormatter.asProductItem("СБОР", params.feeValue));
            printer.printTextInFiscalMode(textFormatter.asProductItem("ВКЛ. НДС", totalVatValue));

            BigDecimal total = params.ticketCostValueWithDiscount.add(params.feeValue);

            printer.addItemRefund("#", total, BigDecimal.ZERO);

            printer.printTotal(total, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.RETURN);

            //добавим пустых строк на отрыв
            printer.printTextInNormalMode("");
            printer.waitPendingOperations();

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

    private final RepealCheckTpl.Printer tplPrinter = new RepealCheckTpl.Printer() {
        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInFiscalMode(text);
        }

        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInFiscalMode(text);
        }
    };

    private final RepealCheckTpl.TextFormatter tplTextFormatter = new RepealCheckTpl.TextFormatter() {
        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String alignCenter(String text) {
            return textFormatter.alignCenterFiscalText(text);
        }

        @Override
        public String alignCenter(String text, TextStyle textStyle) {
            return textFormatter.alignCenterFiscalText(text);
        }

        @Override
        public int getWidth() {
            return textFormatter.getWidthForTextStyle(TextStyle.FISCAL_NORMAL);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }

        @Override
        public String asDate_HH_mm_ss(Date date) {
            return textFormatter.asDate_HH_mm_ss(date);
        }
    };

}
