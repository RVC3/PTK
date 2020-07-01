package ru.ppr.cppk.printer.rx.operation.saleCheck;

import android.util.Log;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ShtrihFiscalHeaderSetter;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция печати чека продажи.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintSaleCheckOperation extends PrintSaleCheckOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintSaleCheckOperation.class);

    public ShtrihPrintSaleCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
        Logger.debug(TAG, "ShtrihPrintSaleCheckOperation() called with: printer = [" + printer + "],\n " +
                "params = [" + params + "],\n" +
                " textFormatter = [" + textFormatter + "], printerResourcesManager = [" + printerResourcesManager + "]");
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.SALE);

            if (params.customerPhoneNumber != null) {
                printer.setCustomerPhoneNumber(params.customerPhoneNumber);
            }
            if (params.customerEmail != null) {
                printer.setCustomerEmail(params.customerEmail);
            }

            if (params.ticketCostCostValueWithoutDiscount.compareTo(params.ticketCostCostValueWithDiscount) == 0) {
                printer.printTextInFiscalMode("ПОЛНАЯ СТОИМОСТЬ");
            } else {
                printer.printTextInFiscalMode(textFormatter.asProductItem("ПОЛНАЯ СТОИМОСТЬ", params.ticketCostCostValueWithoutDiscount));
            }

            printer.addItem(params.ticketTypeName, params.ticketCostCostValueWithDiscount, params.ticketCostVatRate);
            if (Decimals.moreThanZero(params.feeValue)) {
                printer.addItem("СБОР", params.feeValue, params.feeVatRate);
            }

            BigDecimal total = params.ticketCostCostValueWithDiscount.add(params.feeValue);
            printer.printTotal(total, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.SALE);

            return null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2))
                .flatMap(result -> wrap(() -> {
                    params.saleCheckTplPrams.printCheckDateTime = result.getOperationTime();
                    new SaleCheckTpl(tplPrinter, tplTextFormatter, params.saleCheckTplPrams).printToDriver();

                    if (params.addSpaceAfterCheck) {
                        //добавим пустых строк на отрыв
                        printer.printTextInNormalMode("");
                        printer.printTextInNormalMode("");
                        printer.printTextInNormalMode("");
                        printer.printTextInNormalMode("");
                        printer.waitPendingOperations();
                    }

                    return result;
                }));
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    private final SaleCheckTpl.Printer tplPrinter = new SaleCheckTpl.Printer() {
        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInNormalMode(text);
        }

        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInNormalMode(text, textStyle);
        }
    };

    private final SaleCheckTpl.TextFormatter tplTextFormatter = new SaleCheckTpl.TextFormatter() {
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
        public String asStr(long number) {
            return textFormatter.asStr(number);
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
        public String asDate_dd_MMM_yyyy(Date dateTime) {
            return textFormatter.asDate_dd_MMM_yyyy(dateTime);
        }
    };
}
