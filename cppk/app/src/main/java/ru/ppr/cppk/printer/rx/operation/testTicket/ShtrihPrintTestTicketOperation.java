package ru.ppr.cppk.printer.rx.operation.testTicket;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
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
 * Операция печати пробного ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintTestTicketOperation extends PrintTestTicketOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintTestTicketOperation.class);

    public ShtrihPrintTestTicketOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Single<Result> call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.startFiscalDocument(IPrinter.DocType.SALE);

            printer.printTextInFiscalMode("ПРОБНЫЙ");
            printer.printTextInFiscalMode("Станция привязки:" );
            printer.printTextInFiscalMode(getCurrentBindingStation());

            printer.addItem("ПОЛНАЯ СТОИМОСТЬ", BigDecimal.ZERO, BigDecimal.ZERO);
            printer.printTotal(BigDecimal.ZERO, BigDecimal.ZERO, IPrinter.PaymentType.CASH);

            printer.endFiscalDocument(IPrinter.DocType.SALE);

            return (Void) null;
        })
                .flatMap(aVoid -> new PrinterGetLastDocumentInfo(printer, printerResourcesManager)
                        .call()
                        .map(result -> new Result(result.getOperationTime(), result.getSpnd()))
                        .retry(2))
                .flatMap(result -> wrap(() -> {
                    params.testTicketTplParams.printCheckDateTime = result.getOperationTime();
                    new TestTicketTpl(tplPrinter, tplTextFormatter, params.testTicketTplParams).printToDriver();
                    return result;
                }))
                .flatMap(result ->
                        wrap(() -> {
                            printer.printAdjustingTable();
                            printer.printTextInNormalMode("");
                            printer.printTextInNormalMode("");
                            printer.printTextInNormalMode("");
                            printer.printTextInNormalMode("");
                            return result;
                        }))
                .toSingle();
    }

    @Nullable
    private String getCurrentBindingStation() {
        try {
            return Dagger.appComponent().stationRepository().load(
                    (long) Globals.getInstance().getPrivateSettingsHolder().get()
                            .getSaleStationCode(), Di.INSTANCE.nsiVersionManager()
                            .getCurrentNsiVersionId()).getShortName();
        }
        catch (NullPointerException e){
            return "<нет>";
        }
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    private final TestTicketTpl.Printer tplPrinter = new TestTicketTpl.Printer() {
        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInNormalMode(text, textStyle);
        }

        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInNormalMode(text);
        }
    };

    private final TestTicketTpl.TextFormatter tplTextFormatter = new TestTicketTpl.TextFormatter() {
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
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }

        @Override
        public int getWidthForTextStyle(TextStyle textStyle) {
            return textFormatter.getWidthForTextStyle(textStyle);
        }
    };
}
