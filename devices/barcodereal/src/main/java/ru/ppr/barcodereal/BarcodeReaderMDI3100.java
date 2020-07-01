package ru.ppr.barcodereal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.InterfacesCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.ManufacturersCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.ModelsCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.PeripheralTypesCone;
import fr.coppernic.sdk.barcode.BarcodeInterface;
import fr.coppernic.sdk.barcode.opticon.mdi3100.Reader;
import fr.coppernic.sdk.utils.core.CpcResult.RESULT;
import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Реализация считывателя ШК, работающая на встроенном ридере Coppercic C-One.
 */
public class BarcodeReaderMDI3100 implements IBarcodeReader {

    private static final String TAG = Logger.makeLogTag(BarcodeReaderMDI3100.class);
    /**
     * Задержка автоматического выключения ридера после обращения к нему, ms
     */
    public static final long BARCODE_REAL_AUTO_POWER_OFF_DELAY = 10000;
    /**
     * Флаг автоматического выключения ридера после обращения к нему
     */
    public static final boolean BARCODE_REAL_AUTO_POWER_OFF_ENABLED = false;
    /**
     * Скорость передачи по умолчанию
     */
    public static final int BARCODE_REAL_BAUD_RATE_DEFAULT = 115200;

    /**
     * Время проходящее с момента подачи питиния на ридер, до его реального включения, ms
     */
    public static final long POWER_ON_DELAY = 600;

    /**
     * Timeout в секундах на одну попытку чтения штрих-кода
     */
    public static final int READING_TIMEOUT = 1;

    /**
     * Максимальное количество попыток проверок включенности ридера после команды powerOn-> open
     */
    public static final int MAX_CHECK_OPEN_COUNT = 10;

    /**
     * Задержка между попытками получить версию идера для контроля реальной включенности ридера
     */
    public static final int CHECK_OPEN_ITEM_DELAY = 100;

    private static final String BARCODE_READER_PORT = "/dev/ttyHS1";
    private static final boolean IS_SYNC = false;

    private final Reader readerInterface;

    private PowerMgmt powerMng;
    /**
     * Таймер отложенного выключения питания ридера
     */
    private Timer timer = new Timer();
    /**
     * Флаг наличия питания на ножках
     */
    private boolean isPowerOn = false;
    /**
     * Флаг активности (включенности) ридера
     */
    private boolean isOpened = false;
    /**
     * данные, считанные с ШК
     */
    private byte[] data;

    private Config config;

    private CountDownLatch countDownLatch;

    private final ExecutorService executorService;

    public BarcodeReaderMDI3100(Context context, Config config, ExecutorService executorService) {
        Logger.trace(TAG, "BarcodeReaderMDI3100() START");
        long timer = getTimer();
        this.config = config;
        this.executorService = executorService;
        powerMng = new PowerMgmt(context);
        BarcodeHandler barcodeHandler = new BarcodeHandler(this);
        readerInterface = new Reader(context, barcodeHandler);
        Logger.trace(TAG, "BarcodeReaderMDI3100() FINISH" + getTimeString(timer));
    }

    private void powerOn(boolean on) {
        Logger.trace(TAG, "powerOn(" + on + ") START isPowerOn=" + isPowerOn);
        long timer = getTimer();
        if (isPowerOn != on) {
            synchronized (this) {
                powerMng.setPower(PeripheralTypesCone.BarcodeReader, ManufacturersCone.Opticon, ModelsCone.Mdi3100, InterfacesCone.ScannerPort, on);
                if (on)
                    try {
                        Thread.sleep(POWER_ON_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else {
                    isOpened = false;
//                    handler.post(() -> Globals.getInstance().getToaster().showToast("BARCODE Power OFF"));

                }
                isPowerOn = on;
            }
        }
        Logger.trace(TAG, "powerOn(" + on + ") FINISH isPowerOn=" + isPowerOn + getTimeString(timer));
    }

    public static String byteArrayToHex(byte[] a) {
        if(a == null)
            return "null";

        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("\\x%02x", b));
        return sb.toString();
    }

    @Override
    @Nullable
    public byte[] scan() {
        long timer = getTimer();
        Logger.trace(TAG, "scan() START");
        byte[] scanData = null;
        if (!open()) {
            Logger.error(TAG, "scan() - Port not open");
        } else {
            scanData = readerScan();
            //попросим ридер автоматически выключится четез некорое время
            softClose();
        }
        Logger.trace(TAG, "scan() FINISH return " + CommonUtils.bytesToHexWithoutSpaces(scanData) + getTimeString(timer));
        Logger.trace(TAG, "scan(===) FINISH return " + byteArrayToHex(scanData));

        // Удаление Добавленных данных при чтении с турникета
        if (scanData != null) {
            if (scanData != null && scanData.length > 100) {
                Logger.trace(TAG, "test for QR ");

                byte[] cleared_scan = Arrays.copyOfRange(scanData, 48, scanData.length);

                System.arraycopy(scanData, 48, cleared_scan, 0, scanData.length - 48);
                System.out.println(Arrays.toString(cleared_scan));
                Logger.trace(TAG, "scan(3) FINISH return QR code   " + CommonUtils.bytesToHexWithoutSpaces(cleared_scan) + getTimeString(timer));

                try {
                    String nn = new String(Arrays.copyOfRange(scanData, 0, 48), "US-ASCII");
                    if (nn.length() > 44 && nn.charAt(10) == '0' && nn.charAt(10) == '0' && nn.charAt(21) == '0' && nn.charAt(32) == '0' && nn.charAt(43) == '0') {
                        MobileBarcodeReader mobileBarcodeReader = MobileBarcodeReader.getInstance();
                        Logger.trace(TAG, "test for QR zeros: ok");
                        mobileBarcodeReader.setLastMobileCode();
                        mobileBarcodeReader.setLastCode(nn);
/*
                        if (BuildConfig.VERSION_NAME.matches(".*[.]0")) {
                            byte[] b = CommonUtils.hexStringToByteArray("01180000680B9A5BEDA11C000000480300C00444270AE3CB872D8BB5E40B3FB722C8A9CCDD3B081EBCD8CA27A1A8AAB45162B075468ABB92516510DBE5E7BB94DCD89C9327EEA6A4C0941327077FC73BDCB5");
                            return b;
                        }*/
                        return cleared_scan;
                    } else
                        Logger.trace(TAG, "test for QR zeros: fail");

                } catch (UnsupportedEncodingException e) {
                    Logger.debug(TAG, "unsupported exeption " + e.getLocalizedMessage());

                }
            } else {
                MobileBarcodeReader mobileBarcodeReader = MobileBarcodeReader.getInstance();
                mobileBarcodeReader.getIfLastCodeMobile();
            }
        }
        return scanData;
    }

    @Override
    public boolean open() {
        long timer = getTimer();
        Logger.trace(TAG, "open() START");
        boolean out = false;
        cancelPowerOffTimer();
        if (readerInterface != null) {
            powerOn(true);
            out = openReader();
            //если так и не взлетело - выключим ридер
            if (!out) {
                hardClose();
            }
        }
        Logger.trace(TAG, "open() FINISH " + getResString(out) + getTimeString(timer));
        return out;
    }

    private synchronized byte[] readerScan() {
        countDownLatch = new CountDownLatch(1);
        setData(null);
        RESULT res = readerInterface.scan(IS_SYNC);
        Logger.trace(TAG, "startScan(1) - result=\"" + res.toString() + "\"");
        if (res == RESULT.OK) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Logger.error(TAG, e);
                Thread.currentThread().interrupt();
            }
        } else {
            setData(null);
        }
        Logger.trace(TAG, "startScan(2) - result=\"" + res.toString() + "\"");
        countDownLatch = null;
        return getData();
    }

    private synchronized boolean openReader() {
        Logger.trace(TAG, "openReader() START isOpened=" + isOpened);
        long timer = getTimer();
        int iteration = 1;
        while (!isOpened && MAX_CHECK_OPEN_COUNT >= iteration) {
            Logger.trace(TAG, "openReader() START iteration=" + iteration);
            long startIterationTime = System.currentTimeMillis();
            RESULT res = readerInterface.open(BARCODE_READER_PORT, config.getBaudRate());
            if (res == RESULT.OK) {
                isOpened = getFirmwareVersion(new StringBuilder());
                if (getTimeoutValue() != READING_TIMEOUT) {
                    isOpened = setTimeoutValue(READING_TIMEOUT);
                    if (!isOpened) {
                        closeReader();
                    }
                }
            }

            if (!isOpened && (System.currentTimeMillis() - startIterationTime) < CHECK_OPEN_ITEM_DELAY) {
                try {
                    Thread.sleep(CHECK_OPEN_ITEM_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            iteration++;
        }
        Logger.trace(TAG, "openReader() FINISH res=" + isOpened + getTimeString(timer));
        return isOpened;
    }

    private int getTimeoutValue() {
        long timer = getTimer();
        int timeout = readerInterface.getScanTimeoutParam();
        Logger.trace(TAG, "getTimeoutValue() FINISH return " + timeout + getTimeString(timer));
        return timeout;
    }

    private boolean setTimeoutValue(int timeout) {
        long timer = getTimer();
        RESULT res = readerInterface.setScanTimeoutValue(timeout);
        Logger.trace(TAG, "getTimeoutValue(" + timeout + ") FINISH return " + res + getTimeString(timer));
        return res == RESULT.OK;
    }

    @Override
    public void close() {
        Logger.trace(TAG, "close() START");
        long timer = getTimer();
        hardClose();
        Logger.trace(TAG, "close() FINISH" + getTimeString(timer));
    }

    private void closeReader() {
        Logger.trace(TAG, "closeReader START");
        long timer = getTimer();
        synchronized (this) {
            readerInterface.close();
            isOpened = false;
        }
        Logger.trace(TAG, "closeReader FINISH" + getTimeString(timer));
    }


    /**
     * Обработчик считывания ШК
     *
     * @author A.Ushakov
     */
    private static class BarcodeHandler extends Handler {
        private final BarcodeReaderMDI3100 reader;

        BarcodeHandler(BarcodeReaderMDI3100 parentReference) {
            super(Looper.getMainLooper());
            reader = parentReference;
        }

        @Override
        public void handleMessage(Message msg) {
            String fTitle = "BarcodeHandler.handleMessage(msg=\"" + msg.toString() + "\") ";
            Logger.trace(TAG, fTitle + "START");
            long timer = getTimer();

            Reader readerInterface = reader.readerInterface;

            byte[] data = null;
            if (msg.arg1 == BarcodeInterface.State.SCAN.ordinal()) {
                switch (msg.what) {
                    // в новой версии СДК при удачном чтении стал приходить такой код
                    case BarcodeInterface.MSG_DATA_READY:
                        byte[] s = new byte[6];
                        readerInterface.getSymbology(s);
                        data = new byte[readerInterface.getDataLength()];
                        readerInterface.getData(data);
                        break;
                    case BarcodeInterface.MSG_DATA_TIMEOUT:
                        Logger.trace(TAG, fTitle + "MSG_DATA_TIMEOUT");
                        break;
                    case BarcodeInterface.MSG_DATA_ERROR:
                        Logger.trace(TAG, fTitle + "MSG_DATA_ERROR");
                        break;
                    default:
                        Logger.trace(TAG, fTitle + "default");
                        break;
                }

                CountDownLatch countDownLatch = reader.countDownLatch;
                if (countDownLatch != null) {
                    reader.setData(data);
                    countDownLatch.countDown();
                } else {
                    Logger.warning(TAG, fTitle + " WARNING countDownLatch == null, scan result skipped");
                    return;
                }

            }
            //Бывают случаи, когда прилетает такой msg="{ what=5 when=-2ms arg1=7 arg2=-1 }"
            //Здесь следует обратить внимание на arg1=7 - это соответствует BarcodeInterface.State.NONE.ordinal()
            //Т.е. прилетело событие не окончания сканирования, а "просто так" в этом случае reader.countDownLatch = null
            //Т.к. этого события мы не инициировали, не будем на него реагировать.
            else {
                Logger.trace(TAG, fTitle + " - skip sendResult - NO SCAN CALLBACK");
                if (reader.countDownLatch != null) {
                    Logger.warning(TAG, fTitle + " WARNING countDownLatch!=null");
                }
            }
            Logger.trace(TAG, fTitle + "FINISH" + getTimeString(timer));
        }
    }

    @Override
    public boolean getFirmwareVersion(StringBuilder stringBuilder) {
        long timer = getTimer();
        Logger.trace(TAG, "getFirmwareVersion() START");
        StringBuilder sb = new StringBuilder();
        RESULT res = readerInterface.getFirmwareVersion(sb);
        stringBuilder.append(sb.toString().replace("\r", ""));//иначе это в лог не попадает
        Logger.trace(TAG, "getFirmwareVersion() FINISH return \"" + stringBuilder.toString() + "\" res=" + res.toString() + getTimeString(timer));
        return (res == RESULT.OK);
    }

    @Override
    public boolean getModel(String[] model) {
        model[0] = "Opticon MDI3100-SR Imager 2D";
        return true;
    }

    private static long getTimer() {
        return System.currentTimeMillis();
    }

    private static String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

    private String getResString(boolean res) {
        return (res) ? "OK" : "FAILED";
    }

    /**
     * запускает отложенное отключение ридера
     */
    public void softClose() {
        Logger.trace(TAG, "softClose(config=[" + config.toString() + "])");
        if (config.isAutoPowerOffEnabled() && timer == null) {
            startPowerOffTimer();
        }
    }

    private void startPowerOffTimer() {
        Logger.trace(TAG, "startPowerOffTimer(" + config.getAutoPowerOffDelay() + ")");
        timer = new Timer();
        timer.schedule(new CloseReaderTask(), config.getAutoPowerOffDelay());
    }

    //задача для таймера
    private class CloseReaderTask extends TimerTask {
        @Override
        public void run() {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    hardClose();
                }
            });
        }
    }

    /**
     * Сбрасывает таймер отложенного выключения
     */
    private void cancelPowerOffTimer() {
        Logger.trace(TAG, "cancelPowerOffTimer()");
        if (timer != null)
            timer.cancel();
        timer = null;
    }

    private void hardClose() {
        Logger.trace(TAG, "hardClose() START");
        long timeStart = getTimer();
        cancelPowerOffTimer();
        closeReader();
        powerOn(false);
        timer = null;
        Logger.trace(TAG, "hardClose() FINISH" + getTimeString(timeStart));
    }

    /**
     * Настройки считывателя ШК.
     */
    public static class Config {

        /**
         * Задержка перед автоматическим отключением питания ридера
         */
        private final long autoPowerOffDelay;
        /**
         * Разрешение на автоматической отключение питание ридера после чтения, записи
         */
        private final boolean autoPowerOffEnabled;
        /**
         * Cкорость передачи данных
         */
        private final int baudRate;

        public Config(long autoPowerOffDelay, boolean autoPowerOffEnabled, int baudRate) {
            this.autoPowerOffDelay = autoPowerOffDelay;
            this.autoPowerOffEnabled = autoPowerOffEnabled;
            this.baudRate = baudRate;
        }

        public long getAutoPowerOffDelay() {
            return autoPowerOffDelay;
        }

        public boolean isAutoPowerOffEnabled() {
            return autoPowerOffEnabled;
        }

        public int getBaudRate() {
            return baudRate;
        }

        @Override
        public String toString() {
            return "autoPowerOffDelay=" + autoPowerOffDelay + ", autoPowerOffEnabled=" + autoPowerOffEnabled + ", baudRate=" + baudRate;
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        Logger.trace(TAG, "setData(data=" + CommonUtils.bytesToHexWithoutSpaces(data) + ") START");
        long timer = getTimer();
        if (data != null && data.length > 0) {
            // Костыль, ридер добавляет левый байт в конце
            byte[] realData = new byte[data.length - 1];
            System.arraycopy(data, 0, realData, 0, realData.length);
            data = realData;
        }
        this.data = data;
        Logger.trace(TAG, "setData(data=" + CommonUtils.bytesToHexWithoutSpaces(data) + ") FINISH" + getTimeString(timer));
    }
}
