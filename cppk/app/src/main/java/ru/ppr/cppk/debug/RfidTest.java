package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;

import fr.coppernic.cpcframework.cpcask.Defines;
import fr.coppernic.cpcframework.cpcask.Reader;
import fr.coppernic.cpcframework.cpcask.sCARD_SearchExt;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.InterfacesCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.ManufacturersCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.ModelsCone;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt.PeripheralTypesCone;
import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.logger.Logger;

/**
 * Класс для экспериментов  с кардридером.
 *
 * @author G.Kashka
 */
public class RfidTest extends LoggedActivity implements OnClickListener {

    private String TAG = this.getClass().getSimpleName();

    private static final String ASK_READER_DESCRIPTION = "Cpc_Rfid_Ask_Ucm108";
    private static final String ASK_READER_PORT = "/dev/ttyHSL1";

    // RfidReal interface
    private boolean mIsPortOpened = false;
    private Reader mReader = null;
    private boolean mIsUsb = false;
    private String mName = "";

    // Power management
    private PowerMgmt mPowerMgmt;

    private TextView titleTextView;
    private Button openBtn;
    private Button getFwBtn;
    private Button scanBtn;
    private Button testBtn;
    private static EditText logEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_with_barcode);

        mPowerMgmt = new PowerMgmt(getApplicationContext());

        findViews();
        titleTextView.setText(R.string.rfid_test);
        setOnClicListeners();

        if (Reader.isUsb(RfidTest.this)) {
            mName = ASK_READER_DESCRIPTION;
            mIsUsb = true;
        } else {
            mName = ASK_READER_PORT;
            mIsUsb = false;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Reader.getInstance(getApplicationContext(), reader -> {
            Logger.info(TAG, "onCreate() Reader getInstance Done");
            mReader = reader;
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Logger.error(TAG, e);
            Thread.currentThread().interrupt();
        }

        Logger.info(TAG, "onCreate finish");
    }

    private void setOnClicListeners() {
        openBtn.setOnClickListener(this);
        getFwBtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        testBtn.setOnClickListener(this);
    }

    private void findViews() {
        titleTextView = (TextView) findViewById(R.id.title);
        openBtn = (Button) findViewById(R.id.openBtn);
        getFwBtn = (Button) findViewById(R.id.getFwBtn);
        scanBtn = (Button) findViewById(R.id.scanBtn);
        testBtn = (Button) findViewById(R.id.testBtn);
        logEditText = (EditText) findViewById(R.id.logEditText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openBtn:
                if (mIsPortOpened)
                    closePort();
                else
                    openPort();
                break;
            case R.id.getFwBtn:
                getFw();
                break;
            case R.id.scanBtn:
                getRfidAtr();
                break;
            case R.id.testBtn:
                startTest();
                break;
        }

    }

    /**
     * Обработчик тестовой кнопки
     */
    private void startTest() {

        byte numSector = (byte) 0x09;
        byte keyAorB = (byte) 0x00; // 654 = 363534
        byte keyIndex = (byte) 16;

        byte[] status = new byte[1];
        byte[] mifareType = new byte[8];
        byte[] serialNumber = new byte[4];
        byte[] dataRead = new byte[16];

        mifareType[0] = 0x08;

        try {

            // int res = mReader.srxRead(address, nbBytesToRead , status,
            // dataLength, data);

            // 0008b60bbe73

            int res = mReader.mifareReadSector(numSector, keyAorB, keyIndex, mifareType, serialNumber, dataRead, status);

            Logger.info(TAG, "попытка авторизации res=" + res + " status=" + status[0]);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dataRead.length; i++) {
                sb.append(String.format("%02X ", dataRead[i]));
            }
            logEditText.setText("mifareReadSector DATA : " + sb.toString() + "\n" + logEditText.getText().toString());

            for (int j = 0; j < 10; j++) {

                status = new byte[1];
                dataRead = new byte[16];

                res = mReader.mifareAuthenticate((byte) j/* block */, (byte) 0x01/* aORb */, (byte) 0x00/* keyIndex */, mifareType/* mifareType */, serialNumber, status);
                Logger.info(TAG, "попытка авторизации res=" + res + " status=" + status[0]);

                Logger.info(TAG, "читаем блок №" + j);

                res = mReader.mifareReadBlock((byte) (j), dataRead, status);

                Logger.info(TAG, "res=" + res);

                if (Defines.RCSC_Ok == res) {
                    Logger.info(TAG, "считывание блока было успешным");
                } else if (Defines.RCSC_InputDataWrong == res) {
                    Logger.info(TAG, "Error: RCSC_InputDataWrong");
                }

                StringBuilder sbAtr = new StringBuilder();
                for (int i = 0; i < dataRead.length; i++) {
                    sbAtr.append(String.format("%02X ", dataRead[i]));
                }
                logEditText.setText("srxReadBlocks DATA : " + sbAtr.toString() + "\n" + logEditText.getText().toString());

                if (status[0] == 0x02) {
                    Logger.info(TAG, "srxReadBlocks status succesfull");
                    /*
					 * if (dataLength[0] > 0) { // Builds the string ATR
					 * StringBuilder sbAtr = new StringBuilder(); for (int i =
					 * 0; i < dataLength[0]; i++) {
					 * sbAtr.append(String.format("%02X ", data[i])); }
					 * logEditText.setText("srxReadBlocks DATA : " +
					 * sbAtr.toString() + "\n" +
					 * logEditText.getText().toString()); }
					 */

                } else {
                    if (status[0] == 0x03)
                        Logger.info(TAG, "srxReadBlocks status faild : bad params: " + status[0]);
                    else if (status[0] == 0x01)
                        Logger.info(TAG, "srxReadBlocks status faild : bad CRC: " + status[0]);
                    else if (status[0] == 0x00)
                        Logger.info(TAG, "srxReadBlocks status faild : communication interrupted: " + status[0]);
                    else
                        Logger.info(TAG, "srxReadBlocks status faild : " + status[0]);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getRfidAtr() {

        sCARD_SearchExt search = new sCARD_SearchExt();

        search.CONT = 0;
        search.INNO = 1;
        search.ISOA = 1;
        search.ISOB = 1;
        search.MIFARE = 1;
        search.MONO = 1;
        search.MV4k = 1;
        search.MV5k = 1;
        search.TICK = 1;

        int mask = Defines.SEARCH_MASK_INNO | Defines.SEARCH_MASK_ISOA | Defines.SEARCH_MASK_ISOB | Defines.SEARCH_MASK_MIFARE | Defines.SEARCH_MASK_MONO | Defines.SEARCH_MASK_MV4K
                | Defines.SEARCH_MASK_MV5K | Defines.SEARCH_MASK_TICK;
        byte[] COM = new byte[1];
        int[] lpcbATR = new int[1];
        byte[] lpATR = new byte[32];

        int ret = mReader.cscSearchCardExt(search, mask, (byte) 0x01, (byte) 0xFF, COM, lpcbATR, lpATR);

        if (ret != Defines.RCSC_Ok) {
            switch (ret) {
                case Defines.RCSC_Timeout:
                    addLog("Error : Search card Time out");
                    return;
                default:
                    addLog("Error : Search card");
                    return;
            }
        }

        if (COM[0] == 0x6F || lpcbATR[0] == 0) {
            addLog("No card detected");
        } else {
            if (lpcbATR[0] > 0) {
                // Builds the string ATR
                StringBuilder sbAtr = new StringBuilder();
                for (int i = 0; i < lpcbATR[0]; i++) {
                    sbAtr.append(String.format("%02X ", lpATR[i]));
                }
                addLog("ATR : " + sbAtr.toString());
            }
        }
    }

    private void getFw() {
        StringBuilder sbVersion = new StringBuilder();
        int res = mReader.cscVersionCsc(sbVersion);
        if (res == Defines.RCSC_Ok) {
            addLog("FirmWare Version: " + sbVersion.toString());
        } else {
            addLog("Reset error : " + Defines.errorLookUp(res));
        }

        // Sets the IO for the C-ONE
        mReader.cscConfigIoExt((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00);
    }

    private void openPort() {

        if (mReader != null) {

            if (mName.compareTo(ASK_READER_PORT) == 0) {
                mPowerMgmt.setPower(PeripheralTypesCone.RfidSc, ManufacturersCone.Ask, ModelsCone.Ucm108, InterfacesCone.ExpansionPort, true);
            }

            int res = mReader.cscOpen(mName, 115200, mIsUsb);

            if (res == Defines.RCSC_Ok) {
                addLog("port opened");
                openBtn.setText(R.string.close);
                getFwBtn.setEnabled(true);
                scanBtn.setEnabled(true);
                mIsPortOpened = true;
            } else {
                addLog("Open error :" + Defines.errorLookUp(res));
            }
        }

    }

    private void closePort() {
        if (mReader != null) {
            mReader.cscClose();
            addLog("port closed");
            openBtn.setText(R.string.open);
            getFwBtn.setEnabled(false);
            scanBtn.setEnabled(false);
            mIsPortOpened = false;

            if (mName.compareTo(ASK_READER_PORT) == 0) {
                mPowerMgmt.setPower(PeripheralTypesCone.RfidSc, ManufacturersCone.Ask, ModelsCone.Ucm108, InterfacesCone.ExpansionPort, false);
            }
        }
    }

    private void addLog(String text) {
        Logger.info(RfidTest.class.getSimpleName(), text);
        logEditText.setText(text + "\n" + logEditText.getText().toString());
    }
}
