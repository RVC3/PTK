package ru.ppr.cppk.printer.rx.operation.repealCheck;

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
 * Операция печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintRepealCheckOperation extends PrintRepealCheckOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintRepealCheckOperation.class);

    public ShtrihPrintRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Single<Result> call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.RETURN);
            // Перед полной стоимостью,
            // см. скрин 1 http://agile.srvdev.ru/browse/CPPKPP-35362
            new RepealCheckTpl(tplPrinter, tplTextFormatter, params.repealCheckTplParams).printToDriver();

            // В будущем: 02.06.2017 Раньше это скрывали и выводили стоимость без скидки
            // http://agile.srvdev.ru/browse/CPPKPP-24928
            printer.addItemRefund(params.repealCheckTplParams.repealPdTicketTypeName, params.ticketCostValueWithDiscount, params.ticketCostVatRate);
            printer.addItemRefund("СБОР", params.feeValue, params.feeVatRate);

            BigDecimal total = params.ticketCostValueWithDiscount.add(params.feeValue);
            printer.printTotal(total, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.RETURN);

            return null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2))
                .flatMap(result -> wrap(() -> {
                    params.repealCheckTplParams.printCheckDateTime = result.getOperationTime();
                    // Добавим пустых строк на отрыв
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

    private final RepealCheckTpl.Printer tplPrinter = new RepealCheckTpl.Printer() {
        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInFiscalMode(text);
        }

        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInFiscalMode(text, textStyle);
        }
    };

    private final RepealCheckTpl.TextFormatter tplTextFormatter = new RepealCheckTpl.TextFormatter() {
        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String alignCenter(String text) {
            return textFormatter.alignCenterText(text);
        }

        @Override
        public String alignCenter(String text, TextStyle textStyle) {
            return textFormatter.alignCenterText(text, textStyle);
        }

        @Override
        public int getWidth() {
            return textFormatter.getWidthForTextStyle(TextStyle.TEXT_NORMAL);
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
