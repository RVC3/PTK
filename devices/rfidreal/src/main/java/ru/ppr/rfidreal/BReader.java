package ru.ppr.rfidreal;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import fr.coppernic.cpcframework.cpcask.Defines;
import fr.coppernic.cpcframework.cpcask.sCARD_SearchExt;
import fr.coppernic.cpcframework.cpcask.sCARD_SecurParam;
import fr.coppernic.cpcframework.cpcask.sCARD_Session;
import fr.coppernic.cpcframework.cpcask.sCARD_Status;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.helpers.CpcUsb;
import fr.coppernic.tools.cpcdirectserialcommunication.SerialFactory;
import fr.coppernic.tools.cpcftdiserialcommunication.FtdiSerialCommunication;
import fr.coppernic.tools.serialcommunication.OnInstanceCreatedListener;
import fr.coppernic.tools.serialcommunication.SerialCommunication;

public class BReader {
    private static Context sContext = null;
    private static final String TAG = "CpcAsk";
    private static final int RECEIVE_TIMEOUT = 100;
    private int mFuncTimeout;
    private int mSearchTimeout;
    private boolean mIsOpened;
    private boolean mCrcNeeded;
    private static final int MAX_FRAME_SIZE = 300;
    private byte[] mBufOut;
    private int mLnOut;
    private static SerialCommunication mSerialCommunication = null;
    private PendingIntent mPermissionIntent;
    static Iterator<UsbDevice> sDeviceIterator = null;
    static UsbManager sManager = null;
    static UsbDevice sDeviceUsb = null;
    private static boolean mAuthorized = false;
    private static boolean DEBUG = true;
    private boolean dumpData;
    private int mBaudrate;
    private final BroadcastReceiver mUsbReceiver;

    public static boolean isUsb(Context ctx) {
        sManager = (UsbManager)ctx.getSystemService("usb");
        HashMap<String, UsbDevice> deviceListUsb = sManager.getDeviceList();
        sDeviceIterator = deviceListUsb.values().iterator();

        UsbDevice device;
        do {
            if (!sDeviceIterator.hasNext()) {
                return false;
            }

            device = (UsbDevice)sDeviceIterator.next();
        } while(device.getProductId() != 24577 && device.getProductId() != 24593);

        return true;
    }

    private BReader(Context ctx) {
        this.mFuncTimeout = 5000;
        this.mSearchTimeout = 3000;
        this.mIsOpened = false;
        this.mCrcNeeded = false;
        this.mBufOut = new byte[300];
        this.mLnOut = 0;
        this.mPermissionIntent = null;
        this.dumpData = false;
        this.mBaudrate = 0;
        this.mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("fr.coppernic.permission.usb".equals(action)) {
                    synchronized(this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                        Log.d("CpcAsk", "BroadcastReceiver : START");
                        if (intent.getBooleanExtra("permission", false)) {
                            if (device != null) {
                                BReader.mAuthorized = true;
                                BReader.this.unregister();
                            }
                        } else {
                            Log.d("CpcAsk", "permission denied for device " + device);
                        }
                    }
                }

            }
        };
        sContext = ctx;
        this.register();
    }

    public static void getInstance(final Context ctx, final IOnGetReaderInstanceListener listener) {
        if (isUsb(ctx)) {
            listener.OnGetReaderInstance(new BReader(ctx));
        } else {
            SerialFactory.getInstance(ctx, new OnInstanceCreatedListener() {
                public void OnInstanceCreated(SerialCommunication serialInstance) {
                    BReader.mSerialCommunication = serialInstance;
                    listener.OnGetReaderInstance(new BReader(ctx));
                }
            });
        }

    }

    public static void getInstance(Context ctx, IOnGetReaderInstanceListener listener, SerialCommunication serialCommunication, boolean isUsb) {
        mSerialCommunication = serialCommunication;
        if (isUsb) {
            mAuthorized = true;
        }

        listener.OnGetReaderInstance(new BReader(ctx));
    }

    public void destroy() {
        this.unregister();
        this.cscClose();
    }

    public void register() {
        CpcUsb.registerUsbReceiver(sContext, this.mUsbReceiver);
    }

    public void unregister() {
        try {
            sContext.unregisterReceiver(this.mUsbReceiver);
        } catch (Exception var2) {
            Log.e("CpcAsk", var2.toString());
        }

    }

    private static void zeroMemory(byte[] buf, int length) {
        Arrays.fill(buf, (byte)0);
    }

    @SuppressLint("WrongConstant")
    public void getUsbAuthorization() {
        sManager = (UsbManager)sContext.getSystemService("usb");
        HashMap<String, UsbDevice> deviceListUsb = sManager.getDeviceList();

        for(sDeviceIterator = deviceListUsb.values().iterator(); sDeviceIterator.hasNext(); Log.d("CpcAsk", "onCreate : permission requested")) {
            UsbDevice device = (UsbDevice)sDeviceIterator.next();
            if (device.getProductId() == 24577 || device.getProductId() == 24593) {
                sDeviceUsb = device;
                if (this.mPermissionIntent == null) {
                    this.mPermissionIntent = PendingIntent.getBroadcast(sContext, 0, new Intent("fr.coppernic.permission.usb"), 0);
                }

                sManager.requestPermission(sDeviceUsb, this.mPermissionIntent);
            }
        }

    }

    public void setDumpData(boolean dumpData) {
        this.dumpData = dumpData;
    }

    public byte[] getBufOut() {
        return this.mBufOut;
    }

    public int getLnOut() {
        return this.mLnOut;
    }

    public boolean isOpened() {
        return this.mIsOpened;
    }

    public int cscOpen(String name, int baudrate, boolean usb) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscOpen(...) : START");
        }

        if (usb) {
            if (mSerialCommunication == null) {
                mSerialCommunication = new FtdiSerialCommunication(sContext);
                mSerialCommunication.listDevices();
            }
        } else if (mSerialCommunication == null) {
            return 32770;
        }

        if (mAuthorized && usb) {
            this.mIsOpened = mSerialCommunication.openCom(name, baudrate) == 0;
            if (!this.mIsOpened) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscOpen(...) : END ERROR");
                }

                return 32770;
            }

            mSerialCommunication.setRts(true);
        } else {
            if (usb) {
                mSerialCommunication = null;
                if (DEBUG) {
                    Log.d("CpcAsk", "cscOpen(...) : END ERROR");
                }

                return 32787;
            }

            this.mIsOpened = mSerialCommunication.openCom(name, baudrate) == 0;
            if (!this.mIsOpened) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscOpen(...) : END ERROR");
                }

                return 32770;
            }

            mSerialCommunication.setRts(true);
        }

        mSerialCommunication.flush();
        if (DEBUG) {
            Log.d("CpcAsk", "cscOpen(...) : END OK");
        }

        return 32769;
    }

    public int cscOpen(String name, boolean usb) {
        if (usb) {
            if (mSerialCommunication == null) {
                mSerialCommunication = new FtdiSerialCommunication(sContext);
                mSerialCommunication.listDevices();
            }
        } else if (mSerialCommunication == null) {
        }

        if ((!mAuthorized || !usb) && usb) {
            if (usb) {
                mSerialCommunication = null;
                return 32787;
            }
        } else {
            SystemClock.sleep(750L);
            if (this.mBaudrate == 0) {
                int[] baudrates = new int[]{115200, 460800, 9600, 19200, 38400, 57600, 230400};

                for(int i = 0; i < baudrates.length; ++i) {
                    Log.d("CpcAsk", "cscOpen: Trying @ " + Integer.toString(baudrates[i]));
                    this.mIsOpened = mSerialCommunication.openCom(name, baudrates[i]) == 0;
                    if (this.mIsOpened) {
                        mSerialCommunication.setRts(true);
                        SystemClock.sleep(500L);
                    }

                    StringBuilder sbVersion = new StringBuilder();
                    int res = this.cscVersionCsc(sbVersion);
                    if (res == 32769) {
                        this.mBaudrate = baudrates[i];
                        break;
                    }

                    mSerialCommunication.closeCom();
                    this.mIsOpened = false;
                }
            } else {
                this.mIsOpened = mSerialCommunication.openCom(name, this.mBaudrate) == 0;
                if (this.mIsOpened) {
                    mSerialCommunication.setRts(true);
                }
            }

            if (!this.mIsOpened) {
                return 32770;
            }
        }

        mSerialCommunication.flush();
        return 32769;
    }

    public int getBaudrate() {
        return this.mBaudrate;
    }

    public void cscClose() {
        if (this.mIsOpened) {
            mSerialCommunication.setRts(false);
            mSerialCommunication.closeCom();
            this.mIsOpened = false;
        }

    }

    private int receiveCom(int timeout) {
        long start = SystemClock.uptimeMillis();
        long now = SystemClock.uptimeMillis();
        int nbBytesAvailable = 0;
        this.mLnOut = 0;
        Arrays.fill(this.mBufOut, (byte)0);
        int nbBytesRead = 0;

        byte[] temp;
        for(temp = null; nbBytesRead <= 0 && now - start < (long)timeout; now = SystemClock.uptimeMillis()) {
            nbBytesAvailable = mSerialCommunication.getQueueStatus();
            if (nbBytesAvailable > 0) {
                temp = new byte[nbBytesAvailable];
                nbBytesRead = mSerialCommunication.receiveCom(100, nbBytesAvailable, temp);
            }
        }

        if (now - start >= (long)timeout) {
            return -2;
        } else {
            if (DEBUG) {
                Log.i("CpcAsk", "1 - nbBytesRead = " + Integer.toString(nbBytesRead));
            }

            if (nbBytesRead > 0 && nbBytesRead < 300) {
                System.arraycopy(temp, 0, this.mBufOut, 0, nbBytesRead);
            }

            this.mLnOut = nbBytesRead;

            for(int frameLength = (this.mBufOut[1] & 255) + 5; now - start < (long)timeout && this.mLnOut < frameLength; now = SystemClock.uptimeMillis()) {
                nbBytesAvailable = mSerialCommunication.getQueueStatus();
                if (nbBytesAvailable > 0) {
                    Log.d("CpcAsk", "nbBytesAvailable = " + nbBytesAvailable);
                    temp = new byte[nbBytesAvailable];
                    nbBytesRead = mSerialCommunication.receiveCom(250, nbBytesAvailable, temp);
                    Log.d("CpcAsk", "nbBytesRead = " + nbBytesRead);
                    if (nbBytesRead < 0) {
                        return -3;
                    }

                    if (nbBytesRead + this.mLnOut > 300) {
                        return -4;
                    }

                    System.arraycopy(temp, 0, this.mBufOut, this.mLnOut, nbBytesRead);
                    this.mLnOut += nbBytesRead;
                }
            }

            return now - start >= (long)timeout ? -2 : this.mLnOut;
        }
    }

    public int cscSendReceive(int timeout, byte[] bufIn, int lnIn) {
        int vRet = 0;
        if (!this.mIsOpened) {
            return 32770;
        } else if (bufIn == null) {
            return 32773;
        } else if (this.mBufOut == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            if (DEBUG || this.dumpData) {
                Log.d("CpcAsk", ">> " + CpcBytes.byteArrayToString(bufIn, lnIn));
            }

            if (mSerialCommunication.sendCom(bufIn, lnIn) == 0) {
                return 32782;
            } else {
                if (bufIn[2] == 1 && bufIn[3] == 1 && bufIn[4] == -1) {
                    this.mCrcNeeded = false;
                }

                if (bufIn[2] == 1 && bufIn[3] == 1 && bufIn[4] == 0) {
                    this.mCrcNeeded = true;
                }

                vRet = this.receiveCom(timeout);
                if (vRet == -2) {
                    return 32776;
                } else if (vRet == -3) {
                    return 32788;
                } else if (vRet == -4) {
                    return 32789;
                } else {
                    if (this.mLnOut != 1) {
                        if (bufIn[1] != -1) {
                            if (this.mBufOut[1] != 255) {
                                if (this.mBufOut[2] != bufIn[2] || this.mBufOut[3] != bufIn[3]) {
                                    Log.d("CpcAsk", "Error Type 1");
                                    return 32777;
                                }
                            } else if (this.mBufOut[3] != bufIn[2] || this.mBufOut[4] != bufIn[3]) {
                                Log.d("CpcAsk", "Error Type 2");
                                return 32777;
                            }
                        } else if (this.mBufOut[1] != 255) {
                            if (this.mBufOut[2] != bufIn[3] || this.mBufOut[3] != bufIn[4]) {
                                Log.d("CpcAsk", "Error Type 3");
                                return 32777;
                            }
                        } else if (this.mBufOut[3] != bufIn[3] || this.mBufOut[4] != bufIn[4]) {
                            Log.d("CpcAsk", "Error Type 4");
                            return 32777;
                        }
                    }

                    BCommands.giCSCTrameLn = this.mLnOut;
                    System.arraycopy(this.mBufOut, 0, BCommands.giCSCTrame, 0, this.mLnOut);
                    if (DEBUG || this.dumpData) {
                        Log.e("CpcAsk", "<< " + CpcBytes.byteArrayToString(this.mBufOut, this.mLnOut));
                    }

                    if (this.mCrcNeeded && !BCommands.iCSC_TestCRC()) {
                        return 32772;
                    } else if (this.mBufOut[0] == -128) {
                        return 32785;
                    } else {
                        return 32769;
                    }
                }
            }
        }
    }

    public int cscVersionCsc(StringBuilder sb) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscVersionCsc(...) : START");
        }

        BCommands.giCSCTrame[0] = -128;
        BCommands.giCSCTrame[1] = 2;
        BCommands.giCSCTrame[2] = 1;
        BCommands.giCSCTrame[3] = 1;
        BCommands.giCSCTrame[4] = 0;
        BCommands.giCSCTrameLn = 5;
        BCommands.icsc_SetCRC();
        int ret = this.cscSendReceive(3000, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
        if (ret != 32769) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscVersionCsc(...) : END ERROR");
            }

            return ret;
        } else if (this.mLnOut > 6) {
            String szVersion = new String(this.mBufOut, 4, this.mLnOut - 6);
            sb.append(szVersion);
            if (DEBUG) {
                Log.d("CpcAsk", "cscVersionCsc(...) : END OK");
            }

            return 32769;
        } else {
            if (DEBUG) {
                Log.d("CpcAsk", "cscVersionCsc(...) : END ERROR");
            }

            return 32773;
        }
    }

    public int cscResetCsc() {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            BCommands.giCSCTrame[0] = 1;
            BCommands.giCSCTrameLn = 1;
            mSerialCommunication.flush();
            int vRet = mSerialCommunication.sendCom(BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet == 1) {
                vRet = mSerialCommunication.receiveCom(2000, 1, this.mBufOut);
                if (vRet == 1 && this.mBufOut[0] == 16) {
                    Log.d("CpcAsk", "Reset OK");
                    return 32769;
                }
            }

            return 32773;
        }
    }

    public int cscEnterHuntPhaseParameters(byte maxNbCards, byte req, byte nbSlots, byte afi, byte autoSelDiv, byte deselect, byte selectAppli, byte dataLength, byte[] data, short felicaAfi, byte felicaNbSlots) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscEnterHuntPhaseParameters(...) : START");
        }

        int ret = 0;
        if (req != 0 && req != 1) {
            return 32777;
        } else if (deselect != 0 && deselect != 1) {
            return 32777;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_EnterHuntPhaseParameters(maxNbCards, req, nbSlots, afi, autoSelDiv, deselect, selectAppli, dataLength, data, felicaAfi, felicaNbSlots);
            ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscEnterHuntPhaseParameters(...) : END");
                }

                return 32769;
            }
        }
    }

    public int cscSearchCardExt(sCARD_SearchExt search, int searchMask, byte forget, byte timeOut, byte[] com, int[] lpcbATR, byte[] lpATR) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscSearchCardExt(...) : START");
        }
        int vRet = 0;
        zeroMemory(this.mBufOut, this.mBufOut.length);
        byte searchCont;
        if ((searchMask & 1) == 1) {
            searchCont = search.CONT;
        } else {
            searchCont = 0;
        }

        byte searchOth;
        if ((searchMask & 512) == 512) {
            searchOth = search.OTH;
            if (searchOth > 3) {
                searchOth = 3;
            }
        } else {
            searchOth = 0;
        }

        byte searchIsoB;
        if ((searchMask & 2) == 2) {
            searchIsoB = search.ISOB;
            if (searchIsoB > 3) {
                searchIsoB = 3;
            }
        } else {
            searchIsoB = 0;
        }

        byte searchIsoA;
        if ((searchMask & 4) == 4) {
            searchIsoA = search.ISOA;
            if (searchIsoA > 3) {
                searchIsoA = 3;
            }
        } else {
            searchIsoA = 0;
        }

        byte searchTick;
        if ((searchMask & 8) == 8) {
            searchTick = search.TICK;
            if (searchTick > 3) {
                searchTick = 3;
            }
        } else {
            searchTick = 0;
        }

        byte searchInno;
        if ((searchMask & 16) == 16) {
            searchInno = search.INNO;
            if (searchInno > 3) {
                searchInno = 3;
            }
        } else {
            searchInno = 0;
        }

        byte searchMifare;
        if ((searchMask & 32) == 32) {
            searchMifare = search.MIFARE;
            if (searchMifare > 3) {
                searchMifare = 3;
            }
        } else {
            searchMifare = 0;
        }

        byte searchMv4k;
        if ((searchMask & 64) == 64) {
            searchMv4k = search.MV4k;
            if (searchMv4k > 3) {
                searchMv4k = 3;
            }
        } else {
            searchMv4k = 0;
        }

        byte searchMv5k;
        if ((searchMask & 128) == 128) {
            searchMv5k = search.MV5k;
            if (searchMv5k > 3) {
                searchMv5k = 3;
            }
        } else {
            searchMv5k = 0;
        }

        byte searchMono;
        if ((searchMask & 256) == 256) {
            searchMono = search.MONO;
            if (searchMono > 1) {
                searchMono = 1;
            }
        } else {
            searchMono = 0;
        }

        mSerialCommunication.flush();
        BCommands.iCSC_EnterHuntPhase4((byte)0, searchMono, searchOth, searchCont, searchIsoA, searchMifare, searchIsoB, searchTick, searchInno, searchMv4k, searchMv5k, forget, timeOut);
        vRet = this.cscSendReceive(this.mSearchTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
        if (this.mBufOut[1] < 4) {
            return 32777;
        } else {
            zeroMemory(lpATR, lpATR.length);
            com[0] = this.mBufOut[5];
            if (DEBUG) {
                Log.i("CpcAskAndroid", "mLnOut : " + Integer.toString(this.mLnOut));
                Log.i("CpcAskAndroid", "mBufOut[0] : " + Integer.toString(this.mBufOut[0]));
                Log.i("CpcAskAndroid", "mBufOut[1] : " + Integer.toString(this.mBufOut[1]));
                Log.i("CpcAskAndroid", "mBufOut[2] : " + Integer.toString(this.mBufOut[2]));
                Log.i("CpcAskAndroid", "mBufOut[3] : " + Integer.toString(this.mBufOut[3]));
                Log.i("CpcAskAndroid", "COM : " + Integer.toString(this.mBufOut[5]));
                Log.i("CpcAskAndroid", "ATR len : " + Integer.toString(this.mBufOut[6]));
            }

            if (com[0] != 111) {
                int i;
                if (com[0] == 3) {
                    lpcbATR[0] = (this.mBufOut[6] & 255) - 6;
                    if (lpcbATR[0] >= 32) {
                        return 0;
                    }

                    for(i = 0; i < lpcbATR[0]; ++i) {
                        lpATR[i] = this.mBufOut[13 + i];
                    }
                } else {
                    lpcbATR[0] = this.mBufOut[6] & 255;
                    if (lpcbATR[0] >= 32) {
                        return 0;
                    }

                    for(i = 0; i < lpcbATR[0]; ++i) {
                        lpATR[i] = this.mBufOut[7 + i];
                    }
                }
            } else {
                lpcbATR[0] = 0;
            }

            if (DEBUG) {
                Log.d("CpcAsk", "cscSearchCardExt(...) : END OK");
            }

            return vRet;
        }
    }

    public int cscAntennaOff() {
        byte[] mBufOut = new byte[255];
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_SwitchOffAntenna((byte)0);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else {
                return mBufOut[4] != 0 ? '者' : '老';
            }
        }
    }

    public int cscTransparentCommandConfig(byte iso, byte addCRC, byte checkCRC, byte field, byte[] configISO, byte[] configAddCRC, byte[] configCheckCRC, byte[] configField) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_TransparentCommandConfig(iso, addCRC, checkCRC, field);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else {
                configISO[0] = this.mBufOut[4];
                configAddCRC[0] = this.mBufOut[5];
                configCheckCRC[0] = this.mBufOut[6];
                configField[0] = this.mBufOut[7];
                return this.mBufOut[1] != 6 ? '耉' : '老';
            }
        }
    }

    public int cscTransparentCommand(byte[] bufIn, int lnIn, byte[] status, int[] lnOut, byte[] bufOut) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            if (lnIn <= 0) {
                return 32773;
            } else if (lnIn >= 256) {
                return 32773;
            } else if (bufIn == null) {
                return 32773;
            } else {
                BCommands.iCSC_TransparentCommand((byte)lnIn, bufIn);
                int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
                if (vRet != 32769) {
                    return vRet;
                } else {
                    if (lnOut != null && bufOut != null) {
                        status[0] = this.mBufOut[4];
                        lnOut[0] = this.mBufOut[5] & 255;

                        for(int i = 0; i < lnOut[0]; ++i) {
                            bufOut[i] = this.mBufOut[i + 6];
                        }
                    }

                    return 32769;
                }
            }
        }
    }

    public int cscISOCommand(byte[] bufIn, int lnIn, byte[] bufOut, int[] lnOut) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            if (lnIn <= 0) {
                return 32773;
            } else if (lnIn >= 256) {
                return 32773;
            } else if (bufIn == null) {
                return 32773;
            } else {
                BCommands.iCSC_SendToAntenna(bufIn, (byte)lnIn);
                int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
                if (vRet != 32769) {
                    return vRet;
                } else {
                    switch(this.mBufOut[4]) {
                        case -5:
                            return 32772;
                        case -4:
                        case 0:
                            return 32776;
                        case -3:
                            return 32778;
                        case -2:
                        case -1:
                            return 32777;
                        default:
                            if (this.mBufOut[4] != 1) {
                                return 32773;
                            } else {
                                if (lnOut != null && bufOut != null) {
                                    lnOut[0] = this.mBufOut[5] & 255;
                                    if (bufOut != null) {
                                        for(int i = 0; i < lnOut[0]; ++i) {
                                            bufOut[i] = this.mBufOut[i + 6];
                                        }
                                    }
                                }

                                return 32769;
                            }
                    }
                }
            }
        }
    }

    public int cscISOCommandContact(byte[] bufIb, int lnIn, byte apduCase, byte[] bufOut, int[] lnOut) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            if (lnIn <= 0) {
                return 32773;
            } else if (lnIn >= 256) {
                return 32773;
            } else if (bufIb == null) {
                return 32773;
            } else {
                BCommands.iCSC_ISOCommandContact(bufIb, (byte)lnIn, apduCase);
                int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
                if (vRet != 32769) {
                    return vRet;
                } else if (this.mBufOut[4] != 0) {
                    return 32773;
                } else {
                    lnOut[0] = (this.mBufOut[5] & 255) - 1;
                    if (bufOut != null) {
                        for(int i = 0; i < (lnOut[0] & 255); ++i) {
                            bufOut[i] = this.mBufOut[i + 6];
                        }
                    }

                    return 32769;
                }
            }
        }
    }

    public int cscSelectSam(byte nSam, byte type) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscSelectSam(...) : START");
        }

        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscWriteIoExt : END ERROR");
            }

            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_SelectSAM(nSam, type);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscWriteIoExt : END ERROR");
                }

                return vRet;
            } else if (this.mBufOut[1] < 3) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscWriteIoExt : END ERROR");
                }

                return 32777;
            } else if (this.mBufOut[4] != 0) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscWriteIoExt : END ERROR");
                }

                return 32784;
            } else {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscWriteIoExt : END OK");
                }

                return 32769;
            }
        }
    }

    public int cscResetSam(byte nSam, byte[] lpAtr, int[] lpcbAtr) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscResetSam...");
        }

        if (!this.mIsOpened) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscResetSam(...) : END ERROR");
            }

            return 32770;
        } else if (lpAtr == null) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscResetSam(...) : END ERROR");
            }

            return 32773;
        } else if (lpcbAtr == null) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscResetSam(...) : END ERROR");
            }

            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_ResetSAM(nSam);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscResetSam(...) : END ERROR");
                }

                return vRet;
            } else if ((this.mBufOut[4] & 124) != 0) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscResetSam(...) : END ERROR");
                }

                return 32779;
            } else {
                if (this.mBufOut[4] != 255) {
                    lpcbAtr[0] = this.mBufOut[5] & 255;

                    for(int i = 0; i < lpcbAtr[0]; ++i) {
                        lpAtr[i] = this.mBufOut[6 + i];
                    }
                } else {
                    lpcbAtr[0] = 0;
                }

                if (DEBUG) {
                    Log.d("CpcAsk", "cscResetSam(...) : END OK");
                }

                return 32769;
            }
        }
    }

    public int cscIsoBCommandsam(byte[] bufIn, int lnIn, byte[] bufOut, int[] lnOut) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscIsoBCommandsam...");
        }

        if (!this.mIsOpened) {
            return 32770;
        } else if (bufIn == null) {
            return 32773;
        } else if (lnIn == 0) {
            return 32773;
        } else if (lnIn > 255) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_SendISO7816ToSAM(bufIn, (byte)lnIn);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] != 0) {
                return 32779;
            } else {
                if (lnOut != null && this.mBufOut != null) {
                    lnOut[0] = (this.mBufOut[5] & 255) - 1;

                    for(int i = 0; i < lnOut[0]; ++i) {
                        bufOut[i] = this.mBufOut[6 + i];
                    }
                }

                return 32769;
            }
        }
    }

    public int cscSelectCid(byte cid, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (cid != 0 && cid <= 15) {
            mSerialCommunication.flush();
            BCommands.iCSC_SelectCID(cid);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[4];
                return 32769;
            }
        } else {
            return 32786;
        }
    }


    public int cscSendCom(byte[] bufIn, int lnIn) {
        return mSerialCommunication.sendCom(bufIn, lnIn);
    }



    public int cscSetSamBaudratePps(byte proProt, byte paramFd, byte[] status) {
        if (DEBUG) {
            Log.d("CpcAsk", "cscSetSamBaudratePps(...) : START");
        }

        if (!this.mIsOpened) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscSetSamBaudratePps(...) : END ERROR");
            }

            return 32770;
        } else if (proProt != 0 && proProt != 1) {
            if (DEBUG) {
                Log.d("CpcAsk", "cscSetSamBaudratePps(...) : END ERROR");
            }

            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_SamPps(proProt, paramFd);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                if (DEBUG) {
                    Log.d("CpcAsk", "cscSetSamBaudratePps(...) : END ERROR");
                }

                return ret;
            } else {
                status[0] = this.mBufOut[4];
                status[1] = this.mBufOut[5];
                if (DEBUG) {
                    Log.d("CpcAsk", "cscSetSamBaudratePps(...) : END OK");
                }

                return 32769;
            }
        }
    }


    public int cscConfigIoExt(byte inputMask, byte enablePullUp, byte enableFilter, byte outputMask, byte outputDefaultValue, byte outputEnableOpenDrain, byte outputEnablePullUp) {
        Log.d("CpcAsk", "cscConfigIoExt(...) : START");
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_ConfigIoExt(inputMask, enablePullUp, enableFilter, outputMask, outputDefaultValue, outputEnableOpenDrain, outputEnablePullUp);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                Log.d("CpcAsk", "cscConfigIoExt : END ERROR : " + Defines.errorLookUp(vRet));
                return vRet;
            } else {
                Log.d("CpcAsk", "cscConfigIoExt : END OK");
                return 32769;
            }
        }
    }

    public int cscWriteIoExt(byte ioToWrite, byte value) {
        Log.d("CpcAsk", "cscWriteIoExt(...) : START");
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCSC_WriteIoExt(ioToWrite, value);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                Log.d("CpcAsk", "cscWriteIoExt : END ERROR : " + Defines.errorLookUp(vRet));
                return vRet;
            } else {
                Log.d("CpcAsk", "cscWriteIoExt : END OK");
                return 32769;
            }
        }
    }

    public int mifareAuthenticate(byte numSector, byte keyAorB, byte keyIndex, byte[] mifareType, byte[] serialNumber, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (keyAorB >= 10 && keyAorB <= 11) {
            if (keyIndex > 31 && keyIndex != 255) {
                return 32786;
            } else if (numSector > 39) {
                return 32786;
            } else {
                mSerialCommunication.flush();
                BCommands.iMIFARE_Authenticate(numSector, keyAorB, keyIndex);
                int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
                if (vRet != 32769) {
                    return vRet;
                } else if (this.mBufOut[1] < 3) {
                    return 32777;
                } else {
                    status[0] = this.mBufOut[5];
                    mifareType[0] = this.mBufOut[6];

                    for(byte i = 0; i < 4; ++i) {
                        serialNumber[i] = this.mBufOut[i + 7];
                    }

                    return 32769;
                }
            }
        } else {
            return 32786;
        }
    }

    public int mifareLoadReaderKeyIndex(byte KeyIndex, byte[] KeyVal, byte[] Status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (KeyIndex > 31 && KeyIndex != 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_LoadReaderKeyIndex(KeyIndex, KeyVal);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                Status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }


    public int mifareReadBlock(byte numBlock, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_ReadBlock(numBlock);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];

                for(byte i = 0; i < 16; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }


    public int mifareWriteBlock(byte numBlock, byte[] dataToWrite, byte[] dataVerif, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_WriteBlock(numBlock, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];

                for(byte i = 0; i < 16; ++i) {
                    dataVerif[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }


    public int mifareUlIdentifyType(byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_Identify_Type();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 1) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    private Boolean mifareUlVerifyInputData(byte mfulType, int add, int nb) {
        if (mfulType != 32 && mfulType != 33 && mfulType != 34 && mfulType != 35) {
            return false;
        } else if (add % 4 != 0) {
            return false;
        } else {
            switch(mfulType) {
                case 32:
                    if (add <= 60 && add + nb <= 64) {
                        break;
                    }

                    return false;
                case 33:
                    if (add <= 172 && add + nb <= 176) {
                        break;
                    }

                    return false;
                case 34:
                    if (add > 76 || add + nb > 80) {
                        return false;
                    }
                    break;
                case 35:
                    if (add > 160 || add + nb > 164) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }

            return true;
        }
    }

    public int mifareUlRead(byte mfulType, byte add, byte nb, byte[] data, byte[] nbBytesRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (!this.mifareUlVerifyInputData(mfulType, add, nb)) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_Read(add, nb);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 2) {
                return 32777;
            } else {
                nbBytesRead[0] = (byte)(this.mBufOut[4] - 1);
                status[0] = this.mBufOut[5];
                if (status[0] == 2) {
                    for(int i = 0; i < (this.mBufOut[4] & 255) - 1; ++i) {
                        data[i] = this.mBufOut[i + 6];
                    }
                }

                return 32769;
            }
        }
    }

    public int mifareUlWrite(byte mfulType, byte add, byte[] dataToWrite, byte[] dataRead, byte[] status, byte length) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (!this.mifareUlVerifyInputData(mfulType, add, (byte)dataToWrite.length)) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_Write(add, (byte)dataToWrite.length, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 2) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                if (status[0] == 2) {
                    for(int i = 0; i < (this.mBufOut[4] & 255) - 1; ++i) {
                        dataRead[i] = this.mBufOut[i + 6];
                    }
                }

                return 32769;
            }
        }
    }

    public int mifareUlCAuthenticate(byte keyNo, byte keyV, byte divLength, byte divInput, byte[] status, byte[] samStatus, byte length) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (keyNo > 128) {
            return 32786;
        } else if (divLength > 32) {
            return 32786;
        } else if (divInput > 32) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_C_Authenticate(keyNo, keyV, divLength, divInput);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                if (status[0] == 2) {
                    samStatus[0] = this.mBufOut[6];
                    samStatus[1] = this.mBufOut[7];
                }

                return 32769;
            }
        }
    }


    public int mifareUlEv1ReadCounter(byte add, byte length, byte[] status, byte[] counterValue) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (add > 3) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_EV1_Read_Counter(add);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                counterValue[0] = this.mBufOut[6];
                counterValue[1] = this.mBufOut[7];
                counterValue[2] = this.mBufOut[8];
                return 32769;
            }
        }
    }

    public int mifareUlEv1IncrementCounter(byte add, byte[] incrementValue, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (add > 3) {
            return 32786;
        } else if (incrementValue.length != 3) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_EV1_Increment_Counter(add, incrementValue);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 1) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int mifareUlEv1GetVersion(byte[] status, byte[] information) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (information.length != 8) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_EV1_Get_Version();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 2) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];

                for(byte i = 0; i < 8; ++i) {
                    information[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int mifareUlEv1CheckTearingEffect(byte add, byte[] status, byte validFlag) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (add > 3) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_UL_EV1_Check_Tearing_Effect(add);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[4] < 1) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                if (status[0] == 2) {
                    validFlag = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int mifareSamNxpAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock, byte lgDiversifier, byte blockDiversifier, byte[] statusCard, short[] statusSam) {
        if (DEBUG) {
            Log.d("CpcAsk", "mifareSamNxpAuthenticate(...) : START");
        }

        if (!this.mIsOpened) {
            if (DEBUG) {
                Log.d("CpcAsk", "mifareSamNxpAuthenticate(...) : END ERROR");
            }

            return 32770;
        } else if (keyAorB >= 10 && keyAorB <= 11) {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_Authenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier);
            int customFunctionTime = 1;
            long start = SystemClock.elapsedRealtime();
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            Log.d("COPPERNIC", "mifareSamNxpAuthenticate time = " + (SystemClock.elapsedRealtime() - start));
            if (ret != 32769) {
                if (DEBUG) {
                    Log.d("CpcAsk", "mifareSamNxpAuthenticate(...) : END ERROR");
                }

                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                if (DEBUG) {
                    Log.d("CpcAsk", "mifareSamNxpAuthenticate(...) : END OK");
                }

                return 32769;
            }
        } else {
            if (DEBUG) {
                Log.d("CpcAsk", "mifareSamNxpAuthenticate(...) : END ERROR");
            }

            return 32786;
        }
    }

    public int mifareSamNxpReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock, byte lgDiversifier, byte blockDiversifier, byte[] statusCard, short[] statusSam) {
        if (!this.mIsOpened) {
            return 32769;
        } else if (keyAorB >= 10 && keyAorB <= 11) {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_ReAuthenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                return 32769;
            }
        } else {
            return 32786;
        }
    }

    public int mifareSamNxpReadBlock(byte numBlock, byte[] statusCard, short[] statusSam, byte[] dataRead) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_ReadBlock(numBlock);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);

                for(int i = 0; i < 16; ++i) {
                    dataRead[i] = this.mBufOut[i + 8];
                }

                return 32769;
            }
        }
    }

    public int mifareSamNxpWriteBlock(byte numBlock, byte[] dataToWrite, byte[] statusCard, short[] statusSam, byte[] statusWrite) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_WriteBlock(numBlock, dataToWrite);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                statusWrite[0] = this.mBufOut[8];
                return 32769;
            }
        }
    }

    public int mifareSamNxpChangeKey(byte numKey, byte versionKeyA, byte versionKeyB, byte[] defaultAccess, byte numBlock, byte lgDiversifier, byte blockDiversifier, byte[] statusCard, short[] statusSam, byte[] statusChangeKey) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_ChangeKey(numKey, versionKeyA, versionKeyB, defaultAccess, numBlock, lgDiversifier, blockDiversifier);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                statusChangeKey[0] = this.mBufOut[8];
                return 32769;
            }
        }
    }

    public int mifareSamNxpIncrement(byte numBlock, byte[] increment, byte[] statusCard, short[] statusSam, byte[] statusIncrement) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else if (numBlock == 0) {
            return 32786;
        } else {
            if (numBlock < 128) {
                if ((numBlock + 1) % 4 == 0) {
                    return 32786;
                }
            } else if ((numBlock + 1) % 16 == 0) {
                return 32786;
            }

            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_Increment(numBlock, increment);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                statusIncrement[0] = this.mBufOut[8];
                return 32769;
            }
        }
    }

    public int mifareSamNxpDecrement(byte numBlock, byte[] decrement, byte[] statusCard, short[] statusSam, byte[] statusDecrement) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (numBlock > 255) {
            return 32786;
        } else if (numBlock == 0) {
            return 32786;
        } else {
            if (numBlock < 128) {
                if ((numBlock + 1) % 4 == 0) {
                    return 32786;
                }
            } else if ((numBlock + 1) % 16 == 0) {
                return 32786;
            }

            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_Decrement(numBlock, decrement);
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                statusCard[0] = this.mBufOut[5];
                short msb = (short)this.mBufOut[6];
                short lsb = (short)(this.mBufOut[7] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                statusDecrement[0] = this.mBufOut[8];
                return 32769;
            }
        }
    }

    public int mifareSamNxpBackUpValue(byte source, byte destination, byte[] statusCard, short[] statusSam, byte[] statusBackUp) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (source != 0 && destination != 0) {
            if (source != source / 4 + 3 && destination != destination / 4 + 3) {
                mSerialCommunication.flush();
                BCommands.iMIFARE_SAMNXP_BackUpValue(source, destination);
                int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
                if (ret != 32769) {
                    return ret;
                } else if (this.mBufOut[1] < 3) {
                    return 32777;
                } else {
                    statusCard[0] = this.mBufOut[5];
                    short msb = (short)this.mBufOut[6];
                    short lsb = (short)(this.mBufOut[7] & 255);
                    statusSam[0] = (short)(msb << 8 | lsb);
                    statusBackUp[0] = this.mBufOut[8];
                    return 32769;
                }
            } else {
                return 32786;
            }
        } else {
            return 32786;
        }
    }

    public int mifareSamNxpKillAuthentication(short[] statusSam) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iMIFARE_SAMNXP_KillAuthentication();
            int ret = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (ret != 32769) {
                return ret;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                short msb = (short)this.mBufOut[5];
                short lsb = (short)(this.mBufOut[6] & 255);
                statusSam[0] = (short)(msb << 8 | lsb);
                return 32769;
            }
        }
    }


    public int appendRecord(sCARD_SecurParam secur, byte[] rec, byte recSize, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (rec == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_AppendRecord(secur.AccMode, secur.SID, secur.LID, secur.NKEY, secur.RFU, rec, recSize);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cancelPurchaseCd97(byte type, byte[] dataLog, byte[] disp, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataLog == null) {
            return 32773;
        } else if (disp == null && type == 1) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_CancelPurchase(type, dataLog, disp);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97SelectFile(byte selectMode, byte[] idPath, byte idPathLen, byte[] fci, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (idPath == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_SelectFile(selectMode, idPath, idPathLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (fci != null) {
                    for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                        fci[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97StatusFile(byte selectMode, byte[] idPath, byte idPathLen, byte[] fci, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (idPath == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_StatusFile(selectMode, idPath, idPathLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (fci != null) {
                    for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                        fci[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97Invalidate(byte accMode, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Invalidate(accMode);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97Rehabilitate(byte accMode, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Rehabilitate(accMode);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97ChangeKey(byte keyIndex, byte newVersion, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (keyIndex > 3) {
            return 32773;
        } else if (keyIndex < 1) {
            return 32773;
        } else if (newVersion == 0) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ChangeKey(keyIndex, newVersion);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97ChangePin(byte[] oldPin, byte[] newPin, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (oldPin == null) {
            return 32773;
        } else if (newPin == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ChangePIN(oldPin, newPin);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97VerifyPin(byte[] pin, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (pin == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_VerifyPIN(pin);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97Increase(byte accMode, byte sid, int value, int[] newValue, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Increase(accMode, sid, (long)value);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97Decrease(byte accMode, byte sid, int value, int[] newValue, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Decrease(accMode, sid, value);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97ReadRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ReadRecord(accMode, sid, nuRec, dataLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    data[i] = this.mBufOut[i + 7];
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97AppendRecord(byte accMode, byte sid, byte[] rec, byte recSize, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (rec == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_AppendRecord(accMode, sid, rec, recSize);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97UpdateRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_UpdateRecord(accMode, sid, nuRec, data, dataLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97WriteRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_WriteRecord(accMode, sid, nuRec, data, dataLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97OpenSession(byte type, byte sid, byte nRec, sCARD_Session session, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_OpenSecuredSession(type, sid, nRec);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (session != null) {
                    session.NbApp = this.mBufOut[7];

                    int i;
                    for(i = 0; i < (this.mBufOut[7] & 255); ++i) {
                        session.Path[i] = this.mBufOut[i * 2 + 8] * 256 + this.mBufOut[i * 2 + 9];
                    }

                    for(int j = 0; j < 29; ++j) {
                        session.Data[j] = this.mBufOut[j + i * 2 + 8];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97CloseSession(byte[] result, int[] cbResult, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_CloseSecuredSession();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (cbResult != null && result != null) {
                    cbResult[0] = (this.mBufOut[1] & 255) - 5;

                    for(int i = 0; i < cbResult[0]; ++i) {
                        result[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97Purchase(byte type, byte[] dataLog, byte[] disp, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataLog == null) {
            return 32773;
        } else if (disp == null && type == 1) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Purchase(type, dataLog, disp);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97GetEpStatus(byte type, int[] ep, byte[] log, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (ep == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_GetEPStatus(type);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 8) {
                return 32777;
            } else {
                ep[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                int i;
                if (type == 0) {
                    if (this.mBufOut[1] < 30) {
                        return 32777;
                    }

                    if (log != null) {
                        for(i = 0; i < 22; ++i) {
                            log[i] = this.mBufOut[i + 10];
                        }
                    }
                } else {
                    if (this.mBufOut[1] < 27) {
                        return 32777;
                    }

                    if (log != null) {
                        for(i = 0; i < 22; ++i) {
                            log[i] = this.mBufOut[i + 10];
                        }
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97ReloadEp(byte[] chargLog1, byte[] chargLog2, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (chargLog1 == null) {
            return 32773;
        } else if (chargLog2 == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ReloadEP(chargLog1, chargLog2);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int cd97CancelPurchase(byte type, byte[] dataLog, byte[] disp, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataLog == null) {
            return 32773;
        } else if (disp == null && type == 1) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_CancelPurchase(type, dataLog, disp);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int ctxActive(byte[] data, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_Active();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctxRead(byte add, byte nb, byte[] data, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_Read(add, nb);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    data[i] = this.mBufOut[6 + i];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctxUpdate(byte add, byte nb, byte[] dataToWrite, byte[] dataInCTS, byte[] data, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_Update(add, nb, dataToWrite, dataInCTS);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctxRelease(byte param, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_Release(param);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[4];
                return 32769;
            }
        }
    }

    public int ctx512bList(byte rfu, byte[] nbTickets, byte[] serialNumbers, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (nbTickets == null) {
            return 32773;
        } else if (serialNumbers == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTX_512B_List(rfu);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                nbTickets[0] = this.mBufOut[6];

                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    serialNumbers[i] = this.mBufOut[i + 7];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512bSelect(byte[] serialNumber, byte[] serialNumberRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (serialNumberRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTX_512B_Select(serialNumber);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    serialNumberRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512bRead(byte add, byte nb, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512B_Read(add, nb);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512bUpdate(byte add, byte nb, byte[] dataToWrite, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512B_Update(add, nb, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < this.mBufOut[1] - 4; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512bHalt(byte param, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512B_Halt(param);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[4];
                return 32769;
            }
        }
    }

    public int ctx512xAuthenticate(byte add, byte kifKref, byte kvcZero, byte[] status, byte[] dataSamLength, byte[] dataSam) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_Authenticate(add, kifKref, kvcZero);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                dataSamLength[0] = (byte)(this.mBufOut[4] - 1);

                for(int i = 0; i < (dataSamLength[0] & 255); ++i) {
                    dataSam[0] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int ctx512xWriteKey(byte kifKref, byte kvcZero, byte[] status, byte[] dataSamLength, byte[] dataSam) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_WriteKey(kifKref, kvcZero);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                dataSamLength[0] = (byte)((this.mBufOut[4] & 255) - 1);

                for(int i = 0; i < (dataSamLength[0] & 255); ++i) {
                    dataSam[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int ctx512xHalt(byte param, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_Halt(param);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[4];
                return 32769;
            }
        }
    }

    public int ctx512xList(byte rfu, byte[] nbTickets, byte[] serialNumbers, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (nbTickets == null) {
            return 32773;
        } else if (serialNumbers == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTX_512X_List(rfu);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                nbTickets[0] = this.mBufOut[6];

                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    serialNumbers[i] = this.mBufOut[i + 7];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512xSelect(byte[] serialNumber, byte[] serialNumberRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (serialNumberRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTX_512X_Select(serialNumber);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 4) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    serialNumberRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512xRead(byte add, byte nb, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_Read(add, nb);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512xUpdate(byte add, byte nb, byte[] dataToWrite, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_Update(add, nb, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int ctx512xWrite(byte add, byte nb, byte[] dataToWrite, byte[] dataRead, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataRead == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCTx_512X_Write(add, nb, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 4; ++i) {
                    dataRead[i] = this.mBufOut[i + 6];
                }

                status[0] = this.mBufOut[5];
                return 32769;
            }
        }
    }

    public int increase(sCARD_SecurParam secur, byte iCount, int value, int[] newValue, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Increase(secur.AccMode, secur.SID, secur.LID, iCount, value, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int invalidate(sCARD_SecurParam secur, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Invalidate(secur.AccMode, secur.LID, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int lockUnlock(byte type, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Lock_Unlock(type);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int multiDecrease(sCARD_SecurParam secur, byte numberCpt, byte[] data, byte[] newData, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (data != null && newData != null) {
            mSerialCommunication.flush();
            BCommands.iGEN_MultiDecrease(secur.AccMode, secur.SID, secur.LID, secur.NKEY, secur.RFU, numberCpt, data);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    newData[i] = this.mBufOut[i + 7];
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        } else {
            return 32773;
        }
    }

    public int multiIncrease(sCARD_SecurParam secur, byte numberCpt, byte[] data, byte[] newData, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (data != null && newData != null) {
            mSerialCommunication.flush();
            BCommands.iGEN_MultiIncrease(secur.AccMode, secur.SID, secur.LID, (long)secur.NKEY, secur.RFU, numberCpt, data);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    newData[i] = this.mBufOut[i + 7];
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        } else {
            return 32773;
        }
    }

    public int openSession(byte type, sCARD_SecurParam secur, byte nRec, sCARD_Session session, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_OpenSecuredSession(type, secur.SID, nRec, secur.NKEY, secur.RFU, (byte)0);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (session != null) {
                    session.NbApp = this.mBufOut[7];

                    int i;
                    for(i = 0; i < (this.mBufOut[7] & 255); ++i) {
                        session.Path[i] = this.mBufOut[i * 2 + 8] * 256 + this.mBufOut[i * 2 + 9];
                    }

                    for(int j = 0; j < 29; ++j) {
                        session.Data[j] = this.mBufOut[j + i * 2 + 8];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int openSessionExt(byte type, sCARD_SecurParam secur, byte nRec, byte[] kvc, sCARD_Session session, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_OpenSecuredSession(type, secur.SID, nRec, secur.NKEY, secur.RFU, (byte)1);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (session != null) {
                    session.NbApp = this.mBufOut[7];

                    int i;
                    for(i = 0; i < (this.mBufOut[7] & 255); ++i) {
                        session.Path[i] = this.mBufOut[i * 2 + 8] * 256 + this.mBufOut[i * 2 + 9];
                    }

                    for(int j = 0; j < 29; ++j) {
                        session.Data[j] = this.mBufOut[j + i * 2 + 8];
                    }
                }

                kvc[0] = this.mBufOut[37];
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int pinStatus(sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_PINStatus();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int purchaseCd97(byte type, byte[] dataLog, byte[] disp, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (dataLog == null) {
            return 32773;
        } else if (disp == null && type == 1) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Purchase(type, dataLog, disp);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int readRecord(sCARD_SecurParam secur, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_ReadRecord(secur.AccMode, secur.SID, nuRec, dataLen, secur.LID, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    data[i] = this.mBufOut[i + 7];
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int rehabilitate(sCARD_SecurParam secur, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Rehabilitate(secur.AccMode, secur.LID, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int reloadEpCd97(byte[] chargLog1, byte[] chargLog2, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (chargLog1 == null) {
            return 32773;
        } else if (chargLog2 == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_ReloadEP(chargLog1, chargLog2);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int selectFile(byte selectMode, byte[] idPath, byte idPathLen, byte[] fci, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (idPath == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_SelectFile(selectMode, idPath, idPathLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (fci != null) {
                    for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                        fci[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int srxActive(byte[] chipType, byte[] uid, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_Active();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else {
                status[0] = this.mBufOut[5];
                chipType[0] = this.mBufOut[6];

                for(int i = 0; i < 8; ++i) {
                    uid[i] = this.mBufOut[i + 7];
                }

                return 32769;
            }
        }
    }

    public int srxReadBlocks(byte block, byte nbBlocks, byte[] status, byte[] dataLength, byte[] data) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_ReadBlocks(block, nbBlocks);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                dataLength[0] = (byte)(this.mBufOut[4] - 1);
                status[0] = this.mBufOut[5];

                for(int i = 0; i < (dataLength[0] & 255); ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int srxWriteBlocks(byte block, byte nbBlocks, byte dataLen, byte[] dataToWrite, byte[] status, byte[] dataLength, byte[] data) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (block > 255) {
            return 32786;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_WriteBlocks(block, nbBlocks, dataLen, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                status[0] = this.mBufOut[5];
                dataLength[0] = (byte)(this.mBufOut[4] - 1);

                for(byte i = 0; i < (dataLength[0] & 255); ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int srxRelease(byte param, byte[] status) {
        int vRet = 0;
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_Release((byte)0);
            vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else {
                status[0] = this.mBufOut[4];
                return 32769;
            }
        }
    }

    public int srxRead(short address, byte nbBytesToRead, byte[] status, byte[] dataLength, byte[] data) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_Read(address, nbBytesToRead);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                dataLength[0] = (byte)(this.mBufOut[4] - 1);
                status[0] = this.mBufOut[5];

                for(byte i = 0; i < (dataLength[0] & 255); ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int srxWrite(short address, byte nbBytesToWrite, byte[] dataToWrite, byte[] status, byte[] dataLength, byte[] data) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iSRx_Write(address, nbBytesToWrite, dataToWrite);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                dataLength[0] = (byte)(this.mBufOut[4] - 1);
                status[0] = this.mBufOut[5];

                for(byte i = 0; i < (dataLength[0] & 255); ++i) {
                    data[i] = this.mBufOut[i + 6];
                }

                return 32769;
            }
        }
    }

    public int statusFile(byte selectMode, byte[] idPath, byte idPathLen, byte[] fci, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (idPath == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_StatusFile(selectMode, idPath, idPathLen);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (fci != null) {
                    for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                        fci[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int updateRecord(sCARD_SecurParam secur, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_UpdateRecord(secur.AccMode, secur.SID, nuRec, data, dataLen, secur.LID, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int writeRecord(sCARD_SecurParam secur, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_WriteRecord(secur.AccMode, secur.SID, nuRec, data, dataLen, secur.LID, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int verifyPin(sCARD_SecurParam secur, byte[] pin, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (pin == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_VerifyPIN(pin, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int decrease(sCARD_SecurParam secur, byte iCount, int value, int[] newValue, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_Decrease(secur.AccMode, secur.SID, secur.LID, iCount, value, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int getEpStatusCd97(sCARD_SecurParam secur, byte type, int[] ep, byte[] log, sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else if (ep == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_GetEPStatus(type, secur.NKEY, secur.RFU);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 8) {
                return 32777;
            } else {
                ep[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                int i;
                if (type == 0) {
                    if (this.mBufOut[1] < 30) {
                        return 32777;
                    }

                    if (log != null) {
                        for(i = 0; i < 22; ++i) {
                            log[i] = this.mBufOut[i + 10];
                        }
                    }
                } else {
                    if (this.mBufOut[1] < 27) {
                        return 32777;
                    }

                    for(i = 0; i < 19; ++i) {
                        log[i] = this.mBufOut[i + 10];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int giveCertificate(byte keyType, byte param, byte lngBuffer, byte[] buffer, byte lngCertificat, byte[] certificat, byte[] status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iGEN_GiveCertificate(keyType, param, lngBuffer, buffer, lngCertificat);
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 3) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 3; ++i) {
                    certificat[i] = this.mBufOut[i + 5];
                }

                status[0] = this.mBufOut[4];
                return 32769;
            }
        }
    }

    public int gtmlSelectFile(byte selectMode, byte[] idPath, byte idPathLen, byte[] fci, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (idPath == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_SelectFile(selectMode, idPath, idPathLen);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (fci != null) {
                    for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                        fci[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlInvalidate(byte accMode, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Invalidate(accMode);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlRehabilitate(byte accMode, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Rehabilitate(accMode);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlChangePin(byte[] oldPin, byte[] newPin, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (oldPin == null) {
            return 32773;
        } else if (newPin == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ChangePIN(oldPin, newPin);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlVerifyPin(byte[] pin, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (pin == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_VerifyPIN(pin);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlIncrease(byte accMode, byte sid, int value, int[] newValue, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Increase(accMode, sid, (long)value);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlDecrease(byte accMode, byte sid, int value, int[] newValue, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_Decrease(accMode, sid, value);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (newValue != null) {
                    if (this.mBufOut[1] == 5) {
                        newValue[0] = 0;
                    } else {
                        newValue[0] = this.mBufOut[7] * 65536 + this.mBufOut[8] * 256 + this.mBufOut[9];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlReadRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_ReadRecord(accMode, sid, nuRec, dataLen);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                for(int i = 0; i < (this.mBufOut[1] & 255) - 5; ++i) {
                    data[i] = this.mBufOut[i + 7];
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlAppendRecord(byte accMode, byte sid, byte[] rec, byte recSize, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (rec == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_AppendRecord(accMode, sid, rec, recSize);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlUpdateRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_UpdateRecord(accMode, sid, nuRec, data, dataLen);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlWriteRecord(byte accMode, byte sid, byte nuRec, byte dataLen, byte[] data, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else if (data == null) {
            return 32773;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_WriteRecord(accMode, sid, nuRec, data, dataLen);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] != 5) {
                return 32777;
            } else {
                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlOpenSession(byte type, byte sid, byte nRec, sCARD_Session session, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_OpenSecuredSession(type, sid, nRec);
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (session != null) {
                    session.NbApp = this.mBufOut[7];

                    int i;
                    for(i = 0; i < (this.mBufOut[7] & 255); ++i) {
                        session.Path[i] = this.mBufOut[i * 2 + 8] * 256 + this.mBufOut[i * 2 + 9];
                    }

                    for(int j = 0; j < 29; ++j) {
                        session.Data[i] = this.mBufOut[j + i * 2 + 8];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int gtmlCloseSession(byte[] result, int[] cbResult, sCARD_Status status) {
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.iCD97_CloseSecuredSession();
            BCommands.iCD97_ToGTML();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else if (this.mBufOut[1] < 5) {
                return 32777;
            } else {
                if (cbResult != null && result != null) {
                    cbResult[0] = (this.mBufOut[1] & 255) - 5;

                    for(int i = 0; i < cbResult[0]; ++i) {
                        result[i] = this.mBufOut[i + 7];
                    }
                }

                if (status != null) {
                    status.Code = this.mBufOut[4];
                    status.Byte1 = this.mBufOut[5];
                    status.Byte2 = this.mBufOut[6];
                }

                return 32769;
            }
        }
    }

    public int loadISOAConfigForMifareCards(sCARD_Status status) {
        zeroMemory(this.mBufOut, this.mBufOut.length);
        if (!this.mIsOpened) {
            return 32770;
        } else {
            mSerialCommunication.flush();
            BCommands.csc_loadIsoAConfig();
            int vRet = this.cscSendReceive(this.mFuncTimeout, BCommands.giCSCTrame, BCommands.giCSCTrameLn);
            if (vRet != 32769) {
                return vRet;
            } else {
                return this.mBufOut[5] != 0 ? '耉' : '老';
            }
        }
    }
}
