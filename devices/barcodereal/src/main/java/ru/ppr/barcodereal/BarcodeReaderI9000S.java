package ru.ppr.barcodereal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.support.annotation.Nullable;
import android.view.View;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.logger.Logger;

import static android.device.ScanManager.ACTION_DECODE;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;

/**
 * Реализация считывателя ШК, работающая на встроенном ридере МКАССА.
 */

public class BarcodeReaderI9000S extends Activity implements IBarcodeReader {
    private static final String TAG = Logger.makeLogTag(BarcodeReaderI9000S.class);
    private static final int MSG_SHOW_SCAN_RESULT = 1;
    private static final int MSG_SHOW_SCAN_IMAGE = 2;
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    /**
     * Флаг активности (включенности) ридера
     */
    private boolean isOpened = false;
    private byte [] obtain_data;
    private String barcodeStr;
    private byte barcodeType = 0;
    int barcodeLen = 0;
    private android.device.ScanManager readerInterface = null;

    private static long getTimer() {
        return System.currentTimeMillis();
    }
    private String getResString(boolean res) {
        return (res) ? "OK" : "FAILED";
    }
    Context context;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.trace(TAG, "onReceive , action:" + action);
                // Get scan results, including string and byte data etc.
                obtain_data = intent.getByteArrayExtra(android.device.ScanManager.DECODE_DATA_TAG);

/*                String lFile = Environment.getExternalStorageDirectory() + "/pos/log1.txt";
            FileOutputStream fw = null;
            try {
                fw = new FileOutputStream(lFile);
                fw.write(obtain_data);
            }
            catch(Exception e) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }*/

                barcodeLen = intent.getIntExtra(android.device.ScanManager.BARCODE_LENGTH_TAG, 0);
                barcodeType = intent.getByteExtra(android.device.ScanManager.BARCODE_TYPE_TAG, (byte) 0);
                barcodeStr = intent.getStringExtra(android.device.ScanManager.BARCODE_STRING_TAG);
                Logger.trace(TAG, "barcode type:" + barcodeType);
                String scanResult = new String(obtain_data, 0, barcodeLen);
                // print scan results.
                scanResult = " length：" + barcodeLen + "\nbarcode：" + scanResult + "\nbytesToHexString：" + bytesToHexString(obtain_data) + "\nbarcodeStr:" + barcodeStr;
                Logger.trace(TAG, "BroadcastReceiver Result = " + scanResult);
                Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_RESULT);
                msg.obj = scanResult;
                mHandler.sendMessage(msg);
            }
    };

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_SCAN_RESULT:
                    String scanResult = (String) msg.obj;
//                    printScanResult(scanResult);
                    break;
            }
        }
    };

    private void registerReceiver(boolean register) {
        Logger.trace(TAG, "registerReceiver START = " +  register);
        if (register){
            if (readerInterface != null) {
                Logger.trace(TAG, "registerReceiver Create Intent");
                IntentFilter filter = new IntentFilter();
                int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
                String[] value_buf = readerInterface.getParameterString(idbuf);
                if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                    Logger.trace(TAG, "registerReceiver Add action " + value_buf[0]);
                    filter.addAction(value_buf[0]);
                } else {
                    Logger.trace(TAG, "registerReceiver Add action " + ACTION_DECODE);
                    filter.addAction(ACTION_DECODE);
                }
//            filter.addAction(ACTION_CAPTURE_IMAGE);
                if (this.context == null)
                    Logger.trace(TAG, "context == null");
                else
                    Logger.trace(TAG, "context != null");
                this.context.registerReceiver(mReceiver, filter);
            }
        } else if (readerInterface != null) {
            Logger.trace(TAG, "registerReceiver Stop Decoding");
            readerInterface.stopDecode();
            this.context.unregisterReceiver(mReceiver);
        }
        Logger.trace(TAG, "registerReceiver STOP = " +  register);
    }

    public BarcodeReaderI9000S(Context context, BarcodeReaderMDI3100.Config config, ExecutorService executorService) {
        long timer = getTimer();
        Logger.trace(TAG, "BarcodeReaderI9000S() START");
        readerInterface = new android.device.ScanManager();
        this.context = context;
        Logger.trace(TAG, "BarcodeReaderI9000S() context" + context.getClass().getSimpleName());
        Logger.trace(TAG, "BarcodeReaderI9000S() FINISH" + getTimeString(timer));
    }

    private static String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

    @Override
    @Nullable
    public byte[] scan() {
        long timer = getTimer();
        Logger.trace(TAG, "scan() START");
        byte[] scanData = null;

            if (!isOpened)
                if (!open()) {
                Logger.error(TAG, "The scanner wasn't open");
            }
            if (isOpened){
                boolean lpower = readerInterface.getTriggerLockState();
                Logger.trace(TAG, "scan() trigger = " + lpower);
                if (!lpower){
                boolean lres = readerInterface.startDecode();
                Logger.trace(TAG, "scan() Result = " + lres);
                }
            }
        if ((barcodeLen != 0) && (barcodeStr.length() > 0)){
            readerInterface.stopDecode();
//            scanData = obtain_data;
            if (barcodeType == 28) {
                scanData = new byte[obtain_data.length / 2];
                int index = 0;
                for (int i = 0; i < barcodeStr.length(); i += 2) {
                    String ls = barcodeStr.substring(i, i + 2);
                    int litem = parseInt(ls, 16);
                    scanData[index] = (byte) (litem);
                    index++;
                }
            }
            else scanData = obtain_data.clone();
            Logger.trace(TAG, "scan() recv length = " + barcodeLen);
            barcodeLen = 0;
            barcodeStr = "";
            close();
        }
        return scanData;
    }
    @Override
    public boolean open() {
        long timer = getTimer();
        Logger.trace(TAG, "open() START");
        boolean powerOn = false;
        if (readerInterface == null) {
            readerInterface = new android.device.ScanManager();
        }
        if (readerInterface != null) {
            registerReceiver(true);
                powerOn = readerInterface.openScanner();
                if (powerOn){
                    readerInterface.enableAllSymbologies(true);   // or execute enableSymbologyDemo() || enableSymbologyDemo2() is the same.
                    int mode = 0;
                    powerOn = readerInterface.switchOutputMode(mode);
                }
        }
        isOpened = powerOn;
        Logger.trace(TAG, "open() FINISH " + getResString(powerOn) + getTimeString(timer));
        return powerOn;
    }
    @Override
    public void close() {
        Logger.trace(TAG, "close() START");
        long timer = getTimer();
        readerInterface.closeScanner();
        registerReceiver(false);
        isOpened = false;
        readerInterface = null;
        Logger.trace(TAG, "close() FINISH" + getTimeString(timer));
    }
    @Override
    public boolean getModel(String[] model) {
        model[0] = "Scaner I9000S";
        return true;
    }
    @Override
    public boolean getFirmwareVersion(StringBuilder stringBuilder) {
        long timer = getTimer();
        Logger.trace(TAG, "getFirmwareVersion() START");
/*        StringBuilder sb = new StringBuilder();
        RESULT res = readerInterface.getFirmwareVersion(sb);
        stringBuilder.append(sb.toString().replace("\r", ""));//иначе это в лог не попадает
        Logger.trace(TAG, "getFirmwareVersion() FINISH return \"" + stringBuilder.toString() + "\" res=" + res.toString() + getTimeString(timer));*/
//        return (res == RESULT.OK);
        return (true);
    }
}
