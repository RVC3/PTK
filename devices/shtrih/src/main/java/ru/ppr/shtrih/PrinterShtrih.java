package ru.ppr.shtrih;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.zxing.EncodeHintType;
import com.google.zxing.pdf417.encoder.Compaction;
import com.google.zxing.pdf417.encoder.Dimensions;
import com.shtrih.barcode.PrinterBarcode;
import com.shtrih.fiscalprinter.FontNumber;
import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.SmFiscalPrinterException;
import com.shtrih.fiscalprinter.command.CashRegister;
import com.shtrih.fiscalprinter.command.DeviceMetrics;
import com.shtrih.fiscalprinter.command.FSCommunicationStatus;
import com.shtrih.fiscalprinter.command.FSDocumentInfo;
import com.shtrih.fiscalprinter.command.FSPrintCalcReport;
import com.shtrih.fiscalprinter.command.FSPrintCorrectionReceipt;
import com.shtrih.fiscalprinter.command.FSReadSerial;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.fiscalprinter.command.LongPrinterStatus;
import com.shtrih.fiscalprinter.command.OperationRegister;
import com.shtrih.fiscalprinter.command.PrinterDate;
import com.shtrih.fiscalprinter.command.PrinterTime;
import com.shtrih.fiscalprinter.command.ShortPrinterStatus;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.util.ImageReader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jpos.FiscalPrinter;
import jpos.FiscalPrinterConst;
import jpos.JposConst;
import jpos.JposException;
import ru.ppr.ikkm.Printer;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import ru.ppr.utils.Decimals;

/**
 * Реализация принтера "Штрих".
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterShtrih extends Printer {

    private static final String TAG = Logger.makeLogTag(PrinterShtrih.class);

    private static final String IMAGES_DIR = "images";
    private static final String ADJUSTING_TABLE_FILE_NAME = "adjustingTable.bmp";

    /**
     * Ширина билетной ленты в символах для обычного шрифта при обычной печати
     */
    private final static int TEXT_NORMAL_TAPE_WIDTH = 31;
    /**
     * Ширина билетной ленты в символах для увеличенного шрифта при обычной печати
     */
    private final static int TEXT_LARGE_TAPE_WIDTH = 26;
    /**
     * Ширина билетной ленты в символах для обычного шрифта при печати фискального чека
     */
    private final static int FISCAL_NORMAL_TAPE_WIDTH = 26;
    /**
     * Ширина билетной ленты в символах для увеличенного шрифта при печати фискального чека
     */
    private final static int FISCAL_LARGE_TAPE_WIDTH = 26;
    /**
     * Маленький шрифт в фискальном режиме. Используется для печать телефона и e-mail
     */
    private final static int SMALL_FISCAL_FONT_NUMBER = 5;

    private ShtrihFiscalPrinter printer;
    private final SyncChecker syncChecker;
    private final String printerMacAddress;
    private final Context context;
    private final File logDir;
    private final File workingDir;
    private final File imagesDir;
    private final AdjustingTableFileLoader adjustingTableFileLoader;
    private final BluetoothManager bluetoothManager;
    private final InternetManager internetManager;
    private final SimpleDateFormat printerDateFormat;
    private final Timeouts timeouts = new Timeouts();
    private HashMap<Integer, Integer> vatRateToIndexMap = null;
    private Version version;

    public PrinterShtrih(@NonNull Context context,
                         @NonNull File logDir,
                         @NonNull File workingDir,
                         @NonNull AdjustingTableFileLoader adjustingTableFileLoader,
                         @Nullable String printerMacAddress,
                         @NonNull BluetoothManager bluetoothManager,
                         @NonNull InternetManager internetManager,
                         @NonNull SyncChecker syncChecker) throws Exception {
        this.context = context;
        this.logDir = logDir;
        this.workingDir = workingDir;
        this.imagesDir = new File(workingDir, IMAGES_DIR);
        this.adjustingTableFileLoader = adjustingTableFileLoader;
        this.printerMacAddress = printerMacAddress;
        this.bluetoothManager = bluetoothManager;
        this.internetManager = internetManager;
        this.syncChecker = syncChecker;

        printerDateFormat = new SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault());

        initializeWithDriverImpl();

        Logger.trace(TAG, "ShtrihFiscalPrinter created");
    }

    public int closePageImpl(int rotate){
        return 0;
    }
    @Override
    public Timeouts getTimeouts() {
        return timeouts;
    }

    @Override
    protected void prepareResourcesImpl() throws Exception {
        if (!bluetoothManager.enable()) {
            throw new Exception("Could not enable bluetoothManager");
        }
    }

    @Override
    protected void freeResourcesImpl() throws Exception {
        if (printer.getState() != JposConst.JPOS_S_CLOSED) {
            printer.stopFSService();
            Logger.trace(TAG, "stopFSService called");
        } else {
            Logger.trace(TAG, "stopFSService skipped: printer state is closed");
        }
        internetManager.disable();
        if (!bluetoothManager.disable()) {
            throw new Exception("Could not disable bluetoothManager");
        }
    }

    @Override
    protected void initializeWithDriverImpl() throws Exception {
        Logger.trace(TAG, "initializeWithDriverImpl start");
        try {
            Logger.trace(TAG, "initializeWithDriverImpl LogbackConfig.configure");
            LogbackConfig.configure(logDir);
            Logger.trace(TAG, "initializeWithDriverImpl JposConfig.configure");
            JposConfig.configure(context, workingDir, printerMacAddress);
            Logger.trace(TAG, "initializeWithDriverImpl new ShtrihFiscalPrinter");
            printer = new ShtrihFiscalPrinter(new FiscalPrinter());
            Logger.trace(TAG, "ShtrihFiscalPrinter initialized");
        } catch (Exception e) {
            Logger.error(TAG, "initializeWithDriverImpl failed", e);
            throw modifyException(e);
        } finally {
            Logger.trace(TAG, "initializeWithDriverImpl end");
        }
    }

    @Override
    protected boolean checkConnectionWithDriverImpl() throws Exception {
        try {
            String[] dateString = new String[1];
            printer.getDate(dateString);
            return true;
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void connectWithDriverImpl() throws Exception {
        Logger.trace(TAG, "connectWithDriverImpl start");
        try {
            if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                Logger.trace(TAG, "connectWithDriverImpl printer.close start");
                printer.close();
                Logger.trace(TAG, "connectWithDriverImpl printer.close end");
            }
            Logger.trace(TAG, "connectWithDriverImpl open");
            printer.open("ShtrihFptr");
            Logger.trace(TAG, "connectWithDriverImpl claim");
            printer.claim(3000);
            Logger.trace(TAG, "connectWithDriverImpl setDeviceEnabled");
            printer.setDeviceEnabled(true);
            version = getVersion();
            Logger.info(TAG, "connectWithDriverImpl() " + version.toString());

            // Устанавливаем крупный шрифт для надписи "ИТОГ:"
            // http://agile.srvdev.ru/browse/CPPKPP-34337
            // >> В таблице 8 поле 18 укажите необходимый Вам шрифт.
            writeTable(8, 1, 18, "1");

            //http://agile.srvdev.ru/browse/CPPKPP-35574
            if (version.build >= 20025) {
                //изменяем количество нулей после точки в товарной позиции
                //Добавлено значение 2 в поле 29 "ПЕЧАТАТЬ ДРОБНОЕ В КОЛИЧЕСТВЕ" в табл.1. Если передаваемая точность количества до X.001, то количество печатается в формате X.XXX, иначе - в формате X.XXXXXX.
                writeTable(1, 1, 29, "2");

                //Добавлено поле 35 "ФОРМАТ ЗАГОЛОВКА ЧЕКА" в табл.1. Если значение 1, то наименование пользователя
                // 1048, адрес расчетов 1009, место расчетов 1187 печатаются внизу заголовка чека под строчками "номер чека за смену".
                writeTable(1, 1, 35, "1");

                //http://agile.srvdev.ru/browse/CPPKPP-35832
                //Ошибка 112 при работе с принтером. Помогает только перезагрузка ФР и Устройства
                //Падение библиотеки SDK при закрытии смены на прошивке 20025.
                //При условии, что ФР (прошивка 20029) всегда включен (Настройка Таблица 1 значение 0) - ошибка не воспроизводится.
                writeTable(1, 1, 30, "0");
            }


        } catch (Exception e) {
            Logger.error(TAG, "connectWithDriverImpl failed", e);
            throw modifyException(e);
        } finally {
            Logger.trace(TAG, "connectWithDriverImpl end");
        }
    }

    @Override
    public void disconnectWithDriverImpl() throws Exception {
        Logger.trace(TAG, "disconnectWithDriverImpl start");
        try {
            if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                Logger.trace(TAG, "disconnectWithDriverImpl printer.close start");
                printer.close();
                Logger.trace(TAG, "disconnectWithDriverImpl printer.close end");
            }
        } catch (Exception e) {
            Logger.error(TAG, "disconnectWithDriverImpl failed", e);
            throw modifyException(e);
        } finally {
            Logger.trace(TAG, "disconnectWithDriverImpl end");
        }
    }

    @Override
    protected void terminateImpl() throws Exception {
        Logger.trace(TAG, "terminateImpl start");
        try {
            // Зависает функция printer.close()
            // Штрих не хочет чинить, попробуем обойтись одним terminate()
            // Было сделано по аналогии с Moebius, хотя и не ясно, зачем там disconnect().
            // UPD:
            // Это не решает проблемы, повисает printer.release();
            // В будущем: 09.08.2017 Нужно требовать от Штриха возможности убить всё в любой момент из любого потока.
            Logger.trace(TAG, "terminateImpl disconnectInternal start");
            disconnectInternal(false);
            Logger.trace(TAG, "terminateImpl disconnectInternal end");
            if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                Logger.trace(TAG, "terminateImpl printer.release start");
                printer.release();
                Logger.trace(TAG, "terminateImpl printer.release end");
            }
            printer = null;
            Logger.trace(TAG, "ShtrihFiscalPrinter terminated");
        } catch (Exception e) {
            Logger.error(TAG, "terminateImpl failed", e);
            throw modifyException(e);
        } finally {
            Logger.trace(TAG, "terminateImpl end");
        }
    }

    @Override
    protected void onConnectionEstablishedImpl(ConnectResult connectResult) throws Exception {
        Logger.trace(TAG, "isFSServiceStarted = " + printer.isFSServiceStarted());
        if (syncChecker.isSyncRequired(this)) {
            internetManager.enable();
            printer.startFSService();
            Logger.trace(TAG, "startFSService called");
        } else {
            Logger.trace(TAG, "startFSService skipped: sync is not required");
        }
    }

    @Override
    protected void printTextInFiscalModeImpl(String text, TextStyle textStyle) throws Exception {
        try {
            printer.printRecMessage(text, ((textStyle == TextStyle.TEXT_LARGE && version.build >= 20025) ? FontNumber.getItalicFont() : FontNumber.getNormalFont()).getValue());
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printTextInNormalModeImpl(String text, TextStyle textStyle) throws Exception {
        try {
            //используем 7 шрифт чтобы не перегревалось http://agile.srvdev.ru/browse/CPPKPP-35832
            printer.printText(text, (textStyle == TextStyle.TEXT_LARGE && version.build >= 20025) ? FontNumber.getItalicFont() : ((version.build >= 20029) ? FontNumber.getTimesFont() : FontNumber.getNormalFont()));
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void waitPendingOperationsImpl() throws Exception {
        try {
            printer.directIO(SmFptrConst.SMFPTR_DIO_WAIT_PRINT, null, null);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void openShiftImpl(int operatorCode, String operatorName) throws Exception {
        try {
            syncDateTime();
            printer.openFiscalDay();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void setCashierImpl(int operatorCode, String operatorName) throws Exception {
        try {
            String[] lines = new String[1];
            lines[0] = operatorName;
            printer.directIO(SmFptrConst.SMFPTR_DIO_WRITE_CASHIER_NAME, null, lines);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected boolean isShiftOpenedImpl() throws Exception {
        try {
            boolean isShiftOpened = printer.getDayOpened();
            Logger.trace(TAG, "isShiftOpenedImpl() printer.getDayOpened() return " + isShiftOpened);
            return isShiftOpened;
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected Date getDateImpl() throws Exception {
        try {
            String[] dateString = new String[1];
            printer.getDate(dateString);
            return printerDateFormat.parse(dateString[0]);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected int getLastSPNDImpl() throws Exception {
        try {
            FSStatusInfo fsStatusInfo = fsReadStatus();
            return (int) fsStatusInfo.getDocNumber();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected Date getLastCheckTimeImpl() throws Exception {
        try {
            FSStatusInfo fsStatusInfo = fsReadStatus();
            FSDocumentInfo fsDocumentInfo = printer.fsFindDocument((int) fsStatusInfo.getDocNumber());
            return ShtrihUtils.fSDateTimeToDate(fsDocumentInfo.getDateTime());
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected int getShiftNumImpl() throws Exception {
        try {
            return getLongPrinterStatus().getCurrentShiftNumber();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printZReportImpl() throws Exception {
        try {
            printer.printZReport();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void setHeaderLinesImpl(List<String> headerLines) throws Exception {
        try {
            printer.resetPrinter();
            printer.setNumHeaderLines(headerLines.size());
            for (int i = 0; i < headerLines.size(); i++) {
                printer.setHeaderLine(i + 1, headerLines.get(i), false);
            }
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void setVatValueImpl(int vatID, int vatValue) throws Exception {
        try {
            printer.setVatValue(vatID, String.valueOf(vatValue));
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected int getVatValueImpl(int vatID) throws Exception {
        try {
            int[] vatRate = new int[1];
            printer.getVatEntry(vatID, 0, vatRate);
            return vatRate[0];
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printBarcodeImpl(byte[] data) throws Exception {
        try {
            PrinterBarcode barcode = new PrinterBarcode();
            barcode.setText(new String(data, "ISO-8859-1"));
            barcode.setType(SmFptrConst.SMFPTR_BARCODE_PDF417);
            barcode.setPrintType(SmFptrConst.SMFPTR_PRINTTYPE_DRIVER);
            barcode.setTextPosition(SmFptrConst.SMFPTR_TEXTPOS_NOTPRINTED);

            Map<EncodeHintType, Object> params = new HashMap<>();
            // Измерения, тут мы задаем количество колонок и столбцов
            params.put(EncodeHintType.PDF417_DIMENSIONS, new Dimensions(3, 3, 2, 60));
            // Можно задать уровень коррекции ошибок, по умолчанию он 0
            params.put(EncodeHintType.ERROR_CORRECTION, 5);

            params.put(EncodeHintType.PDF417_COMPACTION, Compaction.BYTE);
            params.put(EncodeHintType.MARGIN, 0);
            barcode.addParameter(params);
            barcode.setVScale(3);

            printer.printBarcode(barcode);

            // Хак для обеспечения минимального отступа после ШК
            byte[][] margin = new byte[8][1];
            printer.printRawGraphics(margin);

        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected long getOdometerValueImpl() throws Exception {
        try {
            // Добавлены операционные регистры
            // 196 (младшее слово) 197 (старшее слово),
            // которые содержат 32-битный счетчик количества промотанной бумаги
            // в линиях по 0.125 мм.
            // Чтение регистра возвращает 16-битное число.
            int low = getOperationRegister(196).getValue();
            int high = getOperationRegister(197).getValue();
            long lines = (((long) high) << 16) | low;
            return lines / 8;
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected String getINNImpl() throws Exception {
        try {
            return getLongPrinterStatus().getFiscalIDText();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected String getRegNumberImpl() throws Exception {
        try {
            String[] serial = new String[1];
            printer.directIO(SmFptrConst.SMFPTR_DIO_READ_SERIAL, null, serial);
            return serial[0];
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected String getEKLZNumberImpl() throws Exception {
        return null;
        //этот принтер имеет фискальный накопитель и поддерживает 54ФЗ у него нет ЭКЛЗ
        //метод возвращающий номер ЭКЛЗ у него есть нов он возвращает "". В щелях экономии времени будем просто возвращать null
//        try {
//            String[] serial = new String[1];
//            printer.directIO(SmFptrConst.SMFPTR_DIO_READ_EJ_SERIAL, null, serial);
//            return serial[0];
//        } catch (Exception e) {
//            throw modifyException(e);
//        }
    }

    @Override
    protected String getFNSerialImpl() throws Exception {
        try {
            FSReadSerial fsReadSerial = new FSReadSerial();
            fsReadSerial.setSysPassword(printer.getSysPassword());
            printer.fsReadSerial(fsReadSerial);
            return fsReadSerial.getSerial();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected String getModelImpl() throws Exception {
        try {
            return getDeviceMetrics().getDeviceName();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    private DeviceMetrics getDeviceMetrics() throws Exception {
        try {
            DeviceMetrics deviceMetrics = printer.readDeviceMetrics();
            Logger.trace(TAG, "getDeviceMetrics() " + ShtrihUtils.deviceMetricsToString(deviceMetrics));
            return deviceMetrics;
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected BigDecimal getCashInFRImpl() throws Exception {
        try {
        /* Номера регистров описаны здесь:
        http://agile.srvdev.ru/browse/CPPKPP-32811
        в файле ШТРИХ-МОБАЙЛ-Ф_ИЭ_03.05.17.pdf

        Вырезка:
        Накопления по видам оплаты по 4 типам торговых операций (приход, расход, возврат прихода, возврат расхода) за смену:
        193...196 –наличными;
        197...200 –видом оплаты 2;
        201...204 –видом оплаты 3;
        205...208 –видом оплаты 4;
       */

            // Так сделано на кассе, мы лишь повторили.
            long sum = 0;
            sum += getCashRegister(193).getValue();
            sum += getCashRegister(197).getValue();
            sum += getCashRegister(201).getValue();
            sum += getCashRegister(205).getValue();

            return new BigDecimal(sum).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected long getAvailableSpaceForShiftsImpl() throws Exception {
        try {
            return printer.getRemainingFiscalMemory();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printAdjustingTableImpl() throws Exception {
        try {
            File adjustingTable = new File(imagesDir, ADJUSTING_TABLE_FILE_NAME);
            if (!adjustingTable.exists()) {
                if (!imagesDir.exists() && !imagesDir.mkdirs()) {
                    throw new Exception("Could not create dir: " + imagesDir.getAbsolutePath());
                }
                adjustingTableFileLoader.loadAdjustingTable(adjustingTable);
            }
            /*
            printer.clearImages();
            int index = printer.loadImage(adjustingTable.getAbsolutePath());
            printer.printImage(index);
            */
            byte[][] data = new ImageReader(adjustingTable.getAbsolutePath()).getData();
            printer.printRawGraphics(data);

        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void startFiscalDocumentImpl(DocType docType) throws Exception {
        try {
            printer.resetPrinter();
            if (docType == DocType.SALE) {
                printer.setFiscalReceiptType(SmFptrConst.SMFPTR_RT_SALE);
            } else if (docType == DocType.RETURN) {
                printer.setFiscalReceiptType(SmFptrConst.SMFPTR_RT_RETSALE);
            }
            printer.beginFiscalReceipt(true);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void endFiscalDocumentImpl(DocType docType) throws Exception {
        try {
            printer.endFiscalReceipt(false);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void addItemImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {
        try {
            printer.printRecItem(
                    description,
                    amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                    1000,
                    getVatIndexForRate(vatRate),
                    0,
                    ""
            );
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void addItemRefundImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {
        try {
            printer.printRecItemRefund(
                    description,
                    amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                    0,
                    getVatIndexForRate(vatRate),
                    0,
                    ""
            );
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void addDiscountImpl(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws Exception {
        try {
            printer.printRecItemAdjustment(
                    FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                    "",
                    discount.multiply(new BigDecimal(-100)).intValue(), // переводим рубли в копейки
                    getVatIndexForRate(vatRate)
            );
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printTotalImpl(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws Exception {
        try {
            printer.printRecTotal(
                    total.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                    payment.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                    paymentType == PaymentType.CARD ? "1" : "0"
            );
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected List<ClosedShiftInfo> getShiftsInfoImpl(int startNum, int endNum) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected int getWidthForTextStyleImpl(TextStyle textStyle) {
        switch (textStyle) {
            case TEXT_NORMAL:
                return TEXT_NORMAL_TAPE_WIDTH;
            case TEXT_LARGE:
                return TEXT_LARGE_TAPE_WIDTH;
            case FISCAL_NORMAL:
                return FISCAL_NORMAL_TAPE_WIDTH;
            case FISCAL_LARGE:
                return FISCAL_LARGE_TAPE_WIDTH;
            default:
                throw new IllegalArgumentException("Unsupported textStyle = " + textStyle);
        }
    }

    @Override
    protected boolean isFederalLaw54SupportedImpl() {
        return true;
    }

    @Override
    protected void setCustomerPhoneNumberImpl(String phoneNumber) throws Exception {
        int currentFontNumber = getCurrentFontNumber();
        try {
            printer.setFontNumber(SMALL_FISCAL_FONT_NUMBER);
            printer.directIO(SmFptrConst.SMFPTR_DIO_FS_WRITE_CUSTOMER_PHONE, null, phoneNumber);
        } catch (Exception e) {
            throw modifyException(e);
        } finally {
            try {
                printer.setFontNumber(currentFontNumber);
            } catch (Exception e) {
                throw modifyException(e);
            }
        }
    }

    @Override
    protected void setCustomerEmailImpl(String email) throws Exception {
        int currentFontNumber = getCurrentFontNumber();
        try {
            printer.setFontNumber(SMALL_FISCAL_FONT_NUMBER);
            printer.directIO(SmFptrConst.SMFPTR_DIO_FS_WRITE_CUSTOMER_EMAIL, null, email);
        } catch (Exception e) {
            throw modifyException(e);
        } finally {
            try {
                printer.setFontNumber(currentFontNumber);
            } catch (Exception e) {
                throw modifyException(e);
            }
        }
    }

    private int getCurrentFontNumber() throws Exception {
        int currentFontNumber;
        try {
            currentFontNumber = printer.getFontNumber();
        } catch (Exception e) {
            throw modifyException(e);
        }
        return currentFontNumber;
    }

    @Override
    protected void printNotSentDocsReportImpl() throws Exception {
        try {
            printer.fsPrintCalcReport();
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    @Override
    protected void printCorrectionReceiptImpl(DocType docType, BigDecimal total) throws Exception {
        FSPrintCorrectionReceipt fsPrintCorrectionReceipt = new FSPrintCorrectionReceipt();
        fsPrintCorrectionReceipt.setSysPassword(printer.getSysPassword());
        fsPrintCorrectionReceipt.setTotal(total.multiply(Decimals.HUNDRED).intValue()); // переводим рубли в копейки
        fsPrintCorrectionReceipt.setOperationType(docType == DocType.SALE ? 1 : 3);
        printer.fsPrintCorrectionReceipt(fsPrintCorrectionReceipt);
    }

    @Override
    protected void printDuplicateReceiptImpl() throws Exception {
        printer.printDuplicateReceipt();
    }

    @Override
    protected OfdSettings getOfdSettingsImpl() throws Exception {
        OfdSettings ofdSettings = new OfdSettings();
        ofdSettings.setIp(readTable(15, 1, 1));
        ofdSettings.setPort(Integer.valueOf(readTable(15, 1, 2)));
        ofdSettings.setTimeout(Integer.valueOf(readTable(15, 1, 3)));
        return ofdSettings;
    }

    @Override
    protected void setOfdSettingsImpl(OfdSettings ofdSettings) throws Exception {
        writeTable(15, 1, 1, ofdSettings.getIp());
        writeTable(15, 1, 2, String.valueOf(ofdSettings.getPort()));
        writeTable(15, 1, 3, String.valueOf(ofdSettings.getTimeout()));
    }

    @Override
    protected void scrollPaperInNormalModeImpl(int linesCount) throws Exception {
        try {
            printer.feedPaper(linesCount);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    private FSCommunicationStatus getFSCommunicationStatus() throws Exception {
        FSCommunicationStatus fsCommunicationStatus = printer.fsReadCommStatus();
        Logger.trace(TAG, "getFSCommunicationStatus() return " + ShtrihUtils.fsCommunicationStatusToString(fsCommunicationStatus));
        return fsCommunicationStatus;
    }

    @Override
    protected OfdDocsState getOfdDocsStateImpl() throws Exception {
        FSCommunicationStatus fsCommunicationStatus = getFSCommunicationStatus();
        OfdDocsState ofdDocsState = new OfdDocsState();
        ofdDocsState.setUnsentDocumentsCount(fsCommunicationStatus.getUnsentDocumentsCount());
        ofdDocsState.setFirstUnsentDocumentNumber(fsCommunicationStatus.getFirstUnsentDocumentNumber());
        ofdDocsState.setFirstUnsentDocumentDateTime(ShtrihUtils.fSDateTimeToDate(fsCommunicationStatus.getFirstUnsentDocumentDateTime()));
        Logger.trace(TAG, "getOfdDocsStateImpl() return " + ofdDocsState.toString());
        return ofdDocsState;
    }

    @Override
    protected void startSendingDocsToOfdImpl() throws Exception {
        Logger.trace(TAG, "startSendingDocsToOfdImpl called");
        internetManager.enable();
        printer.startFSService();
    }

    private Exception modifyException(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof SmFiscalPrinterException) {
            SmFiscalPrinterException smFiscalPrinterException = (SmFiscalPrinterException) cause;
            switch (smFiscalPrinterException.getCode()) {
                case 22: {
                    Logger.error(TAG, "Shift time out, code = 22", e);
                    // В будущем: 06.06.2017 Проверить
                    // Поторилось 09.08.2017, после такой ошибки не закрывалась смена, потому что принтер отсался в неверном состоянии
                    // Место мутное, нужно воспроизвести и разобраться
                    // Caused by: com.shtrih.fiscalprinter.SmFiscalPrinterException: 22, ФН: Продолжительность смены более 24 часов
                    return new ShiftTimeOutException();
                }
            }
        } else if (e instanceof JposException) {
            JposException jposException = (JposException) e;
            if (jposException.getMessage() != null && jposException.getMessage().contains("Day end required")) {
                Logger.error(TAG, "Shift time out, day end required", e);
                // В будущем: 09.08.2017 Появился такой вариант
                return new ShiftTimeOutException();
            }
        }
        return e;
    }

    private ShortPrinterStatus getShortPrinterStatus() throws JposException {
        Object[] object = new Object[1];
        printer.directIO(SmFptrConst.SMFPTR_DIO_READ_SHORT_STATUS, null, object);
        ShortPrinterStatus shortPrinterStatus = (ShortPrinterStatus) object[0];
        Logger.trace(TAG, "getShortPrinterStatus, shortPrinterStatus = " + ShtrihUtils.shortPrinterStatusToString(shortPrinterStatus));
        return shortPrinterStatus;
    }

    private LongPrinterStatus getLongPrinterStatus() throws Exception {
        Object[] object = new Object[1];
        printer.directIO(SmFptrConst.SMFPTR_DIO_READ_LONG_STATUS, null, object);
        LongPrinterStatus longPrinterStatus = (LongPrinterStatus) object[0];
        //http://agile.srvdev.ru/browse/CPPKPP-38173
        //такой костыль т.к. регистры могли "не встать" при неудачной печати
        //как выяснилось позднее регистры могут не встать и после этого, но пока оставим.
        if (longPrinterStatus.getSubmode() == 3) {
            Logger.error(TAG, "getLongPrinterStatus submodule=3, longPrinterStatus = " + ShtrihUtils.longPrinterStatusToString(longPrinterStatus));
            try {
                printer.continuePrint();
                printer.waitForPrinting();
            } catch (Exception e) {
                throw modifyException(e);
            }
            return getLongPrinterStatus();
        }
        Logger.trace(TAG, "getLongPrinterStatus, longPrinterStatus = " + ShtrihUtils.longPrinterStatusToString(longPrinterStatus));
        return longPrinterStatus;
    }

    private FSStatusInfo fsReadStatus() throws JposException {
        FSStatusInfo fsStatusInfo = printer.fsReadStatus();
        Logger.trace(TAG, "fsReadStatus() return fsStatusInfo = " + ShtrihUtils.fsStatusInfoToString(fsStatusInfo));
        return fsStatusInfo;
    }


    private CashRegister getCashRegister(int registerNumber) throws JposException {
        int[] data = new int[1];
        Object[] object = new Object[1];
        data[0] = registerNumber;
        printer.directIO(SmFptrConst.SMFPTR_DIO_READ_CASH_REG, data, object);
        CashRegister cashRegister = (CashRegister) object[0];
        Logger.trace(TAG, "getCashRegister, registerNumber = " + registerNumber + ", cashRegister = " + ShtrihUtils.cashRegisterToString(cashRegister));
        return cashRegister;
    }

    private OperationRegister getOperationRegister(int registerNumber) throws JposException {
        int[] data = new int[1];
        Object[] object = new Object[1];
        data[0] = registerNumber;
        printer.directIO(SmFptrConst.SMFPTR_DIO_READ_OPER_REG, data, object);
        OperationRegister operationRegister = (OperationRegister) object[0];
        Logger.trace(TAG, "getOperationRegister, registerNumber = " + registerNumber + ", operationRegister = " + ShtrihUtils.operationRegisterToString(operationRegister));
        return operationRegister;
    }

    /**
     * Получает индекс налоговой ставки по значению.
     *
     * @param vatRate Значение налоговой ставки
     * @return Индекс налоговой ставки
     */
    private int getVatIndexForRate(@Nullable BigDecimal vatRate) throws Exception {
        loadVatRates();
        // Ставки хранятся с сотыми (1800, 1000), поэтому надо домножить на 100
        Integer intVatRate = vatRate == null ? null : vatRate.multiply(new BigDecimal("100")).intValueExact();
        Integer vatIndex = vatRateToIndexMap.get(intVatRate);
        if (vatIndex == null) {
            throw new IllegalArgumentException("Unsupported vat rate = " + vatRate);
        } else {
            Logger.trace(TAG, "getVatIndexForRate " + vatRate + " = " + vatIndex);
            return vatIndex;
        }
    }

    /**
     * Выполняет загрузку списка налоговых ставок.
     *
     * @throws Exception В случае ошибки обращения к принтеру
     */
    private void loadVatRates() throws Exception {
        try {
            if (vatRateToIndexMap == null) {
                vatRateToIndexMap = new HashMap<>();
                /*
                Документация
                http://agile.srvdev.ru/browse/CPPKPP-32811
                в файле UnifiedPOS_1.12_12012007.pdf
                */

                /*
                Страница 59:
                -----------------------------------------------------------------------------------------
                Ряд   |Поле|Назначение     |Размер |Возможные значения             |Значение по умолчанию
                 ----------------------------------------------------------------------------------------
                1 .. 4| 1  |Величина налога|2 BIN  |0 – 9999                       |
                -----------------------------------------------------------------------------------------
                      | 2  |Название налога|57 CHAR|57 символов в кодировке WIN1251|1 НДС 18%
                      |    |               |       |                               |2-НДС 10%
                      |    |               |       |                               |3-НДС 0%
                      |    |               |       |                               |4-Без НДС
                      |    |               |       |                               |5 НДС 18/118
                      |    |               |       |                               |6 НДС 10/110
                */

                /*
                Без НДС, можно использовать индекс 0
                Вырезка из документации:
                printRecItem Method:
                Parameter Description
                vatInfo       VAT rate identifier or amount. If not used a zero must be transferred.
                */
                vatRateToIndexMap.put(null, 0);
                Logger.trace(TAG, "vatValue for index = " + 0 + " loaded, = " + null);

                int vatRatesCount = printer.getNumVatRates();
                for (int vatIndex = 1; vatIndex <= vatRatesCount; vatIndex++) {
                    int vatValue = getVatValueImpl(vatIndex);
                    if (!vatRateToIndexMap.containsKey(vatValue)) {
                        vatRateToIndexMap.put(vatValue, vatIndex);
                        Logger.trace(TAG, "vatValue for index = " + vatIndex + " loaded, = " + vatValue);
                    } else {
                        Logger.trace(TAG, "vatValue for index = " + vatIndex + " duplicated, = " + vatValue);
                    }
                }
            }
        } catch (Exception e) {
            // Нельзя оставалять набор ставок в кривом состоянии
            vatRateToIndexMap = null;
            throw e;
        }
    }

    private void writeTable(int tableNumber, int rowNumber, int fieldNumber, String fieldValue) throws Exception {
        Logger.trace(TAG, "printer.writeTable(" + tableNumber + ", " + rowNumber + ", " + fieldNumber + ", " + fieldValue + ")");
        try {
            printer.writeTable(tableNumber, rowNumber, fieldNumber, fieldValue);
        } catch (Exception e) {
            throw modifyException(e);
        }
    }

    private String readTable(int tableNumber, int rowNumber, int fieldNumber) throws Exception {
        Logger.trace(TAG, "printer.readTable(" + tableNumber + ", " + rowNumber + ", " + fieldNumber + ") START");
        String value = null;
        try {
            value = printer.readTable(tableNumber, rowNumber, fieldNumber);
        } catch (Exception e) {
            throw modifyException(e);
        }
        Logger.trace(TAG, "printer.readTable(" + tableNumber + ", " + rowNumber + ", " + fieldNumber + ") FINISH res = " + value);
        return value;
    }

    private Version getVersion() throws Exception {
        Version fv = new Version();
        try {
            fv.deviceServiceVersion = printer.getDeviceServiceVersion();
            LongPrinterStatus status = printer.readLongPrinterStatus();
            fv.date = status.getFirmwareDate().toString();
            fv.build = status.getFirmwareBuild();
            fv.version = status.getFirmwareVersion();
        } catch (Exception e) {
            throw modifyException(e);
        }
        return fv;
    }

    private void syncDateTime() throws Exception {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR) - 2000;

        PrinterDate date = new PrinterDate(day, month, year);
        printer.writeDate(date);
        printer.confirmDate(date);

        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        PrinterTime time = new PrinterTime(hour, minutes, seconds);
        printer.writeTime(time);
    }

    public interface AdjustingTableFileLoader {
        void loadAdjustingTable(File dst) throws IOException;
    }

    private class Version {
        int deviceServiceVersion;
        String date;
        int build;
        String version;

        @Override
        public String toString() {
            return "Version{" +
                    "deviceServiceVersion=" + deviceServiceVersion +
                    ", date='" + date + '\'' +
                    ", build=" + build +
                    ", version='" + version + '\'' +
                    '}';
        }
    }

    private static class Timeouts extends Printer.Timeouts {

        private static final int TIMEOUT_PREPARE_RESOURCES = Integer.MAX_VALUE;
        private static final int TIMEOUT_FREE_RESOURCES = Integer.MAX_VALUE;
        private static final int TIMEOUT_CONNECT = Integer.MAX_VALUE;
        private static final int TIMEOUT_CHECK_CONNECTION = Integer.MAX_VALUE;
        private static final int TIMEOUT_DISCONNECT = Integer.MAX_VALUE;
        private static final int TIMEOUT_TERMINATE = Integer.MAX_VALUE;
        private static final int TIMEOUT_PRINT_TEXT = Integer.MAX_VALUE;
        private static final int TIMEOUT_WAIT_PENDING_OPERATIONS = Integer.MAX_VALUE;
        private static final int TIMEOUT_OPEN_SHIFT = Integer.MAX_VALUE;
        private static final int TIMEOUT_SET_CASHIER = Integer.MAX_VALUE;
        private static final int TIMEOUT_GET_DATA = Integer.MAX_VALUE;
        private static final int TIMEOUT_SET_DATA = Integer.MAX_VALUE;
        private static final int TIMEOUT_PRINT_Z_REPORT = Integer.MAX_VALUE;
        private static final int TIMEOUT_PRINT_NOT_SENT_DOCS_REPORT = Integer.MAX_VALUE;
        private static final int TIMEOUT_SET_HEADERS = Integer.MAX_VALUE;
        private static final int TIMEOUT_PRINT_ADJUSTING_TABLE = Integer.MAX_VALUE;
        private static final int TIMEOUT_PRINT_BARCODE = Integer.MAX_VALUE;
        private static final int TIMEOUT_FISCAL_COMMAND = Integer.MAX_VALUE;
        private static final int TIMEOUT_CONNECTION_ESTABLISHED = Integer.MAX_VALUE;

        @Override
        public int getPrepareResourcesTimeout() {
            return TIMEOUT_PREPARE_RESOURCES;
        }

        @Override
        public int getFreeResourcesTimeout() {
            return TIMEOUT_FREE_RESOURCES;
        }

        @Override
        public int getConnectTimeout() {
            return TIMEOUT_CONNECT;
        }

        @Override
        public int getCheckConnectionTimeout() {
            return TIMEOUT_CHECK_CONNECTION;
        }

        @Override
        public int getDisconnectTimeout() {
            return TIMEOUT_DISCONNECT;
        }

        @Override
        public int getTerminateTimeout() {
            return TIMEOUT_TERMINATE;
        }

        @Override
        public int getPrintTextTimeout() {
            return TIMEOUT_PRINT_TEXT;
        }

        @Override
        public int getWaitPendingOperationsTimeout() {
            return TIMEOUT_WAIT_PENDING_OPERATIONS;
        }

        @Override
        public int getOpenShiftTimeout() {
            return TIMEOUT_OPEN_SHIFT;
        }

        @Override
        public int getSetCashierTimeout() {
            return TIMEOUT_SET_CASHIER;
        }

        @Override
        public int getGetDataTimeout() {
            return TIMEOUT_GET_DATA;
        }

        @Override
        public int getSetDataTimeout() {
            return TIMEOUT_SET_DATA;
        }

        @Override
        public int getPrintZReportTimeout() {
            return TIMEOUT_PRINT_Z_REPORT;
        }

        @Override
        public int getPrintNotSentDocsReportTimeout() {
            return TIMEOUT_PRINT_NOT_SENT_DOCS_REPORT;
        }

        @Override
        public int getSetHeadersTimeout() {
            return TIMEOUT_SET_HEADERS;
        }

        @Override
        public int getPrintAdjustingTableTimeout() {
            return TIMEOUT_PRINT_ADJUSTING_TABLE;
        }

        @Override
        public int getPrintBarcodeTimeout() {
            return TIMEOUT_PRINT_BARCODE;
        }

        @Override
        public int getFiscalCommandTimeout() {
            return TIMEOUT_FISCAL_COMMAND;
        }

        @Override
        public int getEndFiscalDocumentTimeout() {
            return TIMEOUT_FISCAL_COMMAND;
        }

        @Override
        public int getConnectionEstablishedTimeout() {
            return TIMEOUT_CONNECTION_ESTABLISHED;
        }
    }

    public interface BluetoothManager {
        boolean enable();

        boolean disable();

        boolean isEnabled();
    }

    /**
     * Менеджер для включения/выключения интернета
     */
    public interface InternetManager {
        boolean enable();

        boolean disable();
    }
}
