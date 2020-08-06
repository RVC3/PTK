package ru.ppr.ikkm.file;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ppr.ikkm.Printer;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.ikkm.file.state.State;
import ru.ppr.ikkm.file.state.StateVirtual;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.storage.PrinterStateStorage;
import ru.ppr.ikkm.file.transaction.FiscalTransaction;
import ru.ppr.ikkm.file.transaction.Transaction;
import ru.ppr.ikkm.file.transaction.TransactionException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.DateFormatOperations;
import ru.ppr.utils.Decimals;

/**
 * Принтер который пишет в файл напечатанные документы
 * <p>
 * Created by Артем on 15.02.2016.
 */
public class PrinterFile extends Printer {

    private static final String TAG = Logger.makeLogTag(PrinterFile.class);

    private static final String Model = "FilePrinter";

    private static final int TAPE_WIDTH = 28;

    private final static int ONE_LINE_HEIGHT = 5;//милитеров
    private final static int MAX_HEADERS_LINE = 5;
    public static final Long PRINTER_FILE_ID = 646168L;
    public static final String MODEl = "PrinterFile";
    private State state;
    private Transaction transaction;
    private File fileToWrite;
    private final File workingDir;
    private final PrinterStateStorage printerStateStorage;
    private final OfdSettings ofdSettings;
    private final OfdDocsState ofdDocsState;

    public PrinterFile(@NonNull File workingDir,
                       @NonNull final PrinterStateStorage stateStorage) {
        this.workingDir = workingDir;
        printerStateStorage = stateStorage;
        // Возможно стоит как то динамически задавать тип Storage
        fileToWrite = new File(workingDir, "printer.txt");
        ofdSettings = new OfdSettings();
        ofdSettings.setIp("STAB");
        ofdSettings.setPort(0);
        ofdSettings.setTimeout(5);
        ofdDocsState = new OfdDocsState();
        ofdDocsState.setFirstUnsentDocumentNumber(0);
        ofdDocsState.setFirstUnsentDocumentDateTime(new Date());
        ofdDocsState.setUnsentDocumentsCount(0);
    }

    public int closePageImpl(int rotate){
        return 0;
    }

    @Override
    protected void initializeWithDriverImpl() throws Exception {

    }

    @Override
    protected void terminateImpl() throws Exception {
        disconnectInternal(false);
        Logger.trace(TAG, "PrinterFile terminated");
    }

    @Override
    protected void onConnectionEstablishedImpl(ConnectResult connectResult) throws Exception {

    }

    @Override
    protected void prepareResourcesImpl() throws Exception {

    }

    @Override
    protected void freeResourcesImpl() throws Exception {

    }

    @Override
    protected boolean checkConnectionWithDriverImpl() throws Exception {
        return true;
    }

    @Override
    protected void connectWithDriverImpl() throws Exception {
        connectToDb();
    }

    @Override
    public void disconnectWithDriverImpl() throws Exception {
        disconnectFromDb();
    }

    @Override
    protected void printTextInFiscalModeImpl(String text, TextStyle textStyle) throws Exception {
        addToFile(text + "\n");
    }

    @Override
    protected void printTextInNormalModeImpl(String text, TextStyle textStyle) throws Exception {
        addToFile(text + "\n");
    }

    @Override
    protected void waitPendingOperationsImpl() throws Exception {
        /* NOP */
    }

    @Override
    protected void openShiftImpl(int operatorCode, String operatorName) throws Exception {
        checkShiftClosed();

        if (!availableShifts() || !availableDocuments()) {
            final String message = !availableShifts() ? "Available shifts is 0" : "Available docs is 0";
            addToFile(message);
            throw new Exception(message);
        }

        state.open(new Operator(operatorName, (byte) operatorCode));
        state.decrementAvailableSpaceForDocs();
        state.decrementAvailableSpaceForShifts();

        StringBuilder builder = new StringBuilder();
        addPreLineToBuilder(builder);
        builder.append(String.format(Locale.getDefault(), "СПДН # %1$d", state.getLastSPDN()))
                .append("\n");
        builder.append(String.format(Locale.getDefault(), "ККМ # %1$s", state.getKkmNumber()))
                .append("\n");
        builder.append(String.format(Locale.getDefault(), "Открытие смены # %1$d", state.getLastShiftNum()))
                .append("\n");
        builder.append(String.format(Locale.getDefault(), "Оператор: %1$d %2$s", operatorCode, operatorName))
                .append("\n");
        addPostLineToBuilder(builder);
        addToFile(builder.toString());
    }

    @Override
    protected void setCashierImpl(int operatorCode, String operatorName) throws Exception {
        state.setOperator(new Operator(operatorName, (byte) operatorCode));
    }

    @Override
    protected boolean isShiftOpenedImpl() throws Exception {
        return state.isOpened();
    }

    @Override
    protected Date getDateImpl() throws Exception {
        return state.getPrinterDate();
    }

    @Override
    protected int getLastSPNDImpl() throws Exception {
        return state.getLastSPDN();
    }

    @Override
    protected Date getLastCheckTimeImpl() throws Exception {
        return state.getLastCheckTime();
    }

    @Override
    protected int getShiftNumImpl() throws Exception {
        return state.getLastShiftNum();
    }

    @Override
    protected void printZReportImpl() throws Exception {
        state.close();
        state.decrementAvailableSpaceForDocs();
        StringBuilder builder = new StringBuilder();
        addPreLineToBuilder(builder);
        builder.append("Отчет закрытия смены").append("\n");
        builder.append(alignWidth("Сумма по фискальнику", " =" + state.getTotalForShift())).append("\n");
        rollToBuilder(1, builder);
        addPostLineToBuilder(builder);
        addToFile(builder.toString());
    }

    @Override
    protected void setHeaderLinesImpl(List<String> headerLines) throws Exception {
        state.setHeadersLine(headerLines);
    }

    @Override
    protected void setVatValueImpl(int vatID, int vatValue) throws Exception {
        state.setVatValue(vatID, vatValue);
    }

    @Override
    protected int getVatValueImpl(int vatID) throws Exception {
        return state.getVatValue(vatID);
    }

    @Override
    protected void printBarcodeImpl(byte[] data) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonUtils.bytesToHexWithSpaces(data));
        rollToBuilder(2, builder);
        addToFile(builder.toString());
        writeBarcodeToImage(data);
    }

    private void writeBarcodeToImage(byte[] data) {
        String fileName = String.format(Locale.getDefault(), "%1$d.bin", System.currentTimeMillis());
        File file = new File(workingDir, fileName);
        try {
            if (file.createNewFile()) {
                FileUtils.writeByteArrayToFile(file, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected long getOdometerValueImpl() throws Exception {
        return state.getOdometerValue();
    }

    @Override
    protected String getINNImpl() throws Exception {
        return state.getInn();
    }

    @Override
    protected String getRegNumberImpl() throws Exception {
        return state.getRegisterNumber();
    }

    @Override
    protected String getEKLZNumberImpl() throws Exception {
        return state.getEklz();
    }

    @Override
    protected String getFNSerialImpl() throws Exception {
        return null;
    }

    @Override
    protected String getModelImpl() throws Exception {
        return Model;
    }

    @Override
    protected BigDecimal getCashInFRImpl() throws Exception {
        return state.getTotal();
    }

    @Override
    protected long getAvailableSpaceForShiftsImpl() throws Exception {
        return state.getAvailableSpaceForShifts();
    }

    @Override
    protected void printAdjustingTableImpl() throws Exception {
        StringBuilder builder = new StringBuilder();
        rollToBuilder(1, builder);
        builder.append("Настроечная таблица").append("\n");
        rollToBuilder(1, builder);
        addToFile(builder.toString());
    }

    @Override
    protected void startFiscalDocumentImpl(DocType docType) throws Exception {
        checkShiftOpen();
        StringBuilder builder = new StringBuilder();
        addPreLineToBuilder(builder);
        if (!shiftIsOpenLessOneDay()) {
            builder.append("Превышен интервал открытой смены").append("\n");
            rollToBuilder(1, builder);
            addPostLineToBuilder(builder);
            addToFile(builder.toString());
            throw new ShiftTimeOutException();
        }

        List<String> headers = state.getHeaderLines();
        int index = 0;
        for (String line : headers) {
            if (index == MAX_HEADERS_LINE) break;
            index++;
            builder.append(line).append("\n");
        }
        Operator operator = state.getOperator();
        builder.append(operator.getOperatorName()).append("\n");
        builder.append("ККМ # ").append(state.getKkmNumber()).append("\n");
        transaction = new FiscalTransaction(docType, state.getLastSPDN() + 1);
        addToFile(builder.toString());
    }

    @Override
    protected void addItemImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {
        checkShiftOpen();
        checkIsStartFiscalDoc();

        StringBuilder builder = new StringBuilder();
        builder.delete(0, builder.length());
        try {
            BigDecimal nds = vatRate == null ? BigDecimal.ZERO : Decimals.getVATValueIncludedFromRate(amount, vatRate, Decimals.RoundMode.WITHOUT);
            transaction.addItem(description, amount, nds);
            builder.append(alignWidth(description, " =" + amount)).append("\n");
            if (nds.compareTo(BigDecimal.ZERO) != 0) {
                //выводим в случае если значение налоговой ставки больше 0
                builder.append(alignWidth("ВКЛ. НДС", String.valueOf(nds))).append("\n");
            }
        } catch (TransactionException ex) {
            builder.append(ex.getMessage()).append("\n");
            rollToBuilder(1, builder);
            builder.append("Отмена фискальной операции").append("\n");
            throw new Exception(ex);
        } finally {
            addToFile(builder.toString());
        }
    }

    @Override
    protected void addItemRefundImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {

        StringBuilder builder = new StringBuilder();
        try {
            BigDecimal nds = vatRate == null ? BigDecimal.ZERO : Decimals.getVATValueIncludedFromRate(amount, vatRate, Decimals.RoundMode.WITHOUT);
            transaction.addItemRefund(description, amount, nds);
            builder.append(alignWidth(description, " =" + amount)).append("\n");
            if (nds.compareTo(BigDecimal.ZERO) != 0) {
                //выводим в случае если значение налоговой ставки больше 0
                builder.append(alignWidth("ВКЛ. НДС", String.valueOf(nds))).append("\n");
            }
        } catch (TransactionException ex) {
            builder.append(ex.getMessage()).append("\n");
            rollToBuilder(1, builder);
            builder.append("Отмена фискальной операции").append("\n");
            throw new Exception(ex);
        } finally {
            addToFile(builder.toString());
        }
    }

    @Override
    protected void addDiscountImpl(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws Exception {
        checkIsStartFiscalDoc();
        StringBuilder builder = new StringBuilder();
        try {
            BigDecimal vatValue = vatRate == null ? BigDecimal.ZERO : Decimals.getVATValueIncludedFromRate(newAmount, vatRate, Decimals.RoundMode.WITHOUT);
            BigDecimal nds = vatRate == null ? BigDecimal.ZERO : Decimals.getVATValueIncludedFromRate(newAmount, vatRate, Decimals.RoundMode.WITHOUT);
            //если тут небыло добавлено позиций, то вылетит исключение
            transaction.addDiscount(discount, newAmount, nds);
            builder.append(alignWidth("", "=" + newAmount)).append("\n");
            if (!Double.valueOf(0D).equals(vatValue)) {
                //выводим в случае если значение налоговой ставки больше 0
                builder.append(alignWidth("ВКЛ. НДС", String.valueOf(vatValue))).append("\n");
            }
        } catch (TransactionException ex) {
            builder.append(ex.getMessage()).append("\n");
            rollToBuilder(1, builder);
            builder.append("Отмена фискальной операции").append("\n");
            throw new Exception(ex);
        } finally {
            addToFile(builder.toString());
        }
    }

    @Override
    protected void printTotalImpl(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws Exception {
        StringBuilder builder = new StringBuilder();
        try {
            transaction.printTotal(total, payment, paymentType);

            BigDecimal shortChange = payment.subtract(total);

            if (Decimals.lessThanZero(shortChange)) {
                throw new Exception(" total(" + total + ") > payment(" + payment + ")");
            }

            builder.append(alignWidth("ИТОГ", " =" + total)).append("\n");
            builder.append(alignWidth("НАЛИЧНЫМИ", " =" + payment)).append("\n");

            if (!Decimals.isZero(shortChange)) {
                builder.append(alignWidth("СДАЧА", " =" + shortChange)).append("\n");
            }

            String inn = state.getInn();
            String registerNumber = state.getRegisterNumber();
            int spdnNumber = state.getLastSPDN() + 1; // к этому моменту спдн еще не увеличен, поэтому вручную увеличим значение на 1
            Date printDate = state.getPrinterDate();

            String spdnText = "КО1 #" + spdnNumber;
            String printDateText = DateFormatOperations.getDateForOut(printDate);

            builder.append(alignWidth(spdnText, printDateText)).append("\n");
            builder.append(alignWidth("ИНН", inn)).append("\n");
            builder.append(alignWidth("РН", registerNumber)).append("\n");
            rollToBuilder(1, builder);
            addPostLineToBuilder(builder);
        } catch (TransactionException ex) {
            builder.delete(0, builder.length());
            builder.append(ex.getMessage()).append("\n");
            rollToBuilder(1, builder);
            builder.append("Отмена фискальной операции").append("\n");
            addPostLineToBuilder(builder);
            throw new Exception(ex);
        } finally {
            addToFile(builder.toString());
        }
    }

    @Override
    protected List<ClosedShiftInfo> getShiftsInfoImpl(int startNum, int endNum) throws Exception {
        return Collections.emptyList();
    }

    @Override
    protected int getWidthForTextStyleImpl(TextStyle textStyle) {
        return TAPE_WIDTH;
    }

    @Override
    protected boolean isFederalLaw54SupportedImpl() {
        return true;
    }

    @Override
    protected void setCustomerPhoneNumberImpl(String phoneNumber) throws Exception {
        // nop
    }

    @Override
    protected void setCustomerEmailImpl(String email) throws Exception {
        // nop
    }

    @Override
    protected void printNotSentDocsReportImpl() throws Exception {
        addToFile("Отчет о непереданных в ОФД документах" + "\n");
    }

    @Override
    protected void printCorrectionReceiptImpl(DocType docType, BigDecimal total) throws Exception {
        // В будущем: 09.06.2017 Нужно реализовать приход/расход по кассе на сумму total
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void printDuplicateReceiptImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected OfdSettings getOfdSettingsImpl() throws Exception {
        return ofdSettings;
    }

    @Override
    protected void setOfdSettingsImpl(OfdSettings ofdSettings) throws Exception {
        this.ofdSettings.setTimeout(ofdSettings.getTimeout());
        this.ofdSettings.setIp(ofdSettings.getIp());
        this.ofdSettings.setPort(ofdSettings.getPort());
    }

    @Override
    protected void scrollPaperInNormalModeImpl(int linesCount) throws Exception {
        for (int i = 0; i < linesCount; i++)
            addToFile("\n");
    }

    @Override
    protected OfdDocsState getOfdDocsStateImpl() throws Exception {
        return ofdDocsState;
    }

    @Override
    protected void startSendingDocsToOfdImpl() throws Exception {

    }

    private void rollToBuilder(int lines, StringBuilder builder) {

        Preconditions.checkArgument(lines > 0);
        for (int i = 0; i < lines; i++) {
            builder.append("\n");
        }
    }

    /**
     * Добавляет строку, которая обозначает начало документа
     */
    private void addPreLineToBuilder(StringBuilder builder) {

        builder.append("\n")
                .append(alignCenter(" --- START DOC ---"))
                .append("\n");
    }

    /**
     * Добавляет строку, которая обозначает конец документа
     */
    private void addPostLineToBuilder(StringBuilder builder) {
        builder.append("\n").append(alignCenter(" --- END DOC ---"));
        rollToBuilder(2, builder);
    }

    private void addToFile(String text) {
        try {
            state.appendOdometrValue(calculateLineCounts(text) * ONE_LINE_HEIGHT);
            FileUtils.writeStringToFile(fileToWrite, text, true);
        } catch (IOException e) {
            throw new IllegalStateException("Error write to file - " + e.getMessage(), e);
        }
    }

    private int calculateLineCounts(@NonNull String text) {
        String[] linesArray = text.split("\n");
        int size = linesArray.length;
        return size == 0 ? 1 : size;
    }

    private void checkShiftOpen() throws Exception {
        if (!state.isOpened())
            throw new Exception("Shift is not open");
    }

    @Override
    protected void endFiscalDocumentImpl(DocType docType) throws Exception {
        transaction.endTransaction(docType);
        Check check = transaction.build();
        state.saveCheck(check);
        state.decrementAvailableSpaceForDocs();
    }

    /**
     * Проверяет продолжительность открытия смены
     *
     * @return true - смена открыта меньше суток либо смен не было, false смена открыта больше суток
     */
    private boolean shiftIsOpenLessOneDay() {
        boolean result = true;
        Date openShiftTime = state.getOpenDate();
        if (openShiftTime != null) {
            Calendar currentTime = Calendar.getInstance();
            // Календарь для определения времени закрытия смены
            Calendar closeTimeCalendar = Calendar.getInstance();
            closeTimeCalendar.setTime(openShiftTime);
            closeTimeCalendar.add(Calendar.HOUR_OF_DAY, 24);
            if (currentTime.after(closeTimeCalendar)) {
                result = false;
            }
        }
        return result;
    }

    private void checkShiftClosed() throws Exception {
        if (state.isOpened()) {
            throw new Exception("Shift not closed");
        }
    }

    private void checkIsStartFiscalDoc() throws Exception {
        if (!transaction.isStarted()) {
            throw new Exception("Transaction is not started");
        }
    }

    private boolean availableDocuments() {
        return state.getAvailableSpaceForDocs() != 0;
    }

    private boolean availableShifts() {
        return state.getAvailableSpaceForShifts() != 0;
    }

    private void connectToDb() {
        if (state != null) {
            return;
        }

        state = new StateVirtual(printerStateStorage);
    }

    private void disconnectFromDb() {
        if (state == null) {
            return;
        }
        state.closeState();
        state = null;
    }

    /**
     * Выравнивает текст по краям.
     *
     * @param first  Первое слово, будет с левой стороны
     * @param second Второе слово, будет с правой стороны
     * @return Строка с выравниванием по краям
     */
    private String alignWidth(String first, String second) {

        StringBuilder alignString = new StringBuilder();

        int countSpace = TAPE_WIDTH - (first.length() + second.length());

        if (countSpace < 0) {
            return alignString.append(first).append(" ").append(second).substring(0, TAPE_WIDTH);
        }

        alignString.append(first);
        for (int i = 0; i < countSpace; i++) {
            alignString.append(' ');
        }
        alignString.append(second);

        return alignString.toString();
    }

    /**
     * Выравнивает строку по центру.
     *
     * @param text Текст
     * @return Строка с выравниванием по центру
     */
    public String alignCenter(String text) {

        if (text.length() > TAPE_WIDTH) {
            return text.substring(0, TAPE_WIDTH);
        }

        StringBuilder builder = new StringBuilder();

        int textLen = text.length();
        int countSpace = TAPE_WIDTH - textLen;
        int leftCountSpace = countSpace / 2;
        //добавляем пробелы слева
        for (int i = 0; i < leftCountSpace; i++) {
            builder.append(' ');
        }
        //добавляем текст
        builder.append(text);

        return builder.toString();
    }
}
