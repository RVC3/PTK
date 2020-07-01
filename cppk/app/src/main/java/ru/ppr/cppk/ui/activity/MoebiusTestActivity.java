package ru.ppr.cppk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.moebiusdrvr.DateTimeFE;
import com.moebiusdrvr.KKMDopInfo;
import com.moebiusdrvr.KKMInfoStateData;
import com.moebiusdrvr.KKMTotSendHost;
import com.moebiusdrvr.MoebiusFE;
import com.moebiusdrvr.PaymentSt;
import com.moebiusdrvr.ResultAsSingleString;
import com.moebiusdrvr.ResultAsString;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.CP866;
import ru.ppr.logger.Logger;

/////////////////////////////////////////
//class KKMInfoData
//{
//    public byte State = 0;
//
//    public short Year = 0;
//    public byte Month = 0;
//    public byte Day = 0;
//    public byte Hour = 0;
//    public byte Min = 0;
//    public byte Sec = 0;
//
//    public byte[] FubNum = null;
//
//    public short NumShift = 0;
//    public int DocNum = 0;
//}
/////////////////////////////////////////////////////

public class MoebiusTestActivity extends Activity {

    private static final String TAG = "MainActivity";

    static final byte[] CARRIAGE_RETURN_WITH_LINE_FEED = new byte[]{'\r', '\n'};

    enum DocType {
        SALE(1), RETURN(2);

        private final int code;

        DocType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static DocType create(int code) {
            DocType[] types = values();
            for (DocType type : types) {
                if (code == type.getCode()) {
                    return type;
                }
            }
            return null;
        }
    }


    enum PaymentType {
        CASH(1), CARD(2);

        private final int code;

        PaymentType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PaymentType create(int code) {
            PaymentType[] methods = values();
            for (PaymentType method : methods) {
                if (code == method.code) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * Called when the activity is first created.
     */
    private final Semaphore semDone = new Semaphore(0, true);

    private SimpleDateFormat formatterForEklzTime;

    private MoebiusFE moebius = null;
    private final int SLEEPINTERVAL = 500;

    PaymentSt[] cardPay = new PaymentSt[16];
    PaymentSt[] creditPay = new PaymentSt[16];

    private EditText printerNameEt;
    private EditText printerMacEt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moebius_test);


        formatterForEklzTime = new SimpleDateFormat("dd.MM.yyy HH:mm");
        formatterForEklzTime.setTimeZone(TimeZone.getDefault());

        printerNameEt = (EditText) findViewById(R.id.printerName);
        printerNameEt.setText("ZEBRA-EZ320K 14000005");
//        String sDevice = "ZEBRA-EZ320K 14000005";    /* Наша зебра */
//    	String sDevice = "ZEBRA-EZ320K 15000007";    /* Антон */
//    	String sDevice = "ZEBRA-EZ320K 00000002";    /* Антон */
//    	String sDevice = "ZEBRA-EZ320K 000002";    /* Антон */
        //String sDevice = "ZEBRA-EZ320K 00002222";   /* Женя */

        printerMacEt = (EditText) findViewById(R.id.printerMac);
        printerMacEt.setText("10:00:e8:6c:3d:f0");

        for (int i = 0; i < cardPay.length; i++) {
            cardPay[i] = new PaymentSt();
            creditPay[i] = new PaymentSt();
        }

        try {
            moebius = new MoebiusFE(this);
            moebius.setDebug(true);
//            moebius.getAllDevicesAsync();
//            String[] devices = moebius.getDiscoveryResult(20);

            //String[] devices = getTestAllDevicesAsync();
        } catch (Exception e) {
            Globals.getInstance().getToaster().showToast(e.getMessage());
        }
    }

    public void onFullTest(View view) {
        // -----------------------------------------------------------------------------------
        String sDescription;
        byte err;
        DateTimeFE dt = new DateTimeFE();
        PaymentSt[] cardPay = new PaymentSt[16];
        PaymentSt[] creditPay = new PaymentSt[16];
        for (int i = 0; i < cardPay.length; i++) {
            cardPay[i] = new PaymentSt();
            creditPay[i] = new PaymentSt();
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "kkmGetCommonData";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.kkmGetCommonData((byte) 0);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "kkmOpenShift";
        Log.w("~~~ --- Activity ---", sDescription);
        try {
            err = moebius.kkmOpenShift(new DateTimeFE(), (byte) 4, "Иванов И.И.");
            Log.e("~~~ --- Activity ---", sDescription + ": retCode=" + err);
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            if (err != 0)
                return;
        } catch (Exception e) {
            Globals.getInstance().getToaster().showToast(e.getMessage());
            e.printStackTrace();
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_put";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_put((int) 111, (int) 123, (int) 123, (byte) 1, false, false, "", "", (byte) 1, false, true);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_head";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_head((byte) 1, (byte) 1, "0");
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        //вставим произвольные строки в чек
        err = moebius.prn_tpm_write(8, CP866.toBytes("Some Text 1"));
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        err = moebius.prn_tpm_write(8, CP866.toBytes(""));
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        err = moebius.prn_tpm_write(8, CP866.toBytes("Some Text 2"));
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        sDescription = "mbs_RecItem";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_RecItem("какой-то текст", "66", "1", (int) 111, (int) 1, (int) 111, (int) 0, false, "");
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_Subtotal";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_Subtotal((int) 111);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_tot";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_tot((int) 111, (int) 500, (int) 611, cardPay, creditPay, "xxx", (int) 1, (int) 1);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_head";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_head((byte) -2, (byte) 1, "0");
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_RecItem";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_RecItem("товар 1", "0123456789", "1", (int) 111, (int) 1, (int) 111, (int) 0, false, "");
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_Subtotal";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_Subtotal((int) 111);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_tot";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_tot((int) 111, (int) 500, (int) 611, cardPay, creditPay, "yyy", (int) 1, (int) 1);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_put";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.mbs_put((int) 111, (int) 123, (int) 123, (byte) 2, false, false, "", "", (byte) 1, false, true);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "X-report";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.kkmGetShiftData((byte) 0);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "kkmCloseShift";
        Log.w("~~~ --- Activity ---", sDescription);
        try {
            err = moebius.kkmCloseShift(new DateTimeFE(), (byte) 0xC0);
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            if (err != 0)
                return;
        } catch (Exception e) {
            Globals.getInstance().getToaster().showToast(e.getMessage());
            e.printStackTrace();
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "kkmGetCommonData";
        Log.w("~~~ --- Activity ---", sDescription);
        err = moebius.kkmGetCommonData((byte) 0);
        if (err != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        Globals.getInstance().getToaster().showToast("Successful test");
        Log.w("~~~ --- Activity ---", "FullTest завершен успешно!");
    }

    public void onMyReceipt(View view) {
        String sDescription = "";
        byte rc = 0;
        PaymentSt[] cardPay = new PaymentSt[16];
        PaymentSt[] creditPay = new PaymentSt[16];
        int lenPay = 16;
        for (int i = 0; i < lenPay; i++) {
            cardPay[i] = new PaymentSt();
            creditPay[i] = new PaymentSt();
        }

        //---------------------------------------------------------------------------
        int VatID0 = 0;
        int VatID1 = 1;
        int VatID2 = 2;

        //---------------------------------------------------------------------------

//                sDescription = "GetVatValue";
//                Log.w("~~~ --- Activity ---", sDescription);
//                ResultAsSingleInt res = new ResultAsSingleInt();
//                rc = moebius.getVatValue(VatID2, res);
//                if (rc != 0)
//                {
//                    Toaster.showToast(this,  sDescription + ": retCode=" + rc);
//                    break;
//                }

//                byte IndentReceiptHead = moebius.getIndentReceiptHead();
//                IndentReceiptHead = (20<<1) | 1;
//                rc = moebius.setIndentReceiptHead(IndentReceiptHead);
//                IndentReceiptHead = moebius.getIndentReceiptHead();

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_head";
        Log.w("~~~ --- Activity ---", sDescription);
        rc = moebius.mbs_head((byte) 1, (byte) 1, "0");
        if (rc != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_RecItem";
        Log.w("~~~ --- Activity ---", sDescription);
        //rc = moebius.mbs_RecItem("товар 1", "0123456789", "1", (int)111, (int)1, (int)111, (int)0, false, "");
        rc = moebius.mbs_RecItem("товар 1", "0123456789", "1", (int) 10000, (int) 1, (int) 10000, (int) VatID0, false, "");
        if (rc != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
            return;
        }
        rc = moebius.mbs_RecItem("товар 2", "0123456789", "1", (int) 20000, (int) 1, (int) 20000, (int) VatID0, false, "");
        if (rc != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
            return;
        }
//                rc = moebius.mbs_RecItem("товар 3", "0123456789", "1", (int)10000, (int)1, (int)10000, (int)VatID0, false, "");
//                if (rc != 0)
//                {
//                    Toaster.showToast(this,  sDescription + ": retCode=" + rc);
//                    break;
//                }
//                rc = moebius.mbs_RecItem("товар 4", "0123456789", "1", (int)10000, (int)1, (int)10000, (int)VatID0, false, "");
//                if (rc != 0)
//                {
//                    Toaster.showToast(this,  sDescription + ": retCode=" + rc);
//                    break;
//                }

//                // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//                sDescription = "mbs_Subtotal";
//                Log.w("~~~ --- Activity ---", sDescription);
//                //rc = moebius.mbs_Subtotal((int)10000);
//                rc = moebius.mbs_Subtotal((int)30000);
//                if (rc != 0)
//                {
//                    Toaster.showToast(this,  sDescription + ": retCode=" + rc);
//                    break;
//                }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        sDescription = "mbs_tot";
        Log.w("~~~ --- Activity ---", sDescription);
        //rc = moebius.mbs_tot((int)10000, (int)0, (int)10000, cardPay, creditPay, "xxx", (int)1, (int)1);
        rc = moebius.mbs_tot((int) 30000, (int) 0, (int) 30000, cardPay, creditPay, "~~~~~~~~~~~~~~~~~~~~", (int) 1, (int) 1);
        if (rc != 0) {
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
            return;
        }

        //===========================================================================
        //moebius.disconnect();

        //...........................................................................

        //---------------------------------------------------------------------------
    }

    public void onOpenShift(View view) {
        String sDescription;
        byte err = 0;

        sDescription = "Open shift -- 1";
        String sPar = "������ �.�.";
        Log.w("~~~ --- Activity ---", sDescription);
        try {
            err = moebius.kkmOpenShift(new DateTimeFE(), (byte) 4, sPar);
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            if (err != 0)
                return;
        } catch (Exception e) {
            Globals.getInstance().getToaster().showToast(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    public void onCloseShift(View view) {
        String sDescription;
        byte err = 0;

        sDescription = "Close shift";
        Log.w("~~~ --- Activity ---", sDescription);
        try {
            err = moebius.kkmCloseShift(new DateTimeFE(), (byte) 0xC0);
            Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + err);
            if (err != 0)
                return;
        } catch (Exception e) {
            Globals.getInstance().getToaster().showToast(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    //###############################################
    //===============================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sDescription = "onActivityResult: requestCode=" + requestCode + "; resultCode=" + resultCode;
        Log.w("~~~ --- Activity ---", sDescription);
    }

    //###############################################
    public void onClick(View view) {
        // "ZEBRA-EZ320K 15999999"
        String[] devices = null;

        switch (view.getId()) {
            case (R.id.DiscoveryBtn): {
                try {
                    moebius.getAllDevicesAsync();
                } catch (Exception e) {
                    Globals.getInstance().getToaster().showToast(e.getMessage());
                    return;
                }
                //Toaster.showToast(this,  "getAllDevicesAsync: Successful");
                break;
            }
            case (R.id.button2): {
                try {
                    devices = moebius.getDiscoveryResult(5);
                } catch (InterruptedException e) {
                    Globals.getInstance().getToaster().showToast(e.getMessage());
                    return;
                }
                String result = "getDiscoveryResult Successful: ";
                if (devices == null)
                    result += "<NULL>";
                else {
                    for (String val : devices) {
                        result += "\r\n";
                        result += val;
                    }
                }
                Globals.getInstance().getToaster().showToast(result);
                break;
            }
            case (R.id.InitForNameBtn): {
                try {
                    String sDevice = printerNameEt.getText().toString();
                    Log.w("~~~ --- Init Device ---", sDevice);
                    Globals.getInstance().getToaster().showToast(sDevice);
                    moebius.init(sDevice);
                } catch (Exception e) {
                    Globals.getInstance().getToaster().showToast(e.getMessage());
                    return;
                }
                break;
            }

            case (R.id.InitForMacBtn): {
                try {
                    String sDevice = printerMacEt.getText().toString().toUpperCase();
                    Log.w("~~~ --- Init Device ---", sDevice);
                    Globals.getInstance().getToaster().showToast(sDevice);
                    moebius.initFromAddress(sDevice);
                } catch (Exception e) {
                    Globals.getInstance().getToaster().showToast(e.getMessage());
                    return;
                }
                break;
            }

            case (R.id.button9): {
                String sDescription;
                byte rc = 0;


                sDescription = "kkmGetKKMDopInfo";
                Log.w("~~~ --- Activity ---", sDescription);
                KKMDopInfo DopInfo = new KKMDopInfo();
                rc = moebius.kkmGetKKMDopInfo(DopInfo);
                if (rc != 0) {
                    Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
                    break;
                }

                break;
            }
            case (R.id.button11): {
                String sDescription;
                byte rc = 0;
                sDescription = "kkmGetKKMInfoEx";
                Log.w("~~~ --- Activity ---", sDescription);
                KKMTotSendHost TotSendHost = new KKMTotSendHost();
                rc = moebius.kkmGetKKMInfoEx(TotSendHost);
                if (rc != 0) {
                    Globals.getInstance().getToaster().showToast(sDescription + ": retCode=" + rc);
                    break;
                }
                Globals.getInstance().getToaster().showToast(sDescription + " Ok");

//                sDescription = "sendBlock";
//                Log.w("~~~ --- Activity ---", sDescription);
//                for ( int i = 1; i <+ 400; i++)
//                {
//                    String sData = String.valueOf(i);
//                    sData += "\r\n";
//                    byte[] arrData = sData.getBytes();
//                    rc = moebius.sendBlock(arrData);
//                    if (rc != 0)
//                    {
//                        Toaster.showToast(this,  sDescription + ": retCode=" + rc);
//                        break;
//                    }
//                }
                break;
            }
            case (R.id.IsConnectedBtn): {
                boolean isConnected = false;
                try {
                    isConnected = moebius.isConnected(5);
                } catch (InterruptedException e) {
                    Globals.getInstance().getToaster().showToast("isConnected exception: " + e.getMessage());
                    break;
                }

                if (isConnected) {
                    Globals.getInstance().getToaster().showToast("Connected!!!");
                } else {
                    Globals.getInstance().getToaster().showToast("Not connected");
                }
                break;
            }
            case (R.id.DisconnectBtn): {
                moebius.disconnect();
                Globals.getInstance().getToaster().showToast("Disconnected!!!");
                break;
            }
            case (R.id.sendBlockTestBtn): {
                try {
                    for (int i = 0; i < 15; i++) {
                        printTextInNormalMode("" + i + "------------------------");
                    }
                    printAdjustingTable();
                    for (int i = 0; i < 40; i++) {
                        printTextInNormalMode("-" + i + "--------------------");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case (R.id.checkConnAfterZReport): {

                try {
                    checkConnectionByManager();
                    isShiftOpened();
                    setHeaderLines(Collections.singletonList("ПТК E1444000983 (ID 96)"));
                    openShift(1, "Иванов И. И.");
                    getDate();
                    isShiftOpened();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getDate();
                    printTextInNormalMode("АО ЦЕНТРАЛЬНАЯ ППК");
                    printTextInNormalMode("УКК КУРСКИЙ УЧАСТОК");
                    printTextInNormalMode("Иванов И. И.           К5664");
                    printTextInNormalMode("ПТК E1444000983 (ID 96)");
                    printTextInNormalMode("ИНН 0123456789");
                    printTextInNormalMode("ЭКЛЗ 0123456789");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("      ЛЬГОТНАЯ СМЕННАЯ      ");
                    printTextInNormalMode("         ВЕДОМОСТЬ          ");
                    printTextInNormalMode("        СМЕНА № 133         ");
                    printTextInNormalMode("24.03.2016 12:09:09");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("МАРШРУТ 0");
                    printTextInNormalMode("БЕЗ ГРУППЫ");
                    printTextInNormalMode(" --ЛЬГОТА 2605--");
                    printTextInNormalMode("  ВСЕГО        =3");
                    printTextInNormalMode("  ВЫП. ДОХОД   =153.70");
                    printTextInNormalMode("Региональные");
                    printTextInNormalMode(" --ЛЬГОТА 2763--");
                    printTextInNormalMode("  ВСЕГО        =1");
                    printTextInNormalMode("  ВЫП. ДОХОД   =0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("ПОЛНЫЙ ВЫП. ДОХОД");
                    printTextInNormalMode("ЗА СМЕНУ       =153.70");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("      ПЕЧАТЬ ЗАКОНЧЕНА      ");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    checkConnectionByManager();
                    waitPendingOperations();
                    getCashInFR();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getDate();
                    printTextInNormalMode("АО ЦЕНТРАЛЬНАЯ ППК");
                    printTextInNormalMode("УКК КУРСКИЙ УЧАСТОК");
                    printTextInNormalMode("Иванов И. И.           К5664");
                    printTextInNormalMode("ПТК E1444000983 (ID 96)");
                    printTextInNormalMode("ИНН 0123456789");
                    printTextInNormalMode("ЭКЛЗ 0123456789");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("     СМЕННАЯ ВЕДОМОСТЬ      ");
                    printTextInNormalMode("        СМЕНА № 133         ");
                    printTextInNormalMode("24.03.2016 12:09:16");
                    printTextInNormalMode("ПО v.0.39.201.0");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("ПРОБНЫХ ВЕДОМОСТЕЙ 3");
                    printTextInNormalMode("КОНТР. ЖУРН.       3");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("СУММА ПО ФИСКАЛЬНОМУ");
                    printTextInNormalMode("РЕГИСТРАТОРУ");
                    printTextInNormalMode("=0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("НАЧАЛО СМЕНЫ");
                    printTextInNormalMode(" ПЕРВЫЙ ДОКУМЕНТ");
                    printTextInNormalMode(" 24.03.2016 10:02:19");
                    printTextInNormalMode(" ДОК. № 000047");
                    printTextInNormalMode(" ОПЕРАТОР № 1");
                    printTextInNormalMode("ОКОНЧАНИЕ СМЕНЫ");
                    printTextInNormalMode(" ПОСЛЕДНИЙ ДОКУМЕНТ");
                    printTextInNormalMode(" 24.03.2016 11:21:17");
                    printTextInNormalMode(" ДОК. № 000053");
                    printTextInNormalMode(" ОПЕРАТОР № 1");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("ВЫРУЧКА");
                    printTextInNormalMode(" МЕС.          =3285.90");
                    printTextInNormalMode(" СУММ АННУЛ.   =726.50");
                    printTextInNormalMode(" ЗА ВЫЧЕТОМ");
                    printTextInNormalMode(" АННУЛ.        =2559.40");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("ВЫРУЧКА ЗА СМЕНУ");
                    printTextInNormalMode(" ВСЕГО         =470.30");
                    printTextInNormalMode("  ТАРИФ        =420.30");
                    printTextInNormalMode("  СУММ АННУЛ.  =143.50");
                    printTextInNormalMode("  ТАРИФ ЗА ВЫЧЕТОМ");
                    printTextInNormalMode("  АННУЛ.       =276.80");
                    printTextInNormalMode("  СБОР         =50.00");
                    printTextInNormalMode("  СУММ АННУЛ.  =0.00");
                    printTextInNormalMode("  СБОР ЗА ВЫЧЕТОМ");
                    printTextInNormalMode("  АННУЛ.       =50.00");
                    printTextInNormalMode("    ВКЛ. НДС   =7.63");
                    printTextInNormalMode(" НАЛИЧНЫМИ     =470.30");
                    printTextInNormalMode(" ЗА ВЫЧЕТОМ");
                    printTextInNormalMode(" АННУЛ.        =326.80");
                    printTextInNormalMode(" ПО БАНКОВСКИМ");
                    printTextInNormalMode(" КАРТАМ        =0.00");
                    printTextInNormalMode(" ЗА ВЫЧЕТОМ");
                    printTextInNormalMode(" АННУЛ.(БАНК)  =0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("КОЛИЧЕСТВО ДОКУМЕНТОВ");
                    printTextInNormalMode("ЗА СМЕНУ");
                    printTextInNormalMode(" ВСЕГО         =7");
                    printTextInNormalMode(" ПРОБН.        =1");
                    printTextInNormalMode(" РАЗОВЫХ       =5");
                    printTextInNormalMode("  ВКЛ.ДОПЛ.7000=0");
                    printTextInNormalMode(" БАГАЖ         =0");
                    printTextInNormalMode(" АННУЛ.        =1");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("          БАГАЖ");
                    printTextInNormalMode("СУММА          =0.00");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("   ПРОВЕРЕНО ДОКУМЕНТОВ");
                    printTextInNormalMode("БСК АБОНЕМЕНТЫ =0");
                    printTextInNormalMode("БСК РАЗОВЫЕ    =7");
                    printTextInNormalMode("РАЗОВЫЕ БИЛЕТЫ =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("     РАЗОВЫЕ ПОЛНЫЕ");
                    printTextInNormalMode("СУММА          =470.30");
                    printTextInNormalMode("КОЛИЧ.         =5");
                    printTextInNormalMode("СУММ АННУЛ.    =143.50");
                    printTextInNormalMode("АННУЛ.         =1");
                    printTextInNormalMode("ЗА ВЫЧЕТОМ");
                    printTextInNormalMode("АННУЛ.         =326.80");
                    printTextInNormalMode(" ВКЛЮЧАЯ СБОР");
                    printTextInNormalMode(" ЗА ВЫЧЕТ АНН. =50.00");
                    printTextInNormalMode("НДС            =7.63");
                    printTextInNormalMode("ПО БАНКОВСКИМ");
                    printTextInNormalMode("КАРТАМ         =0.00");
                    printTextInNormalMode("--> (в одну сторону)");
                    printTextInNormalMode("СУММА          =470.30");
                    printTextInNormalMode("КОЛИЧ.         =5");
                    printTextInNormalMode("СУММ АННУЛ.    =143.50");
                    printTextInNormalMode("АННУЛ.         =1");
                    printTextInNormalMode("ЗА ВЫЧЕТОМ");
                    printTextInNormalMode("АННУЛ.         =326.80");
                    printTextInNormalMode(" --ПОЛНЫХ--");
                    printTextInNormalMode("  СУММА        =173.00");
                    printTextInNormalMode("  КОЛИЧ.       =1");
                    printTextInNormalMode(" --ЛЬГОТНЫХ--");
                    printTextInNormalMode("  СУММА        =153.80");
                    printTextInNormalMode("  КОЛИЧ.       =3");
                    printTextInNormalMode("  ВЫП. ДОХОД.  =153.70");
                    printTextInNormalMode("  ПО БСК (СОЦ)");
                    printTextInNormalMode("   КОЛИЧ.      =3");
                    printTextInNormalMode(" --БЕЗДЕНЕЖНЫХ--");
                    printTextInNormalMode("  КОЛИЧ.       =0");
                    printTextInNormalMode("  ВЫП. ДОХОД.  =0.00");
                    printTextInNormalMode("  ПО БСК (СОЦ)");
                    printTextInNormalMode("   КОЛИЧ.      =0");
                    printTextInNormalMode("<--> (туда-обратно)");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("     РАЗОВЫЕ ДЕТСКИЕ");
                    printTextInNormalMode("СУММА          =0.00");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("  СТАТИСТИКА ПО МАРШРУТАМ");
                    printTextInNormalMode("МАРШРУТ 0");
                    printTextInNormalMode("АО ЦЕНТРАЛЬНАЯ ППК");
                    printTextInNormalMode("ПАССАЖИРСКИЙ");
                    printTextInNormalMode("ПЕРЕВОЗЧИК \"0015\"");
                    printTextInNormalMode("ПРОЕЗД \"О\"");
                    printTextInNormalMode("ВЫРУЧКА        =470.30");
                    printTextInNormalMode("КОЛИЧ.         =5");
                    printTextInNormalMode(" --ПОЛНЫХ--");
                    printTextInNormalMode("  СУММА        =173.00");
                    printTextInNormalMode("  КОЛИЧ.       =1");
                    printTextInNormalMode(" --ЛЬГОТНЫХ--");
                    printTextInNormalMode("  СУММА        =297.30");
                    printTextInNormalMode("  КОЛИЧ.       =4");
                    printTextInNormalMode(" --БЕЗДЕНЕЖНЫХ--");
                    printTextInNormalMode("  КОЛИЧ.       =0");
                    printTextInNormalMode("АННУЛИРОВАНО");
                    printTextInNormalMode(" СУММА         =143.50");
                    printTextInNormalMode(" АННУЛ.        =1");
                    printTextInNormalMode("ПО БАНКОВСКИМ КАРТАМ");
                    printTextInNormalMode(" СУММА         =0.00");
                    printTextInNormalMode(" КОЛИЧ.        =0");
                    printTextInNormalMode(" СУММ АННУЛ.   =0.00");
                    printTextInNormalMode(" АННУЛ.        =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("  РАСХОД БИЛЕТНОЙ ЛЕНТЫ");
                    printTextInNormalMode("КОЛИЧ. БОБИН        =0");
                    printTextInNormalMode("КОЛИЧ. ЧЕКОВ        =0");
                    printTextInNormalMode("КОЛИЧ. ОТЧЕТОВ      =0");
                    printTextInNormalMode("РАСХОД Л.(м)   =0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("      ПЕЧАТЬ ЗАКОНЧЕНА      ");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    waitPendingOperations();
                    checkConnectionByManager();
                    getOdometerValue();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getDate();
                    isShiftOpened();
                    isShiftOpened();
                    getINN();
                    getRegNumber();
                    getEKLZNumber();
                    getModel();
                    getAvailableSpaceForDocs();
                    getAvailableSpaceForShifts();
                    checkConnectionByManager();
                    printTextInNormalMode("АО ЦЕНТРАЛЬНАЯ ППК");
                    printTextInNormalMode("УКК КУРСКИЙ УЧАСТОК");
                    printTextInNormalMode("Иванов И. И.           К5664");
                    printTextInNormalMode("ПТК E1444000983 (ID 96)");
                    printTextInNormalMode("ИНН 0123456789");
                    printTextInNormalMode("ЭКЛЗ 0123456789");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ГАШЕНИЕ СМЕННЫХ ИТОГОВ");
                    printTextInNormalMode("        СМЕНА № 133");
                    printTextInNormalMode("МЕС.           =470.30");
                    printTextInNormalMode("СУММА В ФР     =0.00");
                    printTextInNormalMode("СМЕНА          =0.00");
                    printTextInNormalMode("БИЛЕТОВ        =0");
                    printTextInNormalMode("РАСХОД Л.(М.)  =3.37");
                    printTextInNormalMode("ЗАП. ФП        =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("      ПЕЧАТЬ ЗАКОНЧЕНА      ");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    waitPendingOperations();
                    getDate();
                    checkConnectionByManager();
                    isShiftOpened();
                    printZReport();
                    checkConnectionByManager();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case (R.id.print100lines): {

                try {
                    Log.i("MainActivity", "printTextInNormalMode started");
                    for (int i = 1; i <= 100; i++) {
                        printTextInNormalMode("line#" + i);
                    }
                    Log.i("MainActivity", "printTextInNormalMode completed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case (R.id.print100linesAndCheckPaper): {

                try {
                    Log.i("MainActivity", "printTextInNormalMode started");
                    for (int i = 1; i <= 100; i++) {
                        printTextInNormalMode("line#" + i);
                    }
                    Log.i("MainActivity", "printTextInNormalMode completed");
                    waitPendingOperations();
                    Log.i("MainActivity", "waitPendingOperations completed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case (R.id.printCheck): {
                try {
                    for (int i = 1; i <= 5; i++) {
                        startFiscalDocument(DocType.SALE);
                        printTextInFiscalMode("Документ № " + i);
                        printTextInFiscalMode("РАЗОВЫЙ ПОЛНЫЙ");
                        printTextInFiscalMode("A <==> B");
                        // Полная стоимость
                        BigDecimal fullPriceFullValue = new BigDecimal("24.34");
                        printTextInFiscalMode("ПОЛНАЯ СТОИМ." + fullPriceFullValue);
                        // Сбор
                        BigDecimal feeRealValue = new BigDecimal("54.72");
                        BigDecimal feeVatRealValue = new BigDecimal("6.11");
                        printTextInFiscalMode("СБОР" + feeRealValue);
                        printTextInFiscalMode("ВКЛ. НДС" + feeVatRealValue);
                        BigDecimal total = fullPriceFullValue.add(feeRealValue);
                        addItem("#", total, 0);
                        printTotal(total, new BigDecimal("100"), PaymentType.CASH);
                        endFiscalDocument(DocType.SALE);

                        Date date = getLastCheckTime();
                        int SPND = getLastSPND();

                        printBarcode(new byte[100]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case R.id.printTestShiftSheet: {
                try {
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getCashInFR();
                    checkConnectionByManager();
                    getDate();
                    printTextInNormalMode("АО ЦЕНТРАЛЬНАЯ ППК");
                    printTextInNormalMode("ПОДОЛЬСКИЙ УЧАСТОК");
                    printTextInNormalMode("Иванов И. И.           К1234");
                    printTextInNormalMode("ПТК 96 (ID 1007)");
                    printTextInNormalMode("ИНН 000000000000000");
                    printTextInNormalMode("ЭКЛЗ 0000000000");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("      ПРОБНАЯ СМЕННАЯ       ");
                    printTextInNormalMode("       ВЕДОМОСТЬ № 5        ");
                    printTextInNormalMode("         СМЕНА № 22         ");
                    printTextInNormalMode("30.05.2016 12:26:14");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("СУММА ПО ФИСКАЛЬНОМУ");
                    printTextInNormalMode("РЕГИСТРАТОРУ");
                    printTextInNormalMode("=0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("НАЧАЛО СМЕНЫ");
                    printTextInNormalMode(" ПЕРВЫЙ ДОКУМЕНТ");
                    printTextInNormalMode(" 30.05.2016 12:16:51");
                    printTextInNormalMode(" ДОК. № 000038");
                    printTextInNormalMode(" ОПЕРАТОР № 1");
                    printTextInNormalMode(" ПОСЛЕДНИЙ ДОКУМЕНТ");
                    printTextInNormalMode(" 30.05.2016 12:22:43");
                    printTextInNormalMode(" ДОК. № 000039");
                    printTextInNormalMode(" ОПЕРАТОР № 1");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("ВЫРУЧКА");
                    printTextInNormalMode(" МЕС.          =2926.00");
                    printTextInNormalMode(" СУММ АННУЛ.   =1021.50");
                    printTextInNormalMode(" ЗА ВЫЧЕТОМ");
                    printTextInNormalMode(" АННУЛ.        =1904.50");
                    printTextInNormalMode(" ");
                    printTextInNormalMode("ВЫРУЧКА ЗА СМЕНУ");
                    printTextInNormalMode(" ВСЕГО         =0.00");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("КОЛИЧЕСТВО ДОКУМЕНТОВ");
                    printTextInNormalMode("ЗА СМЕНУ");
                    printTextInNormalMode(" ВСЕГО         =2");
                    printTextInNormalMode(" ПРОБН.        =2");
                    printTextInNormalMode(" РАЗОВЫХ       =0");
                    printTextInNormalMode("  ВКЛ.ДОПЛ.7000=0");
                    printTextInNormalMode(" БАГАЖ         =0");
                    printTextInNormalMode(" АННУЛ.        =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("          БАГАЖ");
                    printTextInNormalMode("СУММА          =0.00");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("   ПРОВЕРЕНО ДОКУМЕНТОВ");
                    printTextInNormalMode("БСК АБОНЕМЕНТЫ =0");
                    printTextInNormalMode("БСК РАЗОВЫЕ    =0");
                    printTextInNormalMode("РАЗОВЫЕ БИЛЕТЫ =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("     РАЗОВЫЕ ПОЛНЫЕ");
                    printTextInNormalMode("СУММА          =0.00");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("     РАЗОВЫЕ ДЕТСКИЕ");
                    printTextInNormalMode("СУММА          =0.00");
                    printTextInNormalMode("КОЛИЧ.         =0");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode("      ПЕЧАТЬ ЗАКОНЧЕНА      ");
                    printTextInNormalMode("----------------------------");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    printTextInNormalMode(" ");
                    waitPendingOperations();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case (R.id.disconnectAfterPrintText): {
                try {
                    ExecutorService executorService = Executors.newFixedThreadPool(3);
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            Logger.debug(TAG, "printTextInNormalMode - start");
                            try {
                                printTextInNormalMode("Строка");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "printTextInNormalMode - finish");
                        }
                    });
                    Log.d(TAG, "sleep - start");
                    Thread.sleep(5000);
                    Log.d(TAG, "sleep - finish");
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "disconnectByManager - start");
                            try {
                                disconnectByManager();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "disconnectByManager - finish");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printTextInNormalMode(String text) throws Exception {
        byte err = moebius.sendBlock(concatArrays(text.getBytes("Cp1251"), CARRIAGE_RETURN_WITH_LINE_FEED));
        checkError(err);
    }

    private void waitPendingOperations() throws Exception {
        byte err = moebius.kkmCheckPaper();
        checkError(err);
    }


    protected void printAdjustingTable() throws Exception {
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

//        for (int i =0; i<parts.length; i++) {
//            byte err = -1;
//            byte[] data = concatArrays(parts[i].getBytes(), new byte[]{'\r', '\n'});
//            if (i==parts.length-1)  {
//                err = moebius.sendBlockWithCheckPaper(data);
//            }
//            else {
//                err = moebius.sendBlock(data);
//            }
//            if (err != 0) {
//                throw new Exception(String.valueOf(err));
//            }
//        }

        for (String part : parts) {
            byte err = moebius.sendBlock(concatArrays(part.getBytes(), new byte[]{'\r', '\n'}));
            checkError(err);
        }

        long time = System.currentTimeMillis();
        byte err = moebius.kkmCheckPaper();
        time = System.currentTimeMillis() - time;
        Log.i("666", "Время выполнения команды kkmCheckPaper: " + time + "mc");
        checkError(err);
    }

    //////

    protected boolean checkConnectionByManager() throws Exception {
        kkmGetKKMInfoState((byte) 0x00);
        return true;
    }

    protected void disconnectByManager() throws Exception {
        moebius.disconnect();
    }

    protected void printTextInFiscalMode(String text) throws Exception {
        if (TextUtils.isEmpty(text)) {
            text = " ";
        }
        byte err = moebius.prn_tpm_write(8, CP866.toBytes(text));
        checkError(err);
    }

    protected void openShift(int operatorCode, String operatorName) throws Exception {
        byte err = moebius.kkmOpenShift(new DateTimeFE(), (byte) operatorCode, operatorName);
        checkError(err);
    }

    protected void setCashier(int operatorCode, String operatorName) throws Exception {
        byte err = moebius.kkmCasherChg((byte) operatorCode, operatorName);
        checkError(err);
    }

    protected boolean isShiftOpened() throws Exception {
        KKMInfoStateData kkmInfoStateData = kkmGetKKMInfoState((byte) 0x00);
        return ((kkmInfoStateData.State & 0x01) == 1);
    }

    protected Date getDate() throws Exception {
        KKMInfoStateData kkmInfoStateData = kkmGetKKMInfoState((byte) 0x00);
        Calendar calendar = Calendar.getInstance();
        calendar.set(kkmInfoStateData.Year,
                kkmInfoStateData.Month - 1, //у принтера месяца начинаются с 1, в Calendar с 0
                kkmInfoStateData.Day,
                kkmInfoStateData.Hour,
                kkmInfoStateData.Min,
                kkmInfoStateData.Sec);

        return calendar.getTime();
    }

    protected int getLastSPND() throws Exception {
        return kkmGetKKMInfoState((byte) 0x00).DocNum;
    }

    protected Date getLastCheckTime() throws Exception {

        String receiptDateStr = kkmGetKKMInfoReg().getReceiptDate();
        return formatterForEklzTime.parse(receiptDateStr);
    }

    protected int getShiftNum() throws Exception {
        return (int) kkmGetKKMInfoState((byte) 0x00).NumShift;
    }

    protected void printZReport() throws Exception {
        byte err = moebius.kkmCloseShift(new DateTimeFE(), (byte) 0xC0);
        checkError(err);
    }

    protected void setHeaderLines(List<String> headerLines) throws Exception {
        String fullHeader = "";
        for (int i = 0; i < headerLines.size(); i++) {
            fullHeader += headerLines.get(i);
            if (i < headerLines.size() - 1)
                fullHeader += "\r\n";
        }
        moebius.kkmChangeHeader(fullHeader);
    }

    protected void setVatValue(int vatID, int vatValue) throws Exception {
        moebius.setVatValue(vatID, vatValue);
    }

    protected int getVatValue(int vatID) throws Exception {
        return 0;
    }

    protected void printBarcode(byte[] data) throws Exception {

        if (data == null)
            throw new NullPointerException("barcodeData is null");

        /*
            за высоту штрих-кода отвечает 3й параметр в первой строке
            если нужно увеличить отступт штрихкода до конца чека, то увеличиваем его
         */

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        String barcodeHeight;

        if (data.length == 82) {
            // разовый
            barcodeHeight = "119";
        } else if (data.length == 91) {
            // доплата
            barcodeHeight = "125";
        } else {
            // стандарт
            barcodeHeight = "130";
        }

        String firstBarcodeParams = "! 300 200 200 " + barcodeHeight + " 1\n";

        // первое число (300) отвечает за горизонтальное смещение
        os.write(firstBarcodeParams.getBytes("UTF-8"));
        os.write("B PDF-417 10 1 XD 2 YD 4 C 3 S 2\r\n".getBytes("UTF-8"));
        os.write(data);
        os.write("\r\n".getBytes("UTF-8"));
        os.write("ENDPDF\r\n".getBytes("UTF-8"));
        os.write("FORM\r\n".getBytes("UTF-8"));
        os.write("PRINT\r\n".getBytes("UTF-8"));

        byte[] command = os.toByteArray();

        if (command.length > 256)
            throw new IllegalArgumentException("printBarcode ERROR:  Length command for print barcode should be less 256 symbols");

        //До вызова cpcl команды обязательно проверить статус бумаги.
        byte err = moebius.kkmCheckPaper();
        checkError(err);

        err = moebius.sendBlockWithCheckPaper(command);
        checkError(err);
    }

    protected void startFiscalDocument(DocType docType) throws Exception {
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
        byte err = moebius.mbs_head(oprCodeEklz, (byte) 1, "0");
        checkError(err);
    }

    protected void endFiscalDocument(DocType docType) throws Exception {

    }

    protected void addItem(String description, BigDecimal amount, int vatIndex) throws Exception {
        byte err = moebius.mbs_RecItem(description,
                description,
                "1",
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                1,
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                vatIndex,
                false,
                "");
        checkError(err);
    }

    protected void addItemRefund(String description, BigDecimal amount, int vatIndex) throws Exception {
        byte err = moebius.mbs_RecItem(description,
                description,
                "1",
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                1,
                amount.multiply(Decimals.HUNDRED).intValue(), // переводим рубли в копейки
                vatIndex,
                false,
                "");
        checkError(err);
    }

    protected void printItemVAT(BigDecimal vatValue, BigDecimal vatRate) throws Exception {
        byte err = moebius.prn_tpm_write(8, CP866.toBytes("ВКЛ. НДС" + "=" + vatValue));
        checkError(err);
    }

    protected void printTotal(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws Exception {
        int totalInt = total.multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки
        int chgInt = payment.subtract(total).multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки
        int paymentInt = payment.multiply(Decimals.HUNDRED).intValue(); // переводим рубли в копейки

        int cashInt = paymentType == PaymentType.CASH ? paymentInt : 0;
        cardPay[0].Sum = paymentType == PaymentType.CARD ? paymentInt : 0;

        byte err = moebius.mbs_tot(totalInt, chgInt, cashInt, cardPay, creditPay, "xxx", 1, 1);
        checkError(err);
    }

    public long getOdometerValue() throws Exception {
        //Thread.sleep(10000);
        ResultAsSingleString resultAsSingleString = new ResultAsSingleString();
        byte err = moebius.kkmGetOdometerValue(resultAsSingleString);
        checkError(err);
        return Long.valueOf(resultAsSingleString.getString().replaceAll("[^\\d]", ""));
    }

    protected String getINN() throws Exception {
        return kkmGetKKMInfoReg().getINN();
    }

    protected String getRegNumber() throws Exception {
        return kkmGetKKMInfoReg().getRNKKM();
    }

    protected String getEKLZNumber() throws Exception {

        ResultAsSingleString out = new ResultAsSingleString();
        byte err = moebius.kkmGetEKLZNumber(out);
        checkError(err);
        return out.getString();
    }

    protected String getModel() throws Exception {
        return "Zebra";
    }

    protected double getCashInFR() throws Exception {
        double sumInCent = kkmGetKKMInfoEx().curCash;//сумма в копейках
        return sumInCent;
    }

    protected long getAvailableSpaceForDocs() throws Exception {
        //принтер пока не возвращает такие данные, поэтому вернем 2
        return 2;
    }

    protected long getAvailableSpaceForShifts() throws Exception {
        return kkmDopInfo().CloseShiftTail;
    }

    private ResultAsString kkmGetKKMInfoReg() throws Exception {
        ResultAsString out = new ResultAsString();
        byte err = moebius.kkmGetKKMInfoReg(out);
        checkError(err);
        return out;
    }

    /**
     * @param oprCode служебный параметр, если равен 0x80, то возвращается промежуток времени после
     *                первой фискальной операции в смене (длина смены), если 0,
     *                то возвращается текущее время в ФР.
     * @return
     * @throws Exception
     */
    private KKMInfoStateData kkmGetKKMInfoState(byte oprCode) throws Exception {
        KKMInfoStateData out = new KKMInfoStateData();
        byte err = moebius.kkmGetKKMInfoState(oprCode, out);
        checkError(err);
        return out;
    }

    /**
     * Получает от принтера информацию по фискальным данным
     *
     * @return структура с информацией о фискальных данных
     * @throws Exception
     */
    private KKMTotSendHost kkmGetKKMInfoEx() throws Exception {
        KKMTotSendHost out = new KKMTotSendHost();
        byte err = moebius.kkmGetKKMInfoEx(out);
        checkError(err);
        return out;
    }

    private KKMDopInfo kkmDopInfo() throws Exception {
        KKMDopInfo out = new KKMDopInfo();
        byte err = moebius.kkmGetKKMDopInfo(out);
        checkError(err);
        return out;
    }

    private void checkError(byte err) throws Exception {
        if (err != 0) {
            throw new Exception(String.valueOf(err));
        }
    }

    public static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
