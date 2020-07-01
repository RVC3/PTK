package ru.ppr.rfidreal;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import fr.coppernic.cpcframework.cpcask.Commands;
import fr.coppernic.cpcframework.cpcask.Defines;
import fr.coppernic.cpcframework.cpcask.OnGetReaderInstanceListener;
import fr.coppernic.cpcframework.cpcask.Reader;
import fr.coppernic.cpcframework.cpcask.sCARD_SearchExt;
import fr.coppernic.cpcframework.cpcask.sCARD_Status;
import fr.coppernic.cpcframework.cpcpowermgmt.cone.PowerMgmt;
import fr.coppernic.sdk.utils.core.CpcBytes;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Реализация обертки над считывателем смарт-карт от Coppernic SDK.
 *
 * @author G.Kashka
 */
public class AscReader implements IAscReader {

    private static final String TAG = AscReader.class.getSimpleName();

    /**
     * значение для увеличение хардварного счетчика
     */
    public static final byte[] INCREMENT_COUNTER_VALUE = {01, 00, 00, 00};

    private PowerMgmt mPowerMgmt = null;
    private Reader mReader = null;

    private static final String RFID_PORT = "/dev/ttyHSL1";
    private static final int BAUDRATE = 115200;

    private int lastResult = Defines.RCSC_Ok;

    /// Константы для работы с картами UID 7bytes
    private final byte[] MIG_7UID_HYSTORICAL_BYTES = {0x66, 0x53, 0x43, 0x4F, 0x4E, 0x45, 0x44, 0x32, 0x30};
    private final byte[] MIG_7UID_HYSTORICAL_BYTES_2 = {0x00, 0x31, (byte) 0xC0, 0x64, 0x08, 0x44, 0x03, (byte) 0x94, 0x00, (byte) 0x90, 0x00};
    private final byte[] REQUEST_A = {(byte) 0x26};
    private final byte[] ANTICOLLISION_LEVEL_1 = {(byte) 0x93, (byte) 0x20};
    private final byte[] SELECT_CASCADE_LEVEL_1_PREFIX = {(byte) 0x93, (byte) 0x70};
    private final byte[] ANTICOLLISION_LEVEL_2 = {(byte) 0x95, (byte) 0x20};
    private final byte[] SELECT_CASCADE_LEVEL_2_PREF = {(byte) 0x95, (byte) 0x70};

    public AscReader(Context context) {
        Logger.info(TAG, "AscReader() constructor START");
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Reader.getInstance(context, new OnGetReaderInstanceListener() {
            @Override
            public void OnGetReaderInstance(Reader reader) {
                Logger.info(TAG, "Constructor() Reader getInstance Done");
                mReader = reader;
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Logger.error(TAG, e);
            Thread.currentThread().interrupt();
        }
        mPowerMgmt = new PowerMgmt(context);
        Logger.info(TAG, "AscReader() constructor FINISH");
    }

    /**
     * Сейчас любая команда ридера может вернуть ошибку таймаута, в этом случае команды выполняются 2-3 секунды
     */
    public boolean isTimeoutError() {
        return (lastResult == Defines.RCSC_Timeout);
    }

    public int getLastResult() {
        return lastResult;
    }

    @Override
    public void setPower(boolean on) {
        long timer = getTimer();
        mPowerMgmt.setPower(PowerMgmt.PeripheralTypesCone.RfidSc, PowerMgmt.ManufacturersCone.Ask, PowerMgmt.ModelsCone.Ucm108,
                PowerMgmt.InterfacesCone.ExpansionPort, on);
        addLog("setPower(on=" + on + ") DONE {} " + getTimeString(timer));
    }

    public boolean cscVersionCsc(StringBuilder firmwareVersion) {
        long timer = getTimer();
//        int res = mReader.cscVersionCsc(firmwareVersion);
        lastResult = cscVersionCscDecompiled(firmwareVersion);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscVersionCsc() DONE {" + getResString(lastResult) + ", version=\"" + firmwareVersion + "\"} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * собственная реализация метода получения версии ридера с уменьшенным таймаутом
     */
    private int cscVersionCscDecompiled(StringBuilder sb) {
        int timeout = 100;
        Commands.giCSCTrame[0] = -128;
        Commands.giCSCTrame[1] = 2;
        Commands.giCSCTrame[2] = 1;
        Commands.giCSCTrame[3] = 1;
        Commands.giCSCTrame[4] = 0;
        Commands.giCSCTrameLn = 5;
        Commands.icsc_SetCRC();
        int ret = cscSendReceive(timeout, Commands.giCSCTrame, Commands.giCSCTrameLn);
        if (ret != Defines.RCSC_Ok) {
            return ret;
        } else if (mReader.getLnOut() > 6) {
            String szVersion = new String(mReader.getBufOut(), 4, mReader.getLnOut() - 6);
            sb.append(szVersion);
            return Defines.RCSC_Ok;
        } else {
            return Defines.RCSC_Timeout;
        }
    }

    public boolean mifareLoadReaderKeyIndex(byte keyIndex, byte[] keyVal) {
        long timer = getTimer();
        byte[] status = new byte[1];
        lastResult = mReader.mifareLoadReaderKeyIndex(keyIndex, keyVal, status);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("mifareLoadReaderKeyIndex(keyIndex=" + keyIndex + ", keyVal=" + getHex(keyVal) + ") DONE {" + getResString(lastResult) + ", status=" + getHex(status) + "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    @Deprecated
    public boolean mifareSamNxpKillAuthenticationOld() {
        long timer = getTimer();
        boolean out = false;
        short[] statusSam = new short[1];
        lastResult = mReader.mifareSamNxpKillAuthentication(statusSam);
        if (lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000)
            out = true;
        addLog("mifareSamNxpKillAuthentication() DONE {" +
                getResString(lastResult) +
                ", statusSam=" + statusSam[0] +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareSamNxpKillAuthentication() {
        long timer = getTimer();
        boolean out = false;
        short[] statusSam = new short[1];
        lastResult = mReader.mifareSamNxpKillAuthentication(statusSam);
        if (lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000) {
            out = true;

        } else if (lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x6883) {
            addLog("mifareSamNxpKillAuthentication() Iteration 1 DONE {" +
                    getResString(lastResult) +
                    ", statusSam=" + statusSam[0] +
                    "} " + getResString(out) + getTimeString(timer));
            lastResult = mReader.mifareSamNxpKillAuthentication(statusSam);
            if (lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000) {
                out = true;
            }
        }
        addLog("mifareSamNxpKillAuthentication() DONE {" +
                getResString(lastResult) +
                ", statusSam=" + statusSam[0] +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * Пишет блок данных с использованием SAM
     */
    public boolean mifareSamNxpWriteBlock(byte address, byte[] data) {
        long timer = getTimer();
        boolean out = true;
        // for SAM
        byte[] statusCard = new byte[1];
        short[] statusSam = new short[1];
        byte[] statusWrite = new byte[16];
        lastResult = mReader.mifareSamNxpWriteBlock(address, data, statusCard, statusSam, statusWrite);

        if (lastResult != Defines.RCSC_Ok || statusWrite[0] != 10) {
            out = false;
        }

        addLog("mifareSamNxpWriteBlock(address=" + address + ", data=" + getHex(data) + ") DONE {" +
                getResString(lastResult) +
                ", statusCard=" + getHex(statusCard) +
                ", statusSam=" + statusSam[0] +
                ", statusWrite[0]=" + getHex(statusWrite[0]) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * Пишет блок данных без SAM
     */
    public boolean mifareWriteBlock(byte numBlock, byte[] dataToWrite) {
        long timer = getTimer();
        boolean out = true;
        byte[] dataVerif = new byte[16];
        byte[] status = new byte[1];
        lastResult = mReader.mifareWriteBlock(numBlock, dataToWrite, dataVerif, status);

        if (lastResult != Defines.RCSC_Ok || !Arrays.equals(dataToWrite, dataVerif)) {
            out = false;
        }

        addLog("mifareWriteBlock(numBlock=" + numBlock + ", dataToWrite=" + getHex(dataToWrite) + ") DONE {" +
                getResString(lastResult) +
                ", dataVerif=" + getHex(dataVerif) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * Чтение данный с ультралайта
     */
    public boolean mifareUlRead(byte mfulType, byte add, byte length, byte[] data) {
        long timer = getTimer();
        byte[] readCountBytes = new byte[1];
        byte[] tmpArray = new byte[1];
        lastResult = mReader.mifareUlRead(mfulType, add, length, data, readCountBytes, tmpArray);
        boolean out = false;
        if (lastResult != Defines.RCSC_Ok) {
            out = false;
        } else if (length != readCountBytes[0]) {
            out = false;
        } else {
            out = true;
        }
        addLog("mifareUlRead(" +
                "mfulType=" + mfulType +
                ", add=" + add +
                ", length=" + length +
                ") DONE {" + getResString(lastResult) +
                ", data=" + getHex(data) +
                ", readCountBytes=" + getHex(readCountBytes) +
                ", tmpArray=" + getHex(tmpArray) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public int cscConfigIoExt(byte inputMask, byte enablePullUp, byte enableFilter, byte outputMask, byte outputDefaultValue, byte outputEnableOpenDrain, byte outputEnablePullUp) {
        long timer = getTimer();
        lastResult = mReader.cscConfigIoExt(inputMask, enablePullUp, enableFilter, outputMask, outputDefaultValue, outputEnableOpenDrain, outputEnablePullUp);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscConfigIoExt(" +
                "inputMask=" + getHex(inputMask) +
                ", enablePullUp=" + getHex(enablePullUp) +
                ", enableFilter=" + getHex(enableFilter) +
                ", outputMask=" + getHex(outputMask) +
                ", outputDefaultValue=" + getHex(outputDefaultValue) +
                ", outputEnableOpenDrain=" + getHex(outputEnableOpenDrain) +
                ", outputEnablePullUp=" + getHex(outputEnablePullUp) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public int cscWriteIoExt(byte ioToWrite, byte value) {
        long timer = getTimer();
        lastResult = mReader.cscWriteIoExt(ioToWrite, value);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscWriteIoExt(" +
                "ioToWrite=" + getHex(ioToWrite) +
                ", value=" + getHex(value) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public int cscSelectSam(byte nSam, byte type) {
        long timer = getTimer();
        lastResult = mReader.cscSelectSam(nSam, type);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscSelectSam(" +
                "nSam=" + getHex(nSam) +
                ", type=" + getHex(type) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public int cscResetSam(byte nSam, byte[] lpAtr, int[] lpcbAtr) {
        long timer = getTimer();
        lastResult = mReader.cscResetSam(nSam, lpAtr, lpcbAtr);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscResetSam(" +
                "nSam=" + getHex(nSam) +
                //", lpAtr=" + getHex(lpAtr) + - слишком длинное
                // ", lpcbAtr=" + lpcbAtr.toString() + - слишком длинное
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public int cscSetSamBaudratePps(byte proProt, byte paramFd, byte[] status) {
        long timer = getTimer();
        lastResult = mReader.cscSetSamBaudratePps(proProt, paramFd, status);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscSetSamBaudratePps(" +
                "proProt=" + getHex(proProt) +
                ", paramFd=" + getHex(paramFd) +
                ", status=" + getHex(status) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public boolean cscSearchCardExt(byte[] com, int[] lpcbATR, byte[] lpATR) {
        long timer = getTimer();
        sCARD_SearchExt search = new sCARD_SearchExt();

        search.CONT = 0;
        search.INNO = 0;
        search.ISOA = 1;
        search.ISOB = 0;
        search.MIFARE = 1;
        search.MONO = 0;
        search.MV4k = 0;
        search.MV5k = 0;
        search.TICK = 0;

        int searchMask = Defines.SEARCH_MASK_ISOA | Defines.SEARCH_MASK_MIFARE;
        byte forget = (byte) 0x01;
        byte timeOut = (byte) 0x50;
        lastResult = mReader.cscSearchCardExt(search, searchMask, forget, timeOut, com, lpcbATR, lpATR);

        boolean out = lastResult == Defines.RCSC_Ok;

        if (com[0] == 0x6F || lpcbATR[0] == 0 || lpATR.length <= 0) {
            //NO card detected
            out = false;
        } else {
            if (com[0] == 0x02) {
                addLog("cscSearchCardExt() ISO14443-A card detected");
                byte[] atrHistoricalBytes = getCardAtrHistoricalBytes(lpATR);
                if (null == atrHistoricalBytes) {
                    addLog("cscSearchCardExt() Fail to parse ATR");
                    out = false;
                }
                if (out) {
                    addLog("cscSearchCardExt() Historical Byte of ATR : " + CpcBytes.byteArrayToString(atrHistoricalBytes));
                    if (CpcBytes.arrayCmp(atrHistoricalBytes, MIG_7UID_HYSTORICAL_BYTES) || CpcBytes.arrayCmp(atrHistoricalBytes, MIG_7UID_HYSTORICAL_BYTES_2)) {
                        addLog("cscSearchCardExt() This is a MIG 7UID card");
                        long start = SystemClock.elapsedRealtime();
                        int res = select7UidCard();
                        addLog("cscSearchCardExt() select7UidCard() Time = " + (SystemClock.elapsedRealtime() - start));
                        if (Defines.RCSC_Ok == res) {
                            //return CpcResult.RESULT.OK;
                            out = true;
                        } else {
                            out = false;
                            //return CpcResult.RESULT.ERROR;
                        }
                    }
                }
            } else if (com[0] == 0x05) {
                addLog("cscSearchCardExt() MIFARE - ISO14443 card detected");
            } else {
                addLog("cscSearchCardExt() UNKNOWN CARD DETECTED : " + CpcBytes.byteArrayToString(com));
            }
        }

        addLog("cscSearchCardExt(" +
                //"search=" + search..toString() +
                "searchMask=" + searchMask +
                ", forget=" + getHex(forget) +
                ", timeOut=" + getHex(timeOut) +
                ") DONE {" + getResString(lastResult) +
                ", com=" + getHex(com) +
                //", lpcbATR=" + lpcbATR.toString() +
                ", lpATR=" + getHex(lpATR) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * Некий метод получения какого-то AtrNumber. Нужен для чтения UID7byte карт
     *
     * @param atr
     * @return
     */
    private byte[] getCardAtrHistoricalBytes(byte[] atr) {
        long timer = getTimer();

        byte[] out;

        if ((null == atr) || (atr.length < 3)) {
            out = null;
        } else {

            int historicalBytesIndex = atr[1] + 11;
            int historicalBytesLength = (atr[2 + atr[1]]) - 8;

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(atr, historicalBytesIndex, historicalBytesLength);

            out = output.toByteArray();
        }

        addLog("getCardAtrHistoricalBytes(atr=" + getHex(atr) + ") DONE return " + getHex(out) + getTimeString(timer));
        return out;

    }

    /**
     * Метод как-то подготавливает ридер для чтения 7байтовых карт
     *
     * @return
     */
    private int select7UidCard() {

        long timer = getTimer();

        // Switch off Antenna Field
        int res = mReader.cscAntennaOff();
        if (res != Defines.RCSC_Ok) {
            return res;
        }

        // Load the ISOA config as default config for MIFARE cards
        sCARD_Status cmdStatus = new sCARD_Status();
        res = mReader.loadISOAConfigForMifareCards(cmdStatus);
        //if ((res != Defines.RCSC_Ok)||(cmdStatus.Code!=0x00)) {
        if (res != Defines.RCSC_Ok) {
            return res;
        }

        //https://aj.srvdev.ru/browse/CPPKPP-31833
        SystemClock.sleep(30);

        // Configure Transparent mode communication
        byte[] resIso = new byte[1];
        byte[] resAddCrc = new byte[1];
        byte[] resCheckCrc = new byte[1];
        byte[] resConfigField = new byte[1];
        res = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x01, resIso, resAddCrc, resCheckCrc, resConfigField);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        // Send the REQ ISO A command
        byte[] status = new byte[1];
        int[] lnOut = new int[1];
        byte[] bufOut = new byte[256];
        res = mReader.cscTransparentCommand(REQUEST_A, REQUEST_A.length, status, lnOut, bufOut);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        // Configure Transparent mode communication
        res = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x88, (byte) 0x08, (byte) 0x00, resIso, resAddCrc, resCheckCrc, resConfigField);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        byte[] cardClxUid = new byte[5];
        // Send ANTICOLLISION CASCADE LEVEL 1
        res = mReader.cscTransparentCommand(ANTICOLLISION_LEVEL_1, ANTICOLLISION_LEVEL_1.length, status, lnOut, bufOut);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        } else if (lnOut[0] != 5) {
            Log.e(TAG, "Anticollision Initialisation error - invalid CL1 UID answer");
            return Defines.RCSC_BadATR;
        } else {
            // If one card has answered and answer id valid
            // We save the CL1 UID card answer
            System.arraycopy(bufOut, 0, cardClxUid, 0, 5);
        }

        // Configure Transparent mode communication
        res = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x89, (byte) 0x09, (byte) 0x00, resIso, resAddCrc, resCheckCrc, resConfigField);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        byte[] selectCascadeLvl1 = CpcBytes.appendTwoByteArrays(SELECT_CASCADE_LEVEL_1_PREFIX, cardClxUid);
        // Send ANTICOLLISION CASCADE LEVEL 1
        res = mReader.cscTransparentCommand(selectCascadeLvl1, selectCascadeLvl1.length, status, lnOut, bufOut);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        // Configure Transparent mode communication
        res = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x88, (byte) 0x08, (byte) 0x00, resIso, resAddCrc, resCheckCrc, resConfigField);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        // Send ANTICOLLISION CASCADE LEVEL 2
        res = mReader.cscTransparentCommand(ANTICOLLISION_LEVEL_2, ANTICOLLISION_LEVEL_2.length, status, lnOut, bufOut);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        } else if (lnOut[0] != 5) {
            Log.e(TAG, "Anticollision Initialisation error - invalid CL2 UID answer");
            return Defines.RCSC_BadATR;
        } else {
            // If one card has answered and answer id valid
            // We save the CL1 UID card answer
            System.arraycopy(bufOut, 0, cardClxUid, 0, 5);
        }

        // Configure Transparent mode communication
        res = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x89, (byte) 0x09, (byte) 0x00, resIso, resAddCrc, resCheckCrc, resConfigField);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        byte[] selectCascadeLvl2 = CpcBytes.appendTwoByteArrays(SELECT_CASCADE_LEVEL_2_PREF, cardClxUid);
        // Send ANTICOLLISION CASCADE LEVEL 2
        res = mReader.cscTransparentCommand(selectCascadeLvl2, selectCascadeLvl2.length, status, lnOut, bufOut);
        if ((res != Defines.RCSC_Ok)) {
            return res;
        }

        return res;

    }

    public int cscSendReceive(int timeout, byte[] bufIn, int lnIn) {
        long timer = getTimer();
        lastResult = mReader.cscSendReceive(timeout, bufIn, lnIn);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscSendReceive(" +
                "timeout=" + timeout +
                // ", bufIn=" + getHex(bufIn) + - слишком длинное
                ", lnIn=" + lnIn +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public int cscTransparentCommand(byte[] bufIn, int lnIn, byte[] status, int[] lnOut, byte[] bufOut) {
        long timer = getTimer();
        int res = mReader.cscTransparentCommand(bufIn, lnIn, status, lnOut, bufOut);
        boolean out = res == Defines.RCSC_Ok;
        addLog("cscTransparentCommand(" +
                "bufIn=" + getHex(bufIn) +
                ", lnIn=" + lnIn +
                ", status=" + getHex(status) +
                //", lnOut=" + lnOut.toString() +
                //", bufOut=" + getHex(bufOut) +
                ") DONE {" + getResString(res) +
                "} " + getResString(out) + getTimeString(timer));
        return res;
    }

    public int cscTransparentCommandConfig(byte iso, byte addCRC, byte checkCRC, byte field, byte[] configISO, byte[] configAddCRC, byte[] configCheckCRC, byte[] configField) {
        long timer = getTimer();
        lastResult = mReader.cscTransparentCommandConfig(iso, addCRC, checkCRC, field, configISO, configAddCRC, configCheckCRC, configField);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscTransparentCommandConfig(" +
                "iso=" + getHex(iso) +
                ", addCRC=" + getHex(addCRC) +
                ", checkCRC=" + getHex(checkCRC) +
                ", field=" + getHex(field) +
                ", configISO=" + getHex(configISO) +
                ", configAddCRC=" + getHex(configAddCRC) +
                ", configCheckCRC=" + getHex(configCheckCRC) +
                ", configField=" + getHex(configField) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return lastResult;
    }

    public boolean mifareSamNxpReadBlock(byte numBlock, byte[] dataRead) {
        byte[] statusCard = new byte[1];
        short[] statusSam = new short[1];
        long timer = getTimer();
        lastResult = mReader.mifareSamNxpReadBlock(numBlock, statusCard, statusSam, dataRead);

        boolean out = false;
        if (lastResult == Defines.RCSC_Ok && statusCard[0] == 1 && statusSam[0] == (short) 0x9000) {
            out = true;
        }

        addLog("mifareSamNxpReadBlock(" +
                "numBlock=" + numBlock +
                ", statusCard=" + getHex(statusCard) +
                ", statusSam[0]=" + statusSam[0] +
                ", dataRead=" + getHex(dataRead) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareReadBlock(byte numBlock, byte[] dataRead) {
        long timer = getTimer();
        byte[] statusReadBlock = new byte[1];
        lastResult = mReader.mifareReadBlock(numBlock, dataRead, statusReadBlock);
        boolean out = false;
        if (lastResult == Defines.RCSC_Ok && statusReadBlock[0] == 0) {
            out = true;
        }

        addLog("mifareReadBlock(" +
                "numBlock=" + numBlock +
                ", dataRead=" + getHex(dataRead) +
                ", statusReadBlock=" + getHex(statusReadBlock) +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareUlEv1ReadCounter(byte add, byte length, byte[] counterValue) {
        long timer = getTimer();
        byte[] status = new byte[1];
        lastResult = mReader.mifareUlEv1ReadCounter(add, length, status, counterValue);
        boolean out = lastResult == Defines.RCSC_Ok;

        addLog("mifareUlEv1ReadCounter(" +
                "add=" + getHex(add) +
                ", length=" + getHex(length) +
                ") DONE {" + getResString(lastResult) +
                ", counterValue=" + getHex(counterValue) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    /**
     * Пишет блок данных на ультралайт
     */
    public boolean mifareUlWrite(byte mfulType, int page, byte[] dataToWrite) {
        long timer = getTimer();
        boolean out = false;
        byte[] dataRead = new byte[dataToWrite.length];
        byte[] status = new byte[1];
        byte length = 0;
        byte address = (byte) (page * 4);
        lastResult = mReader.mifareUlWrite(mfulType, address, dataToWrite, dataRead, status, length);

        if (lastResult == Defines.RCSC_Ok) {

            //костыль для инкрементации счетчика, почемуто возвращается 0000, хотя запись прошла успешно
            if (page == 41 && Arrays.equals(new byte[dataRead.length], dataRead)) {
                out = true;
            } else if (Arrays.equals(dataToWrite, dataRead)) {
                out = true;
            }
        }

        addLog("mifareUlWrite(mfulType=" + mfulType + ", page=" + page + ", length=" + length + ", dataToWrite=" + getHex(dataToWrite) + ") DONE {" +
                getResString(lastResult) +
                ", dataRead=" + getHex(dataRead) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }


    public boolean cscEnterHuntPhaseParameters() {
        long timer = getTimer();
        lastResult = mReader.cscEnterHuntPhaseParameters((byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00, null, (byte) 0x00, (byte) 0x00);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscEnterHuntPhaseParameters(" +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean cscOpen() {
        long timer = getTimer();
        lastResult = mReader.cscOpen(RFID_PORT, BAUDRATE, false);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("cscOpen(" +
                ") DONE {" + getResString(lastResult) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public void cscClose() {
        long timer = getTimer();
        mReader.cscClose();
        addLog("cscClose(" +
                ") DONE {" +
                "} " + getTimeString(timer));
    }

    @Override
    public boolean mifareUlEv1IncrementCounter(byte add, @NonNull byte[] incrementValue) {
        long timer = getTimer();
        byte[] status = new byte[1];
        lastResult = mReader.mifareUlEv1IncrementCounter(add, incrementValue, status);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("mifareUlEv1IncrementCounter(" +
                "add=" + add +
                ", incrementValue=" + getHex(incrementValue) +
                ") DONE {" + getResString(lastResult) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareUlIdentifyType(byte[] status) {
        long timer = getTimer();
        lastResult = mReader.mifareUlIdentifyType(status);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("mifareUlIdentifyType(" +
                ") DONE {" + getResString(lastResult) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareUlEv1GetVersion(byte[] status, byte[] information) {
        long timer = getTimer();
        lastResult = mReader.mifareUlEv1GetVersion(status, information);
        boolean out = lastResult == Defines.RCSC_Ok;
        addLog("mifareUlEv1GetVersion(" +
                ") DONE {" + getResString(lastResult) +
                ", status=" + getHex(status) +
                ", information=" + getHex(information) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareSamNxpAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock) {
        long timer = getTimer();
        //номер блока для авторизации - любой блок в секторе, например нулевой
        byte blockNumber = 0;
        byte[] statusCard = new byte[1];
        short[] statusSam = new short[1];
        byte lgDiversifier = (byte) 0;
        byte blockDiversifier = (byte) 0;

        lastResult = mReader.mifareSamNxpAuthenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier, statusCard, statusSam);
        boolean out = lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000;

        addLog("mifareSamNxpAuthenticate(" +
                "numKey=" + numKey +
                ", versionKey=" + versionKey +
                ", keyAorB=" + getHex(keyAorB) +
                ", versionKey=" + getHex(versionKey) +
                ", numBlock=" + numBlock +
                ", lgDiversifier=" + lgDiversifier +
                ", blockDiversifier=" + blockDiversifier +
                ") DONE {" + getResString(lastResult) +
                ", statusCard=" + getHex(statusCard) +
                ", statusSam[0]=" + statusSam[0] +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareSamNxpReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock) {
        long timer = getTimer();
        //номер блока для авторизации - любой блок в секторе, например нулевой
        byte blockNumber = 0;
        byte[] statusCard = new byte[1];
        short[] statusSam = new short[1];
        byte lgDiversifier = (byte) 0;
        byte blockDiversifier = (byte) 0;

        lastResult = mReader.mifareSamNxpReAuthenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier, statusCard, statusSam);
        boolean out = lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000;

        addLog("mifareSamNxpReAuthenticate(" +
                "numKey=" + numKey +
                ", versionKey=" + versionKey +
                ", keyAorB=" + getHex(keyAorB) +
                ", versionKey=" + getHex(versionKey) +
                ", numBlock=" + numBlock +
                ", lgDiversifier=" + lgDiversifier +
                ", blockDiversifier=" + blockDiversifier +
                ") DONE {" + getResString(lastResult) +
                ", statusCard=" + getHex(statusCard) +
                ", statusSam[0]=" + statusSam[0] +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    public boolean mifareAuthenticate(byte numSector, byte keyAorB, byte keyIndex) {
        long timer = getTimer();

        byte[] mifareType = {0x00};
        byte[] serialNumber = new byte[4];
        byte[] status = new byte[1];

        lastResult = mReader.mifareAuthenticate(numSector, keyAorB, keyIndex, mifareType, serialNumber, status);
        boolean out = lastResult == Defines.RCSC_Ok && status[0] == 0;

        addLog("mifareAuthenticate(" +
                "numSector=" + numSector +
                ", keyAorB=" + getHex(keyAorB) +
                ", keyIndex=" + keyIndex +
                ") DONE {" + getResString(lastResult) +
                ", mifareType=" + getHex(mifareType) +
                ", serialNumber=" + getHex(serialNumber) +
                ", status=" + getHex(status) +
                "} " + getResString(out) + getTimeString(timer));
        return out;
    }

    private void addLog(String text) {
        Logger.trace(TAG, text);
    }

    private long getTimer() {
        return System.currentTimeMillis();
    }

    private String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

    private String getResString(int res) {
        return "res=" + Defines.errorLookUp(res);
    }

    private String getResString(boolean res) {
        return (res) ? "OK" : "FAILED";
    }

    private String getHex(byte[] value) {
        return CommonUtils.bytesToHexWithoutSpaces(value);
    }

    private String getHex(byte value) {
        return CommonUtils.byteToHex(value);
    }


}
