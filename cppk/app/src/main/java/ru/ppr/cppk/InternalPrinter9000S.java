package ru.ppr.cppk;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.moebiusdrvr.DateTimeFE;
import com.moebiusdrvr.KKMDopInfo;
import com.moebiusdrvr.KKMInfoStateData;
import com.moebiusdrvr.KKMTotSendHost;
import com.moebiusdrvr.MoebiusFE;
import com.moebiusdrvr.PaymentSt;
import com.moebiusdrvr.ResultAsSingleString;
import com.moebiusdrvr.ResultAsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rs.fncore.Const;
import rs.fncore.data.KKMInfo;
import rs.fncore.data.OU;
import rs.fncore.data.Shift;
import rs.utils.app.MessageQueue;
import rs.fncore.FiscalStorage;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.utils.PrintFormat;
import ru.ppr.ikkm.Printer;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.DiscrepancyInTimeException;
import ru.ppr.ikkm.exception.MoebiusException;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ByteUtils;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.Decimals;
import android.device.zint.*;

/**
 * Реализация обертки для драйвера встроенного принтера "i9000S".
 *
 * @author Andrew Kopanev
 */
public class InternalPrinter9000S extends Printer implements MessageQueue.MessageHandler{

    private static final String TAG = Logger.makeLogTag(ru.ppr.cppk.InternalPrinter9000S.class);

    private static final String MODEL = "i9000S_internal";

    private static final byte[] CARRIAGE_RETURN_WITH_LINE_FEED = new byte[]{'\r', '\n'};

    /**
     * Ширина билетной ленты в символах для обычного шрифта при обычной печати
     */
    private final static int TEXT_NORMAL_TAPE_WIDTH = 28;
    /**
     * Ширина билетной ленты в символах для обычного шрифта при печати фискального чека
     */
    private final static int FISCAL_NORMAL_TAPE_WIDTH = 23;

    /**
     * Ошибка, связанная со временем (расхождение, превышена длительность смены и т.п.)
     */
    private static final int MOEBIUS_ANY_TIME_ERROR = 11;

    private String fontName = "arial";
    private Context context;
    private PaymentSt[] cardPay;
    private PaymentSt[] creditPay;
    private final SimpleDateFormat formatterForEklzTime;
    private final File logDir;
    ///////////////////////
    private String printerMacAddress;
    ///////////////////////

    private TextStyle currentTextStyle = null;
    PrintManager_new printer = new PrintManager_new();

    private KKMInfo _kkmInfo = new KKMInfo();
    OU casier;

    private class KKMInfoReader implements Globals.ProcessTask, Globals.ResultTask {
        private String fn_serial = "не доступно", kkm_serial = "не доступно", shift_state = "не доступно", kkm_number = "не доступно";

        @Override
        public void onResult(int result) {
            if(result == Const.Errors.NO_ERROR) {
                kkm_serial = _kkmInfo.getKKMSerial();
                shift_state = "не доступно";
                if(_kkmInfo.isFNPresent())
                    fn_serial = _kkmInfo.getFNNumber();
                else  {
                    fn_serial = "не доступно";
                }
                if(_kkmInfo.isFNActive() || _kkmInfo.isFNArchived()) {
                    kkm_number = _kkmInfo.getKKMNumber();
                    if(_kkmInfo.getShift().isOpen())
                        Logger.trace(TAG, "Смена № " + _kkmInfo.getShift().getNumber() + " открыта.");
                    else
                        Logger.trace(TAG, "Смена № " + _kkmInfo.getShift().getNumber() + "закрыта");
                } else
                    Logger.trace(TAG, "не фискализирован");
            } else {
                Logger.trace(TAG, "Ошибка чтения информации код = " + result);
            }
        }

        // Чтение информации о ККМ
        @Override
        public int execute(FiscalStorage storage, Globals.FNOperaionTask task, Object...args) {
            try {
                return storage.readKKMInfo(_kkmInfo);
            } catch(RemoteException re) {
                return Const.Errors.SYSTEM_ERROR;
            }
        }

    }

    public InternalPrinter9000S(@NonNull final Context context,
                               @NonNull File logDir,
                               @Nullable String printerMacAddress) throws Exception {
        this.logDir = logDir;
        formatterForEklzTime = new SimpleDateFormat("dd.MM.yyy HH:mm", Locale.getDefault());
        formatterForEklzTime.setTimeZone(TimeZone.getDefault());
        cardPay = new PaymentSt[16];
        creditPay = new PaymentSt[16];
        for (int i = 0; i < cardPay.length; i++) {
            cardPay[i] = new PaymentSt();
            creditPay[i] = new PaymentSt();
        }
        this.context = context;
        this.printerMacAddress = printerMacAddress;

        initializeWithDriverImpl();
/*        if (Build.VERSION_CODES.M >= Build.VERSION.SDK_INT) {
            System.loadLibrary("UzintEncode_5.1");
        } else {
            System.loadLibrary("UzintEncode");
        }*/

        Logger.trace(TAG, "I9000S Wrapper created");
    }

    @Override
    public boolean onMessage(Message msg) {
        if (msg.what == Globals.MSG_FISCAL_STORAGE_READY) {
            int result = ((Number) msg.obj).intValue();
            Logger.trace(TAG, "Инициализация завершена, код = " + result);
            if (result == Const.Errors.NO_ERROR) {
                updateKKMInfo();
            }
            return true;
        }
        return false;
    }

    protected void init_fn(){
        try {
            if (!_kkmInfo.isFNActive()) {
                Globals.get_fn_Instance().registerHandler(this);
                Logger.debug(TAG, "Инициализация ядра..");
                if (Globals.get_fn_Instance().initialize()) {
                    Logger.debug(TAG, "updateKKMInfo");
                    updateKKMInfo();
                }
                else {
                }
            }
        }
        catch(Exception e){
            Logger.trace(TAG, e);
        }

    }

    public void set_x_y(){
        printer.reset_currentYPoint();
    }
    @Override
    protected void initializeWithDriverImpl() throws Exception {
        //Создать устройство принтер
        if (printer.getStatus() != PrintManager_new.ERROR_NONE){
            printer.close();
        }
        int printerStatus = printer.getStatus();
        if (printerStatus != PrintManager_new.ERROR_NONE) {
            throw new Exception("Failed opening I9000S internal printer");
        }else {
            printer.initPrint(this.context);
//            printer.setupPage(384, -1);
            set_x_y();
//            printer.clearPage();
            setLogDir(logDir);
            Logger.trace(TAG, "I9000S Printer initialized");
        }
        init_fn();
    }

    private void updateKKMInfo() {
        KKMInfoReader reader = new KKMInfoReader();
        Globals.get_fn_Instance().newTask(Di.INSTANCE.getApp(), reader, reader).execute();
    }

    @Override
    protected void terminateImpl() throws Exception {
        Globals.get_fn_Instance().removeHandler(this);
        // Отключаемся от Фискального ядра
        Globals.get_fn_Instance().deinitialize();

        disconnectInternal(false);
        //Закрыть принтер
        printer.close();
        Logger.trace(TAG, "I9000S Printer terminated");
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
        return printer.getStatus() == printer.ERROR_NONE;
    }

    @Override
    protected void connectWithDriverImpl() throws Exception {
        Logger.trace(TAG, "connectWithDriverImpl start");
        Logger.trace(TAG, "connectWithDriverImpl end");
    }

    @Override
    public void disconnectWithDriverImpl() throws Exception {
        Logger.trace(TAG, "disconnectWithDriverImpl start");
        Logger.trace(TAG, "disconnectWithDriverImpl end");
    }

    /**
     * Выполняет CPCL команду
     *
     * @param command
     * @throws Exception
     */
    private void execCpcl(String command) throws Exception {
        String[] parts = command.split("\\r\\n");

        //До вызова cpcl команды обязательно проверить статус бумаги.
        //byte err = moebius.kkmCheckPaper();
        //checkError(err);

        //в середине cpcl команды не должно вызываться функции проверки статуса бумаги.
        // А вот после отправки всей cpcl команды, необходимо ее вызвать, поэтому последний блок отправим другой командой.
        for (int i = 0; i < parts.length; i++) {
            byte[] data = ByteUtils.concatArrays(parts[i].getBytes(), CARRIAGE_RETURN_WITH_LINE_FEED);
            if (i == parts.length - 1) {
                //err = moebius.sendBlockWithCheckPaper(data);
            } else {
               //err = moebius.sendBlock(data);
            }
            //checkError(err);
        }
    }

    /**
     * Устанавливает стиль размер печатаемого текста.
     *
     * @param textStyle Стиль текста
     * @throws Exception В случае ошибки
     */
    private void setTextStyle(@NonNull TextStyle textStyle) throws Exception {
        Logger.trace(TAG, "setTextStyle ENTER");
        if (textStyle == TextStyle.FISCAL_NORMAL) execCpcl("! U1 SETLP DEJAVU16.CPF 0 16\r\n");
        else execCpcl("! U1 DEJAVU14.CPF 0 14\r\n"); //для обычной печати
        currentTextStyle = textStyle;
    }

    @Override
    protected void printTextInFiscalModeImpl(String text, TextStyle textStyle) throws Exception {
        Logger.trace(TAG, "printTextInFiscalModeImpl ENTER");
        /*
         https://aj.srvdev.ru/browse/CPPKPP-25076
         https://terlis.intraservice.ru/Task/View/29911
         moebius.prn_tpm_write(8, new byte[0]) печатает каракули вместо пустой строки"
         */
//        if (TextUtils.isEmpty(text)) {
//            text = " ";
//        }
//        byte[] arrBytes = ByteUtils.concatArrays(text.getBytes("Cp1251"), CARRIAGE_RETURN_WITH_LINE_FEED);
       // byte err = moebius.prn_tpm_write(8, arrBytes);
       // checkError(err);
        printTextInNormalModeImpl(text, textStyle);
    }

    @Override
    protected void printTextInNormalModeImpl(String text, TextStyle textStyle) throws Exception {
        if (textStyle != currentTextStyle)
            setTextStyle(textStyle);

        Logger.trace(TAG, "printTextInNormalModeImpl = " + text);
//        y += printer.drawText(text, x, y, fontName,
//                24, false, false, 0);
        printer.addText(text, PrintFormat.FONT_NORMAL, PrintFormat.ALIGN_LEFT);
    }

    @Override
    protected void endFiscalDocumentImpl(DocType docType) throws Exception {
        Logger.trace(TAG, "endFiscalDocumentImpl ENTER");
        closePageImpl(0);
    }

    public int closePageImpl(int rotate){
        Logger.trace(TAG, "closePageImpl START");
//        int ret = printer.printPage(rotate);
        int ret = printer.startPrint();
        printer.paperFeed(16);  //paper feed
//        int ret = 0;
//        printer.clearPage();
        set_x_y();
        Logger.trace(TAG, "closePageImpl = " + ret);
        return ret;
    }

    @Override
    protected void waitPendingOperationsImpl() throws Exception {
     //   byte err = moebius.kkmCheckPaper();
     //   checkError(err);
    }

    @Override
    protected void openShiftImpl(int operatorCode, String operatorName) throws Exception {
        casier = new OU(operatorName); // Кассир по умолчанию
        init_fn();
        if (!_kkmInfo.isFNActive()){
            Logger.trace(TAG, "В данном режиме ФН операция невозможна");
            return;
        }
        Logger.trace(TAG, "I9000S.kkmOpenShift() start");
        if (_kkmInfo.getShift().isOpen()){
            Logger.trace(TAG, "Смена и так уже открыта");
            return;
        }
            Globals.get_fn_Instance().newTask(Di.INSTANCE.getApp(), (storage, task, args) -> {
                task.showProgress("Операция выполняется...");
                try {
                    int result;
                    result = storage.toggleShift(casier, new Shift(), null);
                    if(result == Const.Errors.NO_ERROR) // Обновляем информацию о ККМ
                        storage.readKKMInfo(_kkmInfo);
                    return result;
                } catch(RemoteException re) {
                    return Const.Errors.SYSTEM_ERROR;
                }
            }, result -> {
                if(result == Const.Errors.NO_ERROR) {
                    if(_kkmInfo.getShift().isOpen()) {
                        Logger.trace(TAG, String.format("Смена %d успешно открыта", _kkmInfo.getShift().getNumber()));
                        Toast.makeText(Di.INSTANCE.getApp(), String.format("Смена %d успешно открыта", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Logger.trace(TAG, String.format("Смена %d не открыта. Ошибка", _kkmInfo.getShift().getNumber()));
                        Toast.makeText(Di.INSTANCE.getApp(), String.format("Смена %d не открыта. Ошибка", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
                    }
                    new KKMInfoReader().onResult(result); // Отображаем новую информацию о смене
                } else {
                    Logger.trace(TAG, String.format("Операция выполнена с ошибкой %02X", result));
                    Toast.makeText(Di.INSTANCE.getApp(), String.format("Операция выполнена с ошибкой %02X", result), Toast.LENGTH_LONG).show();
                }
            }).execute();
    }

    @Override
    protected void setCashierImpl(int operatorCode, String operatorName) throws Exception {
        //byte err = moebius.kkmCasherChg((byte) operatorCode, operatorName);
        //checkError(err);
    }

    @Override
    protected boolean isShiftOpenedImpl() throws Exception {
        updateKKMInfo();
        Logger.trace(TAG, "isShiftOpenedImpl START. isOpen = " + _kkmInfo.getShift().isOpen());
        return (_kkmInfo.getShift().isOpen());
    }

    @Override
    protected Date getDateImpl() throws Exception {
        Logger.trace(TAG, "getDateImpl START");
        return _kkmInfo.getLastDocument().getDate();
    }

    @Override
    protected int getLastSPNDImpl() throws Exception {
        return _kkmInfo.getShift().getLastDocumentNumber();    }

    @Override
    protected Date getLastCheckTimeImpl() throws Exception {
        Logger.trace(TAG, "getLastCheckTimeImpl START");
        long lwhen = _kkmInfo.getShift().getWhenOpen();
        Calendar lcal = Calendar.getInstance();
        lcal.setTimeInMillis(lwhen);
        return lcal.getTime();
//        return _kkmInfo.getLastDocument().getDate();
    }

    @Override
    protected int getShiftNumImpl() throws Exception {
        //полное количество закрытых смен
        Logger.trace(TAG, "getShiftNumImpl = " + _kkmInfo.getShift().getNumber());
        updateKKMInfo();
        return _kkmInfo.getShift().getNumber();
    }

    @Override
    protected void printZReportImpl() throws Exception {
        Logger.debug(TAG, "I9000S.kkmCloseShift() start");
        init_fn();
        if (!_kkmInfo.isFNActive()){
            Logger.debug(TAG, "В данном режиме ФН операция невозможна");
            return;
        }
        if (!_kkmInfo.getShift().isOpen()){
            Logger.debug(TAG, "Смена и так уже закрыта");
            return;
        }
        Globals.get_fn_Instance().newTask(Di.INSTANCE.getApp(), (storage, task, args) -> {
            Logger.debug(TAG, "newTask start");
            task.showProgress("Операция выполняется...");
            try {
                Logger.debug(TAG, String.format("Операция по закрытию смены выполняется..."));
                int result;
                result = storage.toggleShift(casier, new Shift(), null);
                if(result == Const.Errors.NO_ERROR) // Обновляем информацию о ККМ
                    storage.readKKMInfo(_kkmInfo);
                return result;
            } catch(RemoteException re) {
                return Const.Errors.SYSTEM_ERROR;
            }
        }, result -> {
            Logger.debug(TAG, "Result start");
            if(result == Const.Errors.NO_ERROR) {
                if(!_kkmInfo.getShift().isOpen()) {
                    Logger.debug(TAG, String.format("Смена %d успешно закрыта", _kkmInfo.getShift().getNumber()));
                    Toast.makeText(Di.INSTANCE.getApp(), String.format("Смена %d успешно закрыта", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
                }
                else {
                    Logger.debug(TAG, String.format("Смена %d не закрыта. Ошибка", _kkmInfo.getShift().getNumber()));
                    Toast.makeText(Di.INSTANCE.getApp(), String.format("Смена %d не закрыта. Ошибка", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
                }
                new KKMInfoReader().onResult(result); // Отображаем новую информацию о смене
            } else {
                Logger.debug(TAG, String.format("Операция выполнена с ошибкой %02X", result));
                Toast.makeText(Di.INSTANCE.getApp(), String.format("Операция выполнена с ошибкой %02X", result), Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    @Override
    protected void setHeaderLinesImpl(List<String> headerLines) throws Exception {
        String fullHeader = "";
        for (int i = 0; i < headerLines.size(); i++) {
            fullHeader += headerLines.get(i);
            if (i < headerLines.size() - 1)
                fullHeader += "\r\n";
        }
       // moebius.kkmChangeHeader(fullHeader);
    }

    @Override
    protected void setVatValueImpl(int vatID, int vatValue) throws Exception {
        //moebius.setVatValue(vatID, vatValue);
    }

    @Override
    protected int getVatValueImpl(int vatID) throws Exception {
        return 0;
    }

    public static String byte2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String temp = Integer.toHexString(((int) data[i]) & 0xFF);
            for (int t = temp.length(); t < 2; t++) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    @Override
    protected void printBarcodeImpl(byte[] data) throws Exception {

        Logger.trace(TAG, "printBarcodeImpl() START");
        if ((data == null) || (data.length <= 0))
            throw new NullPointerException("barcodeData is null");

        int status = printer.getStatus();
        /*
            за высоту штрих-кода отвечает 3й параметр в первой строке
            если нужно увеличить отступт штрихкода до конца чека, то увеличиваем его
         */

//        ByteArrayOutputStream os = new ByteArrayOutputStream();

/*        String barcodeHeight;

        if (data.length == 82) {
            // разовый
            barcodeHeight = "119";
        } else if (data.length == 91) {
            // доплата
            barcodeHeight = "125";
        } else {
            // стандарт
            barcodeHeight = "130";
        }*/

/*String lFile = Environment.getExternalStorageDirectory() + "/pos/log.txt";
        FileOutputStream fw = new FileOutputStream(lFile);
try {
    fw.write(data);
}
catch(Exception e) {
    fw.close();
}*/

        int barcodeHeight;
        if (data.length == 82) {
            // разовый
            barcodeHeight = 119;
        } else if (data.length == 91) {
            // доплата
            barcodeHeight = 125;
        } else {
            // стандарт
            barcodeHeight = 130;
        }
        int width = 2;
        int rotate = 0;
        int barcodetype = 55; // BARCODE_PDF417
        int lx = 5;

/*        width = 4;
        barcodeHeight = 350;
        barcodetype = 58; // QR
        lx = 35;*/

        if(status == printer.ERROR_NONE) {
            printer.setupPage(384, -1);
            printer.setGrayLevel(4);
            String ldata = byte2hex(data);
            Result result = EncodeHander.nativeEncode(barcodetype, 120, 10, 0, 1, 0, 0, 0, 0, 0,0, ldata.getBytes());
            if(result != null && result.getRetCode() == 0) {
                byte[] bitmapByteArray = new byte[result.getRawBytesSize()];
                System.arraycopy(result.getRawBytes(), 0,bitmapByteArray,0, result.getRawBytesSize());
                int res = printer.drawBitmapEx(bitmapByteArray, 0, 15,
                        result.getSymbolWidth(), result.getSymbolHeight());
            }
        }
        else
            checkError(status);

//        Logger.trace(TAG, "printBarcodeImpl(data=" + CommonUtils.bytesToHexWithoutSpaces(data) + ") command=" + CommonUtils.bytesToHexWithoutSpaces(command));
        Logger.trace(TAG, "bytesToHexWithoutSpaces(data=" + CommonUtils.bytesToHexWithoutSpaces(data));
        Logger.trace(TAG, "getStringFromBytes(data=" + CommonUtils.getStringFromBytes(data));
        Logger.trace(TAG, "getDecStringFromByteArray(data=" + CommonUtils.getDecStringFromByteArray(data));

        if (data.length > 256)
            throw new IllegalArgumentException("printBarcode ERROR:  Length command for print barcode should be less 256 symbols");

        //До вызова cpcl команды обязательно проверить статус бумаги.
        int err = printer.getStatus();
        if (err == printer.ERROR_PAPERENDED)
            checkError(err);

        //как оказалось печать cpcl команды сбрасывает шрифт обратно в маленький (нормальный) - поэтому сбросим флаг.
        //CPPKPP-30814
        currentTextStyle = TextStyle.TEXT_NORMAL;
//        String ldata = new String(data, 0, data.length);

/*        StringBuilder lString = new StringBuilder("");
        for (int i = 0; i < data.length; i ++){
            int v = data[i] & 0xff;
            String sv = Integer.toHexString(v);
            if (sv.length() < 2) lString.append(0);
            lString.append(sv);
        }
        String ldata = lString.toString();

        Logger.trace(TAG, "String = " + ldata);
        err = printer.drawBarcode(ldata, lx, printer.getCurrentYPoint(), barcodetype, width, barcodeHeight, rotate);*/
        //err = moebius.sendBlockWithCheckPaper(command);
        checkError(err);
        closePageImpl(0);
    }

    @Override
    protected void printAdjustingTableImpl() throws Exception {
        String command =

                "! 276 200 200 100 1\r\n" +

                        "BOX 0 0 285 100 1\r\n" +

                        "BOX 4 4 281 96 1\r\n" +

                        "BOX 8 8 277 92 1\r\n" +

                        "BOX 12 12 273 88 1\r\n" +

                        "BOX 16 16 269 84 1\r\n" +

                        "BOX 20 20 265 80 1\r\n" +

                        "BOX 24 24 261 76 1\r\n" +

                        "BOX 28 28 257 72 1\r\n" +

                        "BOX 32 32 253 68 1\r\n" +

                        "BOX 36 36 249 64 1\r\n" +

                        "BOX 40 40 245 60 1\r\n" +

                        "BOX 44 44 241 56 1\r\n" +

                        "BOX 48 48 237 52 1\r\n" +

                        "FORM\r\n" +

                        "PRINT\r\n";

        String[] parts = command.split("\\r\\n");

        //До вызова cpcl команды обязательно проверить статус бумаги.
        //byte err = moebius.kkmCheckPaper();
        //checkError(err);

        //в середине cpcl команды не должно вызываться функции проверки статуса бумаги.
        // А вот после отправки всей cpcl команды, необходимо ее вызвать, поэтому последний блок отправим другой командой.
        for (int i = 0; i < parts.length; i++) {
            byte[] data = ByteUtils.concatArrays(parts[i].getBytes(), CARRIAGE_RETURN_WITH_LINE_FEED);
            if (i == parts.length - 1) {
              //  err = moebius.sendBlockWithCheckPaper(data);
            } else {
               // err = moebius.sendBlock(data);
            }
          //  checkError(err);
        }
    }

    @Override
    protected void startFiscalDocumentImpl(DocType docType) throws Exception {
        initializeWithDriverImpl();
        /**
         * 1 – код оператора для ЭКЛЗ, максимальное значение в ЭКЛЗ этого параметра
         99, поэтому если вы передадите 255, в чеке все равно напечатается 99, хотя код оператора в
         Мебиусе м.б. от 1 до 255, код оператора в Мебиусе задается при открытии смены или при вызове
         команды смены оператора KkmCasherChg.
         Старший бит параметра oprCodeEklz отвечает за тип чека: если в старшем бите установлен 0, то
         оформляется чек продажи, если в стршем бите установлена 1, то оформляется чек возврата
         */
        byte oprCodeEklz = (byte) 1;
        if (docType == DocType.SALE) {
            oprCodeEklz &= 0x7F;
        } else if (docType == DocType.RETURN) {
            oprCodeEklz |= 0x80;
        }

        //для большого шрифта
       // byte err = moebius.mbs_headEx(oprCodeEklz, (byte) 1, "0");

        //для мальенькго шрифта
        //byte err = moebius.mbs_head(oprCodeEklz, (byte) 1, "0");


       //checkError(err);
    }

    @Override
    protected void addItemImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {
       /* byte err = moebius.mbs_RecItem(description,
                description,
                "1",
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                1,
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                getVatIndexForRate(vatRate),
                false,
                "");
        checkError(err);*/
    }

    @Override
    protected void addItemRefundImpl(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws Exception {
        /*byte err = moebius.mbs_RecItem(description,
                description,
                "1",
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                1,
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                getVatIndexForRate(vatRate),
                false,
                "");
        checkError(err);*/
    }

    @Override
    protected void addDiscountImpl(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws Exception {
        /*byte err = moebius.mbs_adjust_sum(false,
                discount.multiply(new BigDecimal(-100)).intValue(), // переводим рубли в копейки
                newAmount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                false,
                false,
                getVatIndexForRate(vatRate),
                discount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                "");
        checkError(err);*/
    }

    @Override
    protected void printTotalImpl(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws Exception {

/*
        int totalInt = total.multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки
        int chgInt = payment.subtract(total).multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки
        int paymentInt = payment.multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки

        int cashInt = paymentType == PaymentType.CASH ? paymentInt : 0;
        cardPay[0].Sum = paymentType == PaymentType.CARD ? paymentInt : 0;

        //byte err = moebius.mbs_tot(totalInt, chgInt, cashInt, cardPay, creditPay, "xxx", 1, 1);*/
       // checkError(err);
    }

    @Override
    public long getOdometerValueImpl() throws Exception {
//        Thread.sleep(10000);
//        ResultAsSingleString resultAsSingleString = new ResultAsSingleString();
      //  byte err = moebius.kkmGetOdometerValue(resultAsSingleString);
       // checkError(err);
//        Long result = Long.valueOf(resultAsSingleString.getString().replaceAll("[^\\d]", ""));
        Long result = Long.valueOf(0);
//        Logger.info(TAG, "getOdometerValueImpl() StringRes = " + resultAsSingleString.getString());
        return result;
    }

    @Override
    protected String getINNImpl() throws Exception {
        updateKKMInfo();
        return _kkmInfo.ofd().getINN();
    }

    @Override
    protected String getRegNumberImpl() throws Exception {
        //String value = kkmGetKKMInfoReg().getRNKKM();
        //Logger.info(ru.ppr.moebius.PrinterZebraMoebius.class.getSimpleName(), ": ---wrong-maybe--- getRegNumberImpl() = " + value);
        //return value;
        return "dd";
    }

    @Override
    protected String getEKLZNumberImpl() throws Exception {

        //ResultAsSingleString out = new ResultAsSingleString();
        //byte err = moebius.kkmGetEKLZNumber(out);
        //checkError(err);
        //Logger.info(ru.ppr.moebius.PrinterZebraMoebius.class.getSimpleName(), ": ---wrong-maybe--- getEKLZNumberImpl() = " + out.getString());
        //return out.getString();
        return "dd";
    }

    @Override
    protected String getFNSerialImpl() throws Exception {
        String lsn = "не установлен";
        if(_kkmInfo.isFNPresent())
            lsn = _kkmInfo.getFNNumber();
        return lsn;
    }

    @Override
    protected String getModelImpl() throws Exception {
        return MODEL;
    }

    @Override
    protected BigDecimal getCashInFRImpl() throws Exception {
        double sumInCent = kkmGetKKMInfoEx().DSale;//сумма в копейках
        return new BigDecimal(sumInCent).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    protected long getAvailableSpaceForShiftsImpl() throws Exception {
        return kkmDopInfo().CloseShiftTail;
    }

    @NonNull
    private ResultAsString kkmGetKKMInfoReg() throws Exception {
        ResultAsString out = new ResultAsString();
        //byte err = moebius.kkmGetKKMInfoReg(out);
        //checkError(err);
        return out;
    }

    /**
     * @param oprCode служебный параметр, если равен 0x80, то возвращается промежуток времени после
     *                первой фискальной операции в смене (длина смены), если 0,
     *                то возвращается текущее время в ФР.
     * @return
     * @throws MoebiusException
     */
    @NonNull
    protected KKMInfoStateData kkmGetKKMInfoState(byte oprCode) throws Exception {
        KKMInfoStateData out = new KKMInfoStateData();
        //byte err = moebius.kkmGetKKMInfoState(oprCode, out);
        //checkError(err);
        return out;
    }

    /**
     * Получает от принтера информацию по фискальным данным
     *
     * @return структура с информацией о фискальных данных
     * @throws MoebiusException
     */
    @NonNull
    private KKMTotSendHost kkmGetKKMInfoEx() throws Exception {
        KKMTotSendHost out = new KKMTotSendHost();
        //byte err = moebius.kkmGetKKMInfoEx(out);
        //checkError(err);
        return out;
    }

    private KKMDopInfo kkmDopInfo() throws Exception {
        KKMDopInfo out = new KKMDopInfo();
       // byte err = moebius.kkmGetKKMDopInfo(out);
        //checkError(err);
        return out;
    }

    @Override
    protected List<ClosedShiftInfo> getShiftsInfoImpl(int startNum, int endNum) throws Exception {
        List<ClosedShiftInfo> res = new ArrayList<>();
        if (isFiscalMode()) {

            ResultAsSingleString out = new ResultAsSingleString();
            //byte err = moebius.kkmEKLZReportDFP((byte) 1, (byte) 0, (byte) 0, new DateTimeFE(), new DateTimeFE(), startNum, endNum, (byte) 0, out);
            //checkError(err);

            String shiftsInfoStr = out.getString();
            Logger.trace(TAG, shiftsInfoStr);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            String regEx = "ЗАКР\\.СМЕНЫ\\s*(\\d+)\\s*([^ ]+ [^ ]+)[\\s\\S]*?\\*([^ ]+)[\\s\\S]*?\\*([^ ]+)[\\s\\S]*?\\*([^ ]+)[\\s\\S]*?\\*([^ ]+)";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(shiftsInfoStr);
            while (matcher.find()) {
                int shiftNum = Integer.valueOf(matcher.group(1));
                Date closeTime = simpleDateFormat.parse(matcher.group(2));
                BigDecimal totalSaleSum = new BigDecimal(matcher.group(3));
                BigDecimal totalBuySum = new BigDecimal(matcher.group(4));
                BigDecimal totalReturnSaleSum = new BigDecimal(matcher.group(5));
                BigDecimal totalReturnBuySum = new BigDecimal(matcher.group(6));
                res.add(new ClosedShiftInfo(shiftNum, closeTime, totalSaleSum, totalBuySum, totalReturnSaleSum, totalReturnBuySum));
            }
        } else {
            throw new PrinterException("Принтер не фискализирован!");
        }
        return res;
    }

    @Override
    protected int getWidthForTextStyleImpl(TextStyle textStyle) {
        switch (textStyle) {
            case TEXT_NORMAL:
                return TEXT_NORMAL_TAPE_WIDTH;
            case FISCAL_NORMAL:
                return FISCAL_NORMAL_TAPE_WIDTH;
            default:
                throw new IllegalArgumentException("Unsupported textStyle = " + textStyle);
        }
    }

    @Override
    protected boolean isFederalLaw54SupportedImpl() {
        return false;
    }

    @Override
    protected void setCustomerPhoneNumberImpl(String phoneNumber) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void setCustomerEmailImpl(String email) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void printNotSentDocsReportImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void printCorrectionReceiptImpl(DocType docType, BigDecimal total) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void printDuplicateReceiptImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected OfdSettings getOfdSettingsImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void setOfdSettingsImpl(OfdSettings ofdSettings) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void scrollPaperInNormalModeImpl(int linesCount) throws Exception {
        for (int i = 0; i < linesCount; i++)
            printTextInNormalMode(" ");
    }

    @Override
    protected OfdDocsState getOfdDocsStateImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    @Override
    protected void startSendingDocsToOfdImpl() throws Exception {
        throw new UnsupportedOperationException("Operation is not supported");
    }

    private void checkError(int err) throws Exception {
        if (err != 0) {
            if (err == printer.ERROR_PAPERENDED)
                throw new Exception("I9000S internal printer: Out Of Paper");
            if (err == printer.ERROR_OVERHEAT)
                throw new Exception("I9000S internal printer: Printer is over heat");
            if (err == printer.ERROR_LOWVOL)
                throw new Exception("I9000S internal printer: Printer is under voltage");
            if (err == printer.ERROR_BUSY)
                throw new Exception("I9000S internal printer: Printer is busy");
            if (err == printer.ERROR_HARDERR)
                throw new Exception("I9000S internal printer: Driver error");
        }
    }

    private void setLogDir(File path) {
        String fullPathPrefix = Environment.getExternalStorageDirectory().getPath() + "/";
       // moebius.setLogSubDir(path.getName());
        //такой костыль используется потому что зебра не принимает полные пути!
       // moebius.setLogRootDir((path.getParentFile().getAbsolutePath().replace(fullPathPrefix, "")));
    }

    private boolean isFiscalMode() throws Exception {
        KKMInfoStateData kkmInfoStateData = kkmGetKKMInfoState((byte) 0x00);
        return (((kkmInfoStateData.State >> 1) & 0x01) == 1);
    }

    /**
     * Получает индекс налоговой ставки по значению.
     *
     * @param vatRate Значение налоговой ставки
     * @return Индекс налоговой ставки
     */
    private int getVatIndexForRate(@Nullable BigDecimal vatRate) {
        if (vatRate != null && BigDecimal.ZERO.compareTo(vatRate) == 0) {
            return 0;
        } else {
            throw new IllegalArgumentException("Unsupported vat rate = " + vatRate);
        }
    }

    public interface BluetoothManager {
        boolean enable();

        boolean disable();

        boolean isEnabled();
    }

}
