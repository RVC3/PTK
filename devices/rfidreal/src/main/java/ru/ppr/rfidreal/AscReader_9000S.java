package ru.ppr.rfidreal;

import android.content.Context;
import android.device.IccManager;
import android.support.annotation.NonNull;

import ru.ppr.logger.Logger;

/**
 * Реализация обертки над считывателем смарт-карт от Coppernic SDK для i9000S
 *
 * @author A. Kopanev
 */

public class AscReader_9000S implements IAscReader {

    private static final String TAG = AscReader_9000S.class.getSimpleName();

    public static final byte SLOT_IC = 0x00;
    public static final byte SLOT_SAM1 = 0x01;
    public static final byte SLOT_SAM2 = 0x02;
    public static final byte TYPE_IC = 0x01;
    public static final byte TYPE_SLE4442 = 0x02;
    public static final byte VOLT_3 = 0x01;

    IccManager mIccManager = null;

    public AscReader_9000S(Context context) {
        Logger.info(TAG, "AscReader_9000s() constructor START");

        //Создать экземпляр класса IccManager
        mIccManager = new IccManager();

        Logger.info(TAG, "AscReader_9000S() constructor FINISH");
    }

    @Override
    public void setPower(boolean on) {

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
        int err = mIccManager.open(SLOT_SAM1, TYPE_IC, VOLT_3);
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
        return false;
    }

    @Override
    public boolean mifareSamNxpReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock) {
        return false;
    }

    @Override
    public boolean mifareAuthenticate(byte numSector, byte keyAorB, byte keyIndex) {
        return false;
    }
}
