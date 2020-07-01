package ru.ppr.cppk.printer.rx.operation.saleCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.cppk.printer.rx.common.ZebraFiscalHeaderSetter;
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
public class ZebraPrintSaleCheckOperation extends PrintSaleCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintSaleCheckOperation.class);

    public ZebraPrintSaleCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    /**
     * Метод печати
     *
     * @return
     */
    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            new ZebraFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.SALE);

            new SaleCheckTpl(tplPrinter, tplTextFormatter, params.saleCheckTplPrams).printToDriver();

            printer.printTextInFiscalMode(textFormatter.asProductItem("ПОЛНАЯ СТОИМ.", params.ticketCostCostValueWithoutDiscount));
            if (params.ticketCostCostValueWithoutDiscount.compareTo(params.ticketCostCostValueWithDiscount) != 0) {
                printer.printTextInFiscalMode(textFormatter.asProductItem("", params.ticketCostCostValueWithDiscount));
            }

            if (Decimals.moreThanZero(params.feeValue)) {
                printer.printTextInFiscalMode(textFormatter.asProductItem("СБОР", params.feeValue));
            }

            BigDecimal totalVatValue = params.ticketCostVatValue.add(params.feeVatValue);
            if (Decimals.moreThanZero(totalVatValue)) {
                printer.printTextInFiscalMode(textFormatter.asProductItem("ВКЛ. НДС", totalVatValue));
            }

            BigDecimal total = params.ticketCostCostValueWithDiscount.add(params.feeValue);
            printer.addItem("#", total, BigDecimal.ZERO);
            printer.printTotal(total, params.payment, params.paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? IPrinter.PaymentType.CARD : IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.SALE);

            if (params.addSpaceAfterCheck) {
                //добавим пустых строк на отрыв
                printer.printTextInNormalMode("");
                printer.waitPendingOperations();
            }

            return null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2));
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    private final SaleCheckTpl.Printer tplPrinter = new SaleCheckTpl.Printer() {
        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInFiscalMode(text);
        }

        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInFiscalMode(text);
        }
    };

    private final SaleCheckTpl.TextFormatter tplTextFormatter = new SaleCheckTpl.TextFormatter() {
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
        public String asStr(long number) {
            return textFormatter.asStr(number);
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
        public String asDate_dd_MMM_yyyy(Date dateTime) {
            return textFormatter.asDate_dd_MMM_yyyy(dateTime);
        }
    };
}
