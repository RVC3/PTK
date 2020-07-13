package ru.ppr.rfidreal;

import android.content.Context;
import android.device.IccManager;
import android.support.annotation.NonNull;

import fr.coppernic.cpcframework.cpcask.Commands;
import fr.coppernic.cpcframework.cpcask.OnGetReaderInstanceListener;
import fr.coppernic.cpcframework.cpcask.Reader;
import fr.coppernic.cpcframework.cpcask.Defines;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

import android.os.SystemClock;

/**
 * Реализация обертки над считывателем смарт-карт от Coppernic SDK для i9000S
 *
 * @author A. Kopanev
 */

public class AscReader_9000S implements IAscReader {

    private static final String TAG = AscReader_9000S.class.getSimpleName();

    private int lastResult = Defines.RCSC_Ok;
    public static final byte SLOT_IC = 0x00;
    public static final byte SLOT_SAM1 = 0x01;
    public static final byte SLOT_SAM2 = 0x02;
    public static final byte TYPE_IC = 0x01;
    public static final byte TYPE_SLE4442 = 0x02;
    public static final byte VOLT_3 = 0x01;
    private byte Slot;

    public void setSlot(byte slot) {
        Slot = slot;
    }

    IccManager mIccManager = null;

    public AscReader_9000S(Context context) {
        Logger.info(TAG, "AscReader_9000s() constructor START");
        //Создать объект класса Reader
        Reader.getInstance(context, new OnGetReaderInstanceListener() {
            @Override
            public void OnGetReaderInstance(Reader reader) {
                Logger.info(TAG, "Constructor() Reader getInstance Done");
            }
        });

        //Создать экземпляр класса IccManager
        mIccManager = new IccManager();

        Logger.info(TAG, "AscReader_9000S() constructor FINISH");
    }

    @Override
    public void setPower(boolean on) {

    }

    /**
     * Сейчас любая команда ридера может вернуть ошибку таймаута, в этом случае команды выполняются 2-3 секунды
     */
    public boolean isTimeoutError() {
        return (lastResult == Defines.RCSC_Timeout);
    }

    @Override
    public boolean cscVersionCsc(StringBuilder firmwareVersion) {
        return false;
    }

    @Override
    public boolean mifareLoadReaderKeyIndex(byte keyIndex, byte[] keyVal) {
        return false;
    }

    @Override
    public boolean mifareSamNxpWriteBlock(byte address, byte[] data) {
        return false;
    }

    @Override
    public boolean mifareUlRead(byte mfulType, byte add, byte length, byte[] data) {
        return false;
    }

    @Override
    public int cscConfigIoExt(byte inputMask, byte enablePullUp, byte enableFilter, byte outputMask, byte outputDefaultValue, byte outputEnableOpenDrain, byte outputEnablePullUp) {
        return 0;
    }

    @Override
    public int cscWriteIoExt(byte ioToWrite, byte value) {
        return 0;
    }

    @Override
    public int cscSelectSam(byte nSam, byte type) {
        return 0;
    }

    @Override
    public int cscResetSam(byte nSam, byte[] lpAtr, int[] lpcbAtr) {
        return 0;
    }

    @Override
    public int cscSetSamBaudratePps(byte proProt, byte paramFd, byte[] status) {
        return 0;
    }

    @Override
    public boolean cscSearchCardExt(byte[] com, int[] lpcbATR, byte[] lpATR) {
        return false;
    }

    @Override
    public int cscSendReceive(int timeout, byte[] bufIn, int lnIn) {
        return 0;
    }

    @Override
    public int cscTransparentCommand(byte[] bufIn, int lnIn, byte[] status, int[] lnOut, byte[] bufOut) {
        return 0;
    }

    @Override
    public int cscTransparentCommandConfig(byte iso, byte addCRC, byte checkCRC, byte field, byte[] configISO, byte[] configAddCRC, byte[] configCheckCRC, byte[] configField) {
        return 0;
    }

    @Override
    public boolean mifareSamNxpReadBlock(byte numBlock, byte[] dataRead) {
        return false;
    }

    @Override
    public boolean mifareReadBlock(byte numBlock, byte[] dataRead) {
        return false;
    }

    @Override
    public boolean mifareUlEv1ReadCounter(byte add, byte length, byte[] counterValue) {
        return false;
    }

    @Override
    public boolean mifareUlWrite(byte mfulType, int page, byte[] dataToWrite) {
        return false;
    }

    @Override
    public boolean cscEnterHuntPhaseParameters() {
        return false;
    }

    @Override
    public boolean cscOpen() {
        boolean result = true;
        //Открыть IC карту в IC слоте с напряжением 3V
        int err = mIccManager.open(Slot, TYPE_IC, VOLT_3);
        //Проверить, удалось ли открыть карту
        if(err < 0){
            //Ошибка открытия
            Logger.info(TAG, "cscOpen() failed");
            result = false;
        }else{
            Logger.info(TAG, "cscOpen() completed");
        }
        //Проверить, в слоте ли карта
        int ret = mIccManager.detect();
        if(ret < 0){
            //Ошибка открытия
            Logger.info(TAG, "No card in slot");
            result = false;
        }else{
            Logger.info(TAG, "Card detected");
        }
        byte[] atr = new byte[64];
        //Активировать карту
        ret = mIccManager.activate(atr);
        if(ret == -1){
            //Ошибка открытия
            Logger.info(TAG, "Card activation failed");
            result = false;
        }else{
            Logger.info(TAG, "Card activate");
        }
        return result;
    }
    //Закрыть менеджер
    @Override
    public void cscClose() {
        if(mIccManager != null)
            mIccManager.close();
    }

    @Override
    public boolean mifareUlEv1IncrementCounter(byte add, @NonNull byte[] incrementValue) {
        return false;
    }

    @Override
    public boolean mifareUlIdentifyType(byte[] status) {
        return false;
    }

    @Override
    public boolean mifareSamNxpKillAuthentication() {
        return false;
    }

    @Override
    public boolean mifareSamNxpAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock) {
        long timer = System.currentTimeMillis();
        //номер блока для авторизации - любой блок в секторе, например нулевой
        byte blockNumber = 0;
        byte[] statusCard = new byte[1];
        short[] statusSam = new short[1];
        byte lgDiversifier = (byte) 0;
        byte blockDiversifier = (byte) 0;

        Commands.iMIFARE_SAMNXP_Authenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier);

        byte[] rspBuf = new byte[256];
        byte[] rspStatus = new byte[2];
        mIccManager.apduTransmit(Commands.giCSCTrame, Commands.giCSCTrame.length, rspBuf, rspStatus);
        //lastResult = mReader.mifareSamNxpAuthenticate(numKey, versionKey, keyAorB, numBlock, lgDiversifier, blockDiversifier, statusCard, statusSam);
        //boolean out = lastResult == Defines.RCSC_Ok && statusSam[0] == (short) 0x9000;



        /*addLog("mifareSamNxpAuthenticate(" +
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
                "} " + getResString(out) + getTimeString(timer));*/
        //return out;
        return false;
    }

    @Override
    public boolean mifareSamNxpReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock) {
        return false;
    }

    @Override
    public boolean mifareAuthenticate(byte numSector, byte keyAorB, byte keyIndex) {
        long timer = System.currentTimeMillis();

        byte[] mifareType = {0x00};
        byte[] serialNumber = new byte[4];
        byte[] status = new byte[1];

        Commands.iMIFARE_Authenticate(numSector, keyAorB, keyIndex);

        byte[] rspBuf = new byte[256];
        byte[] rspStatus = new byte[2];
        mIccManager.apduTransmit(Commands.giCSCTrame, Commands.giCSCTrame.length, rspBuf, rspStatus);
        int vRet = rspStatus[0];

        Logger.info(TAG, "RSP STATUS 0 " + String.valueOf(rspStatus[0]));
        Logger.info(TAG, "RSP STATUS 1 " + String.valueOf(rspStatus[1]));

        //lastResult = mReader.mifareAuthenticate(numSector, keyAorB, keyIndex, mifareType, serialNumber, status);
        boolean out = lastResult == Defines.RCSC_Ok && status[0] == 0;

        return false;
    }

    private void addLog(String text) {
        Logger.trace(TAG, text);
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

    private String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }
}
