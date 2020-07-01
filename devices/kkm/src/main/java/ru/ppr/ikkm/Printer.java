package ru.ppr.ikkm;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;

public abstract class Printer implements IPrinter {
    private static final String TAG = Logger.makeLogTag(Printer.class);

    public static final long OPEN_SHIFT_MAX_ENABLED_TIME = 1 * 60 * 60 * 1000;

    private final Timeouts timeouts = new Timeouts();
    private ExecutorService executorService;
    private List<String> methodCallTrace;
    /**
     * Флаг, подключен ли сейчас принтер
     */
    private boolean connected = false;

    protected Printer() {
        executorService = Executors.newSingleThreadExecutor((r) -> new Thread(r, "PrinterFuture"));
        methodCallTrace = new ArrayList<>();
    }

    private void onTimeOut() {
        try {
            reInitializeWithDriver();
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
    }

    private interface Wrapper<T> {
        T wrappedCode() throws Exception;

        Method wrappedMethod();

        long getTimeoutInSeconds();
    }

    private class Method {

        private String name;
        private String exemplar;

        private Method(String name) {
            this.name = name;
            exemplar = name + "()";
        }

        private Method setExemplar(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(name).append("(");

            if (params != null)
                for (int i = 0; i < params.length; i++)
                    if (i == params.length - 1)
                        stringBuilder.append(params[i]);
                    else
                        stringBuilder.append(params[i] + ", ");

            exemplar = stringBuilder.append(")").toString();

            return this;
        }

        private String getExemplar() {
            return exemplar;
        }

    }

    private <T> T call(final Wrapper<T> wrapper) throws PrinterException {
        methodCallTrace.add(wrapper.wrappedMethod().getExemplar());

        Exception eWithOuterStackTrace = new Exception("It's outer stack trace for next error log");
        eWithOuterStackTrace.setStackTrace(Thread.currentThread().getStackTrace());

        Future<T> task = executorService.submit(wrapper::wrappedCode);
        try {
            return task.get(wrapper.getTimeoutInSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            for (String str : methodCallTrace) {
                Logger.info(TAG, "printerTrace." + str + ";");
            }
            methodCallTrace.clear();
            Logger.error(TAG, eWithOuterStackTrace.getMessage(), eWithOuterStackTrace);
            Logger.error(TAG, e.getMessage(), e);

            if (e instanceof TimeoutException) {
                onTimeOut();
            }

            if (e instanceof ExecutionException) {

                final Throwable originalException = e.getCause();

                if (originalException instanceof PrinterException) {
                    throw (PrinterException) originalException;
                }

                if (TextUtils.equals(e.getMessage(), "java.io.UnsupportedEncodingException: len is less")) {
                    // На самом деле надо искать проблему в SDK
                    // А так, хоть можно будет переподключиться
                    // Иначе будем ловить len is less до переиницалиции Printer
                    // Актуально для старого СДК от зебры
                    onTimeOut();
                }
            }

            throw new PrinterException(e);
        }
    }

    protected Timeouts getTimeouts() {
        return timeouts;
    }

    /**
     * Выполняет переинициализацию принтера
     *
     * @throws Exception в случае возникновения ошибки
     */
    private void reInitializeWithDriver() throws Exception {
        Logger.trace(TAG, "reInitializeWithDriver");
        terminateImpl();
        initializeWithDriverImpl();
    }

    /**
     * Проверяет подключение, поглощая Exception
     *
     * @return
     */
    private boolean checkConnection() {
        try {
            return checkConnectionWithDriver();
        } catch (PrinterException e) {
            Logger.error(TAG, e);
        }
        return false;
    }

    /**
     * Выполняет подключение к принтеру.
     *
     * @return Результат подключения.
     * @throws PrinterIsNotConnectedException если результат подключения {@link IPrinter.ConnectResult#NOT_CONNECTED}
     */
    public IPrinter.ConnectResult connectAndThrowOnFail() throws PrinterIsNotConnectedException {
        IPrinter.ConnectResult connectResult = connectInternal();
        if (connectResult == IPrinter.ConnectResult.ALREADY_CONNECTED || connectResult == IPrinter.ConnectResult.NOW_CONNECTED) {
            return connectResult;
        }
        throw new PrinterIsNotConnectedException();
    }

    /**
     * Выполняет подключение к принтеру
     *
     * @return Результат подключения.
     */
    private IPrinter.ConnectResult connectInternal() {
        try {
            if (connected) {
                // Якобы уже подключен, проверяем подключение
                connected = checkConnection();
                if (connected) {
                    // Да, всё курто, уже подключены
                    Logger.info(TAG, "already is connected");
                    return IPrinter.ConnectResult.ALREADY_CONNECTED;
                } else {
                    // Косяк, возможно выключали блютуз, или принтер, в
                    // общем, соединение обрывалось, a SDK не отселедило
                    Logger.info(TAG, "really is not connected");
                }
            }

            try {
                // Собственно, подключение
                connectWithDriver();
                connected = true;
            } catch (Exception e) {
                Logger.error(TAG, e.getMessage(), e);
            }
//
            if (connected) {
                // Мы смогли подключиться, проверяем подключение
                connected = checkConnection();
                if (!connected) {
                    // Печаль, говорит, что подключен, а проверку не проходит.
                    // Отключаемся
                    disconnectInternal(true);
                    // Всё, т.к. проверку не проходит, снова отключились и ушли
                    return IPrinter.ConnectResult.NOT_CONNECTED;
                } else {
                    // Чудо, и подключились, и проверка пройдена, всё хорошо
                    return IPrinter.ConnectResult.NOW_CONNECTED;
                }
            } else {
                // Не смогли подключиться
                return IPrinter.ConnectResult.NOT_CONNECTED;
            }

        } catch (Exception e) {
            // Что-то пошло не так, не удалось подключиться
            Logger.error(TAG, e.getMessage(), e);
            return IPrinter.ConnectResult.NOT_CONNECTED;
        }

    }

    /**
     * Выполняет отключение от принтера
     *
     * @param useTimeOut Обернуть во Future c таймаутом, если {@code true}
     */
    protected void disconnectInternal(final boolean useTimeOut) {
        try {
            if (useTimeOut) {
                disconnectWithDriver();
            } else {
                disconnectWithDriverImpl();
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage(), e);
        }
        connected = false;
    }

    @Override
    public ConnectResult connect() throws PrinterException {
        Logger.trace(TAG, "connect");
        ConnectResult connectResult = connectAndThrowOnFail();
        onConnectionEstablished(connectResult);
        return connectResult;
    }

    @Override
    public void disconnect() throws PrinterException {
        Logger.trace(TAG, "disconnect");
        disconnectInternal(true);
    }

    @Override
    public void prepareResources() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                prepareResourcesImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("prepareResources");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrepareResourcesTimeout();
            }

        });
    }

    @Override
    public void freeResources() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                freeResourcesImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("freeResources");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFreeResourcesTimeout();
            }

        });
    }

    @Override
    public boolean checkConnectionWithDriver() throws PrinterException {
        return call(new Wrapper<Boolean>() {

            @Override
            public Boolean wrappedCode() throws Exception {
                boolean res = checkConnectionWithDriverImpl();
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("checkConnectionWithDriver");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getCheckConnectionTimeout();
            }

        });
    }

    @Override
    public void connectWithDriver() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                connectWithDriverImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("connectWithDriver");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getConnectTimeout();
            }

        });
    }

    @Override
    public void disconnectWithDriver() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                disconnectWithDriverImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("disconnectWithDriver");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getDisconnectTimeout();
            }

        });
    }

    @Override
    public void terminate() throws PrinterException {
        Logger.trace(TAG, "terminate");
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                terminateImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("terminate");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getTerminateTimeout();
            }

        });
    }

    private void onConnectionEstablished(ConnectResult connectResult) throws PrinterException {
        Logger.trace(TAG, "onConnectionEstablished");
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                onConnectionEstablishedImpl(connectResult);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("onConnectionEstablished");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getConnectionEstablishedTimeout();
            }

        });
    }

    @Override
    public void printTextInFiscalMode(String text) throws PrinterException {
        printTextInFiscalMode(text, TextStyle.FISCAL_NORMAL);
    }

    @Override
    public void printTextInFiscalMode(String text, TextStyle textStyle) throws PrinterException {
        Logger.info(TAG, "printTextInFiscalMode, text = " + text + ", textStyle = " + textStyle);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printTextInFiscalModeImpl(text, textStyle);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printTextInFiscalMode").setExemplar("text: " + text, "textStyle: " + textStyle);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintTextTimeout();
            }

        });
    }

    @Override
    public void printTextInNormalMode(String text) throws PrinterException {
        printTextInNormalMode(text, TextStyle.TEXT_NORMAL);
    }

    @Override
    public void printTextInNormalMode(String text, TextStyle textStyle) throws PrinterException {
        Logger.info(TAG, "printTextInNormalMode, text = " + text + ", textStyle = " + textStyle);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printTextInNormalModeImpl(text, textStyle);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printTextInNormalMode").setExemplar("text: " + text, "textStyle: " + textStyle);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintTextTimeout();
            }

        });
    }


    @Override
    public void waitPendingOperations() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                waitPendingOperationsImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("waitPendingOperations");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getWaitPendingOperationsTimeout();
            }

        });
    }

    @Override
    public void openShift(int operatorCode, String operatorName) throws PrinterException {
        Logger.info(TAG, "openShift, operatorCode = " + operatorCode + ", operatorName = " + operatorName);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                openShiftImpl(operatorCode, operatorName);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("openShift").setExemplar("operatorCode: " + operatorCode, "operatorName: " + operatorName);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getOpenShiftTimeout();
            }

        });
    }

    @Override
    public void setCashier(int operatorCode, String operatorName) throws PrinterException {
        Logger.info(TAG, "setCashier, operatorCode = " + operatorCode + ", operatorName = " + operatorName);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setCashierImpl(operatorCode, operatorName);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setCashier").setExemplar("operatorCode: " + operatorCode, "operatorName: " + operatorName);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetCashierTimeout();
            }

        });
    }

    @Override
    public boolean isShiftOpened() throws PrinterException {
        return call(new Wrapper<Boolean>() {

            @Override
            public Boolean wrappedCode() throws Exception {
                return isShiftOpenedImpl();
            }

            @Override
            public Method wrappedMethod() {
                return new Method("isShiftOpened");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public Date getDate() throws PrinterException {
        return call(new Wrapper<Date>() {

            @Override
            public Date wrappedCode() throws Exception {
                Date res = getDateImpl();
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getDate");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetDataTimeout();
            }

        });
    }

    @Override
    public int getLastSPND() throws PrinterException {
        return call(new Wrapper<Integer>() {

            @Override
            public Integer wrappedCode() throws Exception {
                int res = getLastSPNDImpl();
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getLastSPND");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public Date getLastCheckTime() throws PrinterException {
        return call(new Wrapper<Date>() {

            @Override
            public Date wrappedCode() throws Exception {
                Date res = getLastCheckTimeImpl();
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getLastCheckTime");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public int getShiftNum() throws PrinterException {
        return call(new Wrapper<Integer>() {

            @Override
            public Integer wrappedCode() throws Exception {
                int res = getShiftNumImpl();
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getShiftNum");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void printZReport() throws PrinterException {
        Logger.info(TAG, "printZReport");
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printZReportImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printZReport");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintZReportTimeout();
            }

        });
    }

    @Override
    public void setHeaderLines(List<String> headerLines) throws PrinterException {
        Logger.info(TAG, "setHeaderLines, headerLinesSize = " + headerLines.size());
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setHeaderLinesImpl(headerLines);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setHeaderLines").setExemplar("headerLines: " + headerLines);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetHeadersTimeout();
            }

        });
    }

    @Override
    public void setVatValue(int vatID, int vatValue) throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setVatValueImpl(vatID, vatValue);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setVatValue").setExemplar("vatID: " + vatID, "vatValue: " + vatValue);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetDataTimeout();
            }

        });
    }

    @Override
    public int getVatValue(int vatID) throws PrinterException {
        return call(new Wrapper<Integer>() {

            @Override
            public Integer wrappedCode() throws Exception {
                int res = getVatValueImpl(vatID);
                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getVatValue").setExemplar("vatID: " + vatID);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void printBarcode(byte[] data) throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printBarcodeImpl(data);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printBarcode").setExemplar("data: " + data);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintBarcodeTimeout();
            }

        });
    }

    @Override
    public void printAdjustingTable() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printAdjustingTableImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printAdjustingTable");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintAdjustingTableTimeout();
            }

        });
    }

    @Override
    public long getOdometerValue() throws PrinterException {
        return call(new Wrapper<Long>() {

            @Override
            public Long wrappedCode() throws Exception {
                long res = getOdometerValueImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getOdometerValue");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public String getINN() throws PrinterException {
        return call(new Wrapper<String>() {

            @Override
            public String wrappedCode() throws Exception {
                String res = getINNImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getINN");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public String getRegNumber() throws PrinterException {
        return call(new Wrapper<String>() {

            @Override
            public String wrappedCode() throws Exception {
                String res = getRegNumberImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getRegNumber");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public String getEKLZNumber() throws PrinterException {
        return call(new Wrapper<String>() {

            @Override
            public String wrappedCode() throws Exception {
                String res = getEKLZNumberImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getEKLZNumber");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public String getFNSerial() throws PrinterException {
        return call(new Wrapper<String>() {

            @Override
            public String wrappedCode() throws Exception {
                String res = getFNSerialImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getFNSerial");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public String getModel() throws PrinterException {
        return call(new Wrapper<String>() {

            @Override
            public String wrappedCode() throws Exception {
                String res = getModelImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getModel");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public BigDecimal getCashInFR() throws PrinterException {
        return call(new Wrapper<BigDecimal>() {

            @Override
            public BigDecimal wrappedCode() throws Exception {
                BigDecimal res = getCashInFRImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getCashInFR");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public long getAvailableSpaceForShifts() throws PrinterException {
        return call(new Wrapper<Long>() {

            @Override
            public Long wrappedCode() throws Exception {
                long res = getAvailableSpaceForShiftsImpl();

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getAvailableSpaceForShifts");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void startFiscalDocument(DocType docType) throws PrinterException {
        Logger.info(TAG, "startFiscalDocument, docType = " + docType);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                startFiscalDocumentImpl(docType);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("startFiscalDocument").setExemplar("docType: " + docType);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public void endFiscalDocument(DocType docType) throws PrinterException {
        Logger.info(TAG, "endFiscalDocument, docType = " + docType);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                endFiscalDocumentImpl(docType);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("endFiscalDocument").setExemplar("docType: " + docType);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getEndFiscalDocumentTimeout();
            }

        });
    }

    @Override
    public void addItem(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws PrinterException {
        Logger.info(TAG, "addItem, description = " + description + ", amount = " + amount + ", vatRate = " + vatRate);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                addItemImpl(description, amount, vatRate);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("addItem").setExemplar("description: " + description, "amount: " + amount, "vatRate: " + vatRate);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public void addItemRefund(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws PrinterException {
        Logger.info(TAG, "addItemRefund, description = " + description + ", amount = " + amount + ", vatRate = " + vatRate);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                addItemRefundImpl(description, amount, vatRate);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("addItem").setExemplar("description: " + description, "amount: " + amount, "vatRate: " + vatRate);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public void addDiscount(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws PrinterException {
        Logger.info(TAG, "addDiscount, discount = " + discount + ", newAmount = " + newAmount + ", vatRate = " + vatRate);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                addDiscountImpl(discount, newAmount, vatRate);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("addDiscount").setExemplar("discount: " + discount, "newAmount: " + newAmount, "vatRate: " + vatRate);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public List<ClosedShiftInfo> getShiftsInfo(int startNum, int endNum) throws PrinterException {
        return call(new Wrapper<List<ClosedShiftInfo>>() {

            @Override
            public List<ClosedShiftInfo> wrappedCode() throws Exception {
                List<ClosedShiftInfo> res = getShiftsInfoImpl(startNum, endNum);

                return res;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getShiftsInfo").setExemplar("startNum: " + startNum, "endNum: " + endNum);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void printTotal(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws PrinterException {
        Logger.info(TAG, "printTotal, total = " + total + ", payment = " + payment + ", paymentType = " + paymentType);
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printTotalImpl(total, payment, paymentType);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printTotal").setExemplar("total: " + total, "payment: " + payment, "paymentType: " + paymentType);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public int getWidthForTextStyle(TextStyle textStyle) {
        return getWidthForTextStyleImpl(textStyle);
    }

    @Override
    public boolean isFederalLaw54Supported() {
        return isFederalLaw54SupportedImpl();
    }

    @Override
    public void setCustomerPhoneNumber(String phoneNumber) throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setCustomerPhoneNumberImpl(phoneNumber);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setCustomerPhoneNumber").setExemplar("phoneNumber: " + phoneNumber);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetDataTimeout();
            }

        });
    }

    @Override
    public void setCustomerEmail(String email) throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setCustomerEmailImpl(email);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setCustomerEmail").setExemplar("email: " + email);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetDataTimeout();
            }

        });
    }

    @Override
    public void printNotSentDocsReport() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printNotSentDocsReportImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printNotSentDocsReport");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintNotSentDocsReportTimeout();
            }

        });
    }

    @Override
    public void printCorrectionReceipt(DocType docType, BigDecimal total) throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printCorrectionReceiptImpl(docType, total);

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printCorrectionReceipt").setExemplar("docType: " + docType, "total: " + total);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public void printDuplicateReceipt() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                printDuplicateReceiptImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("printDuplicateReceipt");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }

    @Override
    public void scrollPaperInNormalMode(int linesCount) throws PrinterException {
        Logger.info(TAG, "scrollPaperInNormalMode(" + linesCount + ")");
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                scrollPaperInNormalModeImpl(linesCount);
                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("scrollPaperInNormalMode").setExemplar("linesCount: " + linesCount);
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getPrintTextTimeout();
            }

        });
    }

    @Override
    public OfdSettings getOfdSettings() throws PrinterException {
        return call(new Wrapper<OfdSettings>() {

            @Override
            public OfdSettings wrappedCode() throws Exception {
                OfdSettings ofdSettings = getOfdSettingsImpl();
                return ofdSettings;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getOfdSettings");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void setOfdSettings(OfdSettings ofdSettings) throws PrinterException {
        Logger.info(TAG, "setOfdSettings(" + ofdSettings.toString() + ")");
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                setOfdSettingsImpl(ofdSettings);
                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("setOfdSettings").setExemplar("ofdSettings: " + ofdSettings.toString());
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getSetDataTimeout();
            }

        });
    }

    @Override
    public OfdDocsState getOfdDocsState() throws PrinterException {
        return call(new Wrapper<OfdDocsState>() {

            @Override
            public OfdDocsState wrappedCode() throws Exception {
                OfdDocsState ofdDocsState = getOfdDocsStateImpl();
                return ofdDocsState;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("getOfdDocsState");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getGetDataTimeout();
            }

        });
    }

    @Override
    public void startSendingDocsToOfd() throws PrinterException {
        call(new Wrapper<Void>() {

            @Override
            public Void wrappedCode() throws Exception {
                startSendingDocsToOfdImpl();

                return null;
            }

            @Override
            public Method wrappedMethod() {
                return new Method("startSendingDocsToOfd");
            }

            @Override
            public long getTimeoutInSeconds() {
                return getTimeouts().getFiscalCommandTimeout();
            }

        });
    }


    /////////////

    /**
     * Подготавливает окружение принтера (Например, включает Bluetooth)
     *
     * @throws Exception
     */
    protected abstract void prepareResourcesImpl() throws Exception;

    /**
     * Подготавливает окружение принтера (Например, выключает Bluetooth)
     *
     * @throws Exception
     */
    protected abstract void freeResourcesImpl() throws Exception;

    protected abstract void initializeWithDriverImpl() throws Exception;

    protected abstract boolean checkConnectionWithDriverImpl() throws Exception;

    protected abstract void connectWithDriverImpl() throws Exception;

    public abstract void disconnectWithDriverImpl() throws Exception;

    protected abstract void terminateImpl() throws Exception;

    protected abstract void onConnectionEstablishedImpl(ConnectResult connectResult) throws Exception;

    /**
     * Печатает произвольный текст в фискальном документе
     *
     * @param text      Текст для печати
     * @param textStyle Стиль текста
     * @throws Exception В случае ошибки
     */
    protected abstract void printTextInFiscalModeImpl(String text, TextStyle textStyle) throws Exception;

    /**
     * Печатает текст на ленте.
     *
     * @param text      Текст для печати
     * @param textStyle Стиль текста
     * @throws Exception В случае ошибки
     */
    protected abstract void printTextInNormalModeImpl(String text, TextStyle textStyle) throws Exception;

    /**
     * Ожидает, пока очередь команд, отправленных на принтер, будет им успешно обработана
     *
     * @throws Exception
     */
    protected abstract void waitPendingOperationsImpl() throws Exception;

    /**
     * Открывает смену на принтере
     *
     * @param operatorCode код оператора
     * @param operatorName имя оператора
     * @throws Exception
     */
    protected abstract void openShiftImpl(int operatorCode, String operatorName) throws Exception;

    /**
     * Устанавливает оператора
     *
     * @param operatorCode код оператора
     * @param operatorName имя оператора
     * @throws Exception
     */
    protected abstract void setCashierImpl(int operatorCode, String operatorName) throws Exception;

    protected abstract boolean isShiftOpenedImpl() throws Exception;

    protected abstract Date getDateImpl() throws Exception;

    protected abstract int getLastSPNDImpl() throws Exception;

    protected abstract Date getLastCheckTimeImpl() throws Exception;

    protected abstract int getShiftNumImpl() throws Exception;

    /**
     * Печатает Z отчет и закрывает смену
     *
     * @throws Exception
     */
    protected abstract void printZReportImpl() throws Exception;

    /**
     * Устанавливает заголовки
     *
     * @param headerLines массив заголовков
     * @throws Exception
     */
    protected abstract void setHeaderLinesImpl(List<String> headerLines) throws Exception;

    /**
     * Добавляет налоговую ставку
     *
     * @param vatID    ид ставки от 1 до 15
     * @param vatValue значение ставки от 1 – это 0.1% до 999 – это 99.9%.
     * @throws Exception
     */
    protected abstract void setVatValueImpl(int vatID, int vatValue) throws Exception;

    /**
     * Возвращает налоговую ставку по индексу
     *
     * @param vatID ид ставки
     * @return
     * @throws Exception
     */
    protected abstract int getVatValueImpl(int vatID) throws Exception;

    protected abstract void printBarcodeImpl(byte[] data) throws Exception;

    protected abstract long getOdometerValueImpl() throws Exception;

    protected abstract String getINNImpl() throws Exception;

    protected abstract String getRegNumberImpl() throws Exception;

    protected abstract String getEKLZNumberImpl() throws Exception;

    protected abstract String getFNSerialImpl() throws Exception;

    protected abstract String getModelImpl() throws Exception;

    protected abstract BigDecimal getCashInFRImpl() throws Exception;

    protected abstract long getAvailableSpaceForShiftsImpl() throws Exception;


    /**
     * Печатает настроечную таблицу
     *
     * @throws Exception
     */
    protected abstract void printAdjustingTableImpl() throws Exception;

    /**
     * Начинает формировать фискальный документ(транзакия)
     *
     * @param docType тип документа
     * @throws Exception
     */
    protected abstract void startFiscalDocumentImpl(DocType docType) throws Exception;

    /**
     * Заканчивает формирование фискального документа(транзакции)
     *
     * @param docType тип документа
     * @throws Exception
     */
    protected abstract void endFiscalDocumentImpl(DocType docType) throws Exception;

    /**
     * Добавление товарной позации в фискальный документ
     *
     * @param description наименование товара
     * @param amount      стоимость
     * @param vatRate     налоговая ставка
     * @throws Exception
     */
    protected abstract void addItemImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception;

    /**
     * Добавление товарной позации в чек возврата
     *
     * @param description наименование товара
     * @param amount      стоимость
     * @param vatRate     налоговая ставка
     * @throws Exception
     */
    protected abstract void addItemRefundImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception;

    /**
     * Добавляет скидку для последней добавленной товарной позиции
     *
     * @param discount  размер скидки, в рублях
     * @param newAmount новая стоимость товара с учетом скидки
     * @param vatRate   налоговая ставка
     * @throws Exception
     */
    protected abstract void addDiscountImpl(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws Exception;

    /**
     * Печатает итог в чеке
     *
     * @param total       полная стоимость в чеке
     * @param payment     полученная сумма от покупателя
     * @param paymentType тип оплаты
     * @throws Exception
     */
    protected abstract void printTotalImpl(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws Exception;

    /**
     * Получает информацию по сменам
     *
     * @param startNum
     * @param endNum
     * @return
     * @throws Exception
     */
    protected abstract List<ClosedShiftInfo> getShiftsInfoImpl(int startNum, int endNum) throws Exception;

    /**
     * Возвращает ширину билетной ленты в символах для указанного стиля текста.
     * Предполагается, что данный метод не требует подключения к принтеру,
     * является очень легким, и его можно вызвать в любой момент из любого потока.
     *
     * @param textStyle Стиль текста
     * @return Ширина билетной ленты в символах
     */
    protected abstract int getWidthForTextStyleImpl(TextStyle textStyle);

    /**
     * Проверяет, поддерживает ли принтер 54 ФЗ.
     * Предполагается, что данный метод не требует подключения к принтеру,
     * является очень легким, и его можно вызвать в любой момент из любого потока.
     *
     * @return {@code true} если поддерживает, {@code false иначе}
     */
    protected abstract boolean isFederalLaw54SupportedImpl();

    /**
     * Устанавливает номер телефона покупателя для отправки чека по смс.
     *
     * @param phoneNumber Номер телефона
     */
    protected abstract void setCustomerPhoneNumberImpl(String phoneNumber) throws Exception;

    /**
     * Устанавливает e-mail покупателя для отправки чека по электронной почте.
     *
     * @param email e-mail
     */
    protected abstract void setCustomerEmailImpl(String email) throws Exception;

    /**
     * Выводит на печать отчет о непереданных в ОФД документах
     */
    protected abstract void printNotSentDocsReportImpl() throws Exception;

    /**
     * Формирует чек коррекции
     *
     * @param docType Тип документа - приход/расход
     * @param total   Сумма коррекции
     */
    protected abstract void printCorrectionReceiptImpl(DocType docType, BigDecimal total) throws Exception;

    /**
     * Печатает дкбликат последнего чека
     */
    protected abstract void printDuplicateReceiptImpl() throws Exception;

    /**
     * Вернет настройки для связи с ОФД
     *
     * @return
     * @throws Exception
     */
    protected abstract OfdSettings getOfdSettingsImpl() throws Exception;

    /**
     * Применит настройк для связи с ОФД
     *
     * @param ofdSettings
     * @throws Exception
     */
    protected abstract void setOfdSettingsImpl(OfdSettings ofdSettings) throws Exception;

    /**
     * Мотает бумагу на указанное количество строк
     *
     * @param linesCount
     * @throws Exception
     */
    protected abstract void scrollPaperInNormalModeImpl(int linesCount) throws Exception;

    /**
     * Вернет статус по неотправленным в ОФД документам
     *
     * @return
     * @throws Exception
     */
    protected abstract OfdDocsState getOfdDocsStateImpl() throws Exception;

    /**
     * Запускает отправку данных в ОФД
     *
     * @return
     * @throws Exception
     */
    protected abstract void startSendingDocsToOfdImpl() throws Exception;

    public static class Timeouts {

        private static final int TIMEOUT_PREPARE_RESOURCES = Integer.MAX_VALUE;
        private static final int TIMEOUT_FREE_RESOURCES = Integer.MAX_VALUE;
        private static final int TIMEOUT_CONNECT = 20;
        private static final int TIMEOUT_CHECK_CONNECTION = 5;
        private static final int TIMEOUT_DISCONNECT = 5;
        private static final int TIMEOUT_TERMINATE = 5;
        private static final int TIMEOUT_PRINT_TEXT = 5;
        private static final int TIMEOUT_WAIT_PENDING_OPERATIONS = 20;
        private static final int TIMEOUT_OPEN_SHIFT = 10;
        private static final int TIMEOUT_SET_CASHIER = 5;
        private static final int TIMEOUT_GET_DATA = 5;
        private static final int TIMEOUT_SET_DATA = 5;
        private static final int TIMEOUT_PRINT_Z_REPORT = 15;
        private static final int TIMEOUT_PRINT_NOT_SENT_DOCS_REPORT = 15;
        private static final int TIMEOUT_SET_HEADERS = 5;
        private static final int TIMEOUT_PRINT_ADJUSTING_TABLE = 10;
        private static final int TIMEOUT_PRINT_BARCODE = 10;
        private static final int TIMEOUT_FISCAL_COMMAND = 5;
        private static final int TIMEOUT_CONNECTION_ESTABLISHED = 5;

        public int getPrepareResourcesTimeout() {
            return TIMEOUT_PREPARE_RESOURCES;
        }

        public int getFreeResourcesTimeout() {
            return TIMEOUT_FREE_RESOURCES;
        }

        public int getConnectTimeout() {
            return TIMEOUT_CONNECT;
        }

        public int getCheckConnectionTimeout() {
            return TIMEOUT_CHECK_CONNECTION;
        }

        public int getDisconnectTimeout() {
            return TIMEOUT_DISCONNECT;
        }

        public int getTerminateTimeout() {
            return TIMEOUT_TERMINATE;
        }

        public int getPrintTextTimeout() {
            return TIMEOUT_PRINT_TEXT;
        }

        public int getWaitPendingOperationsTimeout() {
            return TIMEOUT_WAIT_PENDING_OPERATIONS;
        }

        public int getOpenShiftTimeout() {
            return TIMEOUT_OPEN_SHIFT;
        }

        public int getSetCashierTimeout() {
            return TIMEOUT_SET_CASHIER;
        }

        public int getGetDataTimeout() {
            return TIMEOUT_GET_DATA;
        }

        public int getSetDataTimeout() {
            return TIMEOUT_SET_DATA;
        }

        public int getPrintZReportTimeout() {
            return TIMEOUT_PRINT_Z_REPORT;
        }

        public int getPrintNotSentDocsReportTimeout() {
            return TIMEOUT_PRINT_NOT_SENT_DOCS_REPORT;
        }

        public int getSetHeadersTimeout() {
            return TIMEOUT_SET_HEADERS;
        }

        public int getPrintAdjustingTableTimeout() {
            return TIMEOUT_PRINT_ADJUSTING_TABLE;
        }

        public int getPrintBarcodeTimeout() {
            return TIMEOUT_PRINT_BARCODE;
        }

        public int getFiscalCommandTimeout() {
            return TIMEOUT_FISCAL_COMMAND;
        }

        public int getEndFiscalDocumentTimeout() {
            return TIMEOUT_FISCAL_COMMAND;
        }

        public int getConnectionEstablishedTimeout() {
            return TIMEOUT_CONNECTION_ESTABLISHED;
        }
    }

}
