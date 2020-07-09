package ru.ppr.rfidreal;

import fr.coppernic.cpcframework.cpcask.Defines;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.CardReadErrorType;
import ru.ppr.rfid.ForLegacyStaticKeyAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.SamAccessRule;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAccessRule;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

import android.content.Context;
import android.device.PiccManager;
import android.support.annotation.NonNull;

public class RfidReal_9000S implements IRfid {
    SearchCardRes scRes = new SearchCardRes();
    private static final int BYTE_IN_BLOCK = 16;
    public static final int BLOCK_IN_THE_SECTOR = 4;
    byte keyBuf[] = {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    };
    byte auth_cmd_Buf[] = {(byte)0xff, (byte)0x82, (byte)0x00, (byte)0x00, (byte)0x06};
    byte do_auth_cmd_Buf[] = {(byte)0xff, (byte)0x88, (byte)0x00, (byte)0x60, (byte)0x00};

    /**
     * Набор параметров для авторизации на карте. Необходимо обнулять при
     * смене карты.
     */
    private volatile AuthData cAuthData = new AuthData();
    /**
     * Флаг активности (включенности) ридера
     */
    private boolean is_mReaderOpen = false;
    private boolean isOpened = false;
    private AscReader_9000S mReader = null;
    private PiccManager piccReader = null;
    public static final String TAG = RfidReal_9000S.class.getSimpleName();

    private final RfidReal.Config config;
    public RfidReal_9000S(Context context, RfidReal.Config config){
        Logger.trace(TAG, "RfidReal_9000S() Constructor START");
        this.config = config;
        //Создать экземпляр класса для Ридера SAM карт
        mReader = new AscReader_9000S(context);
        if (piccReader == null){
            Logger.trace(TAG, "RfidReal_9000S() piccReader is creating");
            piccReader = new PiccManager();
        }
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }
    private String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

    private byte mapSamSlotNumber(int code) {
        if (code == 1) {
            return Defines.SAM_SLOT_2;
        } else {
            return Defines.SAM_SLOT_1;
        }
    }


    /**
     * Поиск карты. Необходимая команда для осуществления авторизации
     */
    private boolean searchCard() {
        return searchCard(new SearchCardRes());
    }
    /**
     * Переинициализирует SAM модуль
     */
    private boolean initSAM(byte samSlotNumber) {
        if (is_mReaderOpen) mReader.cscClose();
        mReader.setSlot(samSlotNumber);
        boolean res = mReader.cscOpen();
        return res;
    }

    private enum AuthType {
        mifareSamNxpAuthenticate, mifareSamNxpReAuthenticate;
    }

    private AuthType getAuthType(AuthData authData) {
        if (authData.isAuthSuccess() && !authData.isReAuthDisabled())
            return AuthType.mifareSamNxpReAuthenticate;
        return AuthType.mifareSamNxpAuthenticate;
    }

    private byte mapKeyName(int code) {
        if (code == 2) {
            return (byte) 0x0B;
        } else {
            return (byte) 0x0A;
        }
    }


    /**
     * базовый метод авторизации
     */
    private boolean authWithSam(final SamAccessRule schemeRule, AuthType authType) {
        String fTitle = "authWithSam(authType=" + authType + ") ";
        Logger.trace(getClass(), "authWithSam(schemeRule=" + schemeRule.toString() + ", authType=" + authType + ") " + "START");

        long time = getCurrentTime();
        //номер блока для авторизации - любой блок в секторе, например нулевой
        byte blockNumber = 0;
        byte address = (byte) (schemeRule.getSectorNumber() * BLOCK_IN_THE_SECTOR + blockNumber);

        // номер ключа на sam. 0-й ключ служебный, поэтому
        // инкрементируем
        byte samKeyNumber = (byte) (schemeRule.getCellNumber() + 1);
        byte samKeyVersion = schemeRule.getSamKeyVersion();
        byte samKeyName = mapKeyName(schemeRule.getKeyName());

        int res = -1;
        boolean out = false;

        if (authType == AuthType.mifareSamNxpReAuthenticate) {
            out = mReader.mifareSamNxpReAuthenticate(samKeyNumber, samKeyVersion, samKeyName, address);
        } else {
            out = mReader.mifareSamNxpAuthenticate(samKeyNumber, samKeyVersion, samKeyName, address);
        }
        Logger.trace(getClass(), fTitle + "FINISH result: " + ((out) ? "OK" : "FAILED") + getTimeString(time));
        return out;
    }

    private boolean authorizeWithSamAuthorizationStrategy(int sectorNumber, boolean read, SamAuthorizationStrategy samAuthorizationStrategy) {
        String fTitle = "authenticateWithSamAuthorizationStrategy(sectorNumber=" + sectorNumber + " ," + "read=" + read + ") ";

        Logger.trace(TAG, fTitle + "START");
        boolean out = false;
        long time = getCurrentTime();
//        return true;


        if (cAuthData.isAuthSuccess() && cAuthData.getLastAuthSector() == sectorNumber) {
            SamAccessRule samAccessRule = samAuthorizationStrategy.getKey(sectorNumber, read);
            if (samAccessRule != null) {
                if (samAccessRule.equals(cAuthData.getPreviousSamAccessSchemeRule())) {
                    Logger.trace(RfidReal.class, fTitle + "И так авторизованы для " + ((read) ? "чтения" : "записи") + " в этом секторе!");
                    out = true;
                } else {
                    out = false;
                }
            } else {
                out = false;
            }
        }

        if (!out) {
            SamAccessRule schemeRule;
            while ((schemeRule = samAuthorizationStrategy.getKey(sectorNumber, read)) != null) {
                //если прошлый раз использовался другой SAM или же его вообще еще не активировали - перезагрузим его:
                //также переинициализировать sam нужно если изменился keyVersion https://aj.srvdev.ru/browse/CPPKPP-27499
                if (!(schemeRule.getSamSlotNumber() == cAuthData.getSamSlot()) || !cAuthData.isSamActive() || cAuthData.getLastAuthKeyVersion() != schemeRule.getSamKeyVersion()) {
                    cAuthData.setIsSamActive(initSAM(mapSamSlotNumber(schemeRule.getSamSlotNumber())));
                    cAuthData.setIsReAuthDisabled(true);
                    cAuthData.setSamSlot(schemeRule.getSamSlotNumber());
                }

                out = authWithSam(schemeRule, getAuthType(cAuthData));
                cAuthData.setIsReAuthDisabled(false);
                if (out) {
                    //запомним информацию по схемам если не было ошибок
                    cAuthData.setPreviousSamAccessSchemeRule(schemeRule);
                    samAuthorizationStrategy.setLastSamAccessRuleStatus(out);
                    cAuthData.setLastAuthKeyVersion(schemeRule.getSamKeyVersion());
                    //авторизовались успешно - вываливаемся из цикла
                    break;
                } else {
                    //если была ошибка таймаута - тогда нет смысла продолжать
                    if (mReader.isTimeoutError()) {
                        break;
                    }
                    //если небыло ошибки таймаута, тогда запомним информацию по схемам
                    cAuthData.setPreviousSamAccessSchemeRule(schemeRule);
                    samAuthorizationStrategy.setLastSamAccessRuleStatus(out);
                    //Если ошиблись с ключем, тогда выполним обязательные операции для продолжения чтения
//                    killAuthAndResearch();
                }

                cAuthData.setIsAuthSuccess(false);
                cAuthData.setLastAuthSector(sectorNumber);
            }
        }

        cAuthData.setIsAuthSuccess(out);
        cAuthData.setLastAuthSector(sectorNumber);

        Logger.trace(RfidReal.class, fTitle + "FINISH result: " + out + getTimeString(time));

        return out;
    }

    private boolean loadKey(StaticKeyAccessRule staticKeyAccessRule) {
        String fTitle = "loadKey(key=" + CommonUtils.bytesToHexWithoutSpaces(staticKeyAccessRule.getKey()) + ") ";
        Logger.trace(RfidReal.class, fTitle + "START");
        long time = getCurrentTime();
        if (!cAuthData.isKeyLoaded() || !staticKeyAccessRule.equals(cAuthData.getPreviousStaticKeyAccessRule())) {
            boolean res = mReader.mifareLoadReaderKeyIndex((byte) 0, staticKeyAccessRule.getKey());
            cAuthData.setIsKeyLoaded(res);
        }
        Logger.trace(RfidReal.class, fTitle + "FINISH res: " + ((cAuthData.isKeyLoaded()) ? "OK" : "FAILED") + getTimeString(time));
        return cAuthData.isKeyLoaded();
    }


    private boolean authorizeWithStaticKeyAuthorizationStrategy(int sectorNumber, boolean read, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        String fTitle = "authorizeWithStaticKeyAuthorizationStrategy(sectorNumber=" + sectorNumber + " ," + "read=" + read + ") ";

        Logger.trace(RfidReal.class, fTitle + "START");
//        return true;
        long time = getCurrentTime();

        boolean out = false;

        if (cAuthData.isAuthSuccess() && cAuthData.getLastAuthSector() == sectorNumber) {
            StaticKeyAccessRule staticKeyAccessRule = staticKeyAuthorizationStrategy.getKey(sectorNumber, read);
            if (staticKeyAccessRule != null) {
                if (staticKeyAccessRule.equals(cAuthData.getPreviousStaticKeyAccessRule())) {
                    Logger.trace(RfidReal.class, fTitle + "И так авторизованы для " + ((read) ? "чтения" : "записи") + " в этом секторе!");
                    out = true;
                } else {
                    out = false;
                }
            } else {
                out = false;
            }
        }

        if (!out) {
            StaticKeyAccessRule staticKeyAccessRule;
            while ((staticKeyAccessRule = staticKeyAuthorizationStrategy.getKey(sectorNumber, read)) != null) {
                // без использования SAM модуля
                if (!cAuthData.isAuthSuccess()) {
                    getRfidAtr();
                }

                if (loadKey(staticKeyAccessRule)) {
                    byte addKeyPcd = 0x00;

                    //если была ошибка таймаута - тогда нет смысла продолжать
                    if (mReader.isTimeoutError()) {
                        break;
                    }
                    out = mReader.mifareAuthenticate((byte) sectorNumber, (byte) staticKeyAccessRule.getKeyName(), addKeyPcd);
                    staticKeyAuthorizationStrategy.setLastStaticKeyStatus(out);
                    if (out) {
                        //авторизовались успешно - вываливаемся из цикла
                        break;
                    } else {
                        //если была ошибка таймаута - тогда нет смысла продолжать
                        if (mReader.isTimeoutError())
                            break;
                        else{
                            //авторизовываться после этого нужно заново поэтому отключаем
                            cAuthData.setIsAuthSuccess(false);
                        }
                    }
                } else {
                    staticKeyAuthorizationStrategy.setLastStaticKeyStatus(false);
                }
            }
        }

        cAuthData.setIsAuthSuccess(out);
        cAuthData.setLastAuthSector(sectorNumber);

        Logger.trace(RfidReal.class, fTitle + "FINISH result: " + out + getTimeString(time));

        return out;
    }

    private RfidResult<byte[]> readFromClassic(int startSectorNumber,
                                               int startBlockNumber,
                                               int blockCount,
                                               SamAuthorizationStrategy samAuthorizationStrategy,
                                               StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {

        String functionTitle = "readFromClassic(startSectorNumber=" + startSectorNumber +
                ", startBlockNumber=" + startBlockNumber +
                ", blockCount=" + blockCount +
                ", samAuthorizationStrategy = " + samAuthorizationStrategy +
                ", staticKeyAuthorizationStrategy = " + staticKeyAuthorizationStrategy + ")";

        Logger.trace(TAG, functionTitle + "START");
        long time = getCurrentTime();
        RfidResult<byte[]> result = new RfidResult<>(CardReadErrorType.OTHER, "ошибка открытия ридера");
        if (open()) {
//            SearchCardRes scRes = new SearchCardRes();
            int res = piccReader.activateEx(scRes.atr);
            if (res == 0){

            int block = startBlockNumber;
            int sector = startSectorNumber;

            byte[] tmpArray = new byte[blockCount * BYTE_IN_BLOCK];

            for (int i = 0; i < blockCount; i++) {
                if (block >= 3) {
                    block = 0;
                    sector++;
                }
                Logger.trace(TAG, functionTitle + "readBlocksFromClassic: sector - " + sector + ", block - " + block);

                int address = (sector * BLOCK_IN_THE_SECTOR + block);

                if (samAuthorizationStrategy != null) {
                    if (authorizeWithSamAuthorizationStrategy(sector, true, samAuthorizationStrategy)) {
                        // for SAM
                        byte[] tmp = new byte[BYTE_IN_BLOCK];
//                        byte[] auth_cmd = new byte [BYTE_IN_BLOCK];
//                        byte[] sw = new byte[2];
//                        byte[] SN = new byte[16];
//                        System.arraycopy(auth_cmd_Buf, 0, auth_cmd, 0, 5);
//                        System.arraycopy(keyBuf, 0, auth_cmd, 5, 6);
//                        Logger.trace(TAG, "cmd =" + CommonUtils.bytesToHexWithoutSpaces(auth_cmd));
//                        int res1 = piccReader.apduTransmit(auth_cmd, 11, tmp, sw);
//                        int SNLen = -1;
                        Logger.trace(TAG, "UID = " + CommonUtils.bytesToHexWithoutSpaces(scRes.SN));
                        Logger.trace(TAG, "SNLen = " + scRes.SNLen);
                        int res1 = piccReader.m1_keyAuth(0, address, 6, keyBuf, scRes.SNLen, scRes.SN);
                        Logger.trace(TAG, "m1_keyAuth =" + res1);
//                           System.arraycopy(do_auth_cmd_Buf, 0, auth_cmd, 0, 5);
//                           auth_cmd[3] = (byte)address;
//                           res1 = piccReader.apduTransmit(auth_cmd, 6, tmp, sw);
//                           Logger.trace(TAG, "m2_keyAuth =" + res1 + " " + sw[0] + " " + sw[1]);
                        if (res1 >= 0){
//                        if (mReader.mifareSamNxpReadBlock((byte) address, tmp)) {
                            System.arraycopy(tmp, 0, tmpArray, i * BYTE_IN_BLOCK, BYTE_IN_BLOCK);
                        } else {
                            result = new RfidResult<>(CardReadErrorType.OTHER, "Error read from card");
                            Logger.trace(TAG, functionTitle + "FINISH res=" + result.getErrorMessage() + getTimeString(time));
                            return result;
                        }
                    } else {
                        result = new RfidResult<>(CardReadErrorType.AUTHORIZATION, "ошибка авторизации в секторе " + sector);
                        Logger.trace(TAG, functionTitle + "FINISH res=" + result.getErrorMessage() + getTimeString(time));
                        return result;
                    }
                } else {
                    if (authorizeWithStaticKeyAuthorizationStrategy(sector, true, staticKeyAuthorizationStrategy)) {
                        // for NO SAM
                        byte[] tmp = new byte[BYTE_IN_BLOCK];
//                        byte[] auth_cmd = new byte [BYTE_IN_BLOCK];
//                        byte[] sw = new byte[2];
//                        System.arraycopy(auth_cmd_Buf, 0, auth_cmd, 0, 5);
//                        System.arraycopy(keyBuf, 0, auth_cmd, 5, 6);
//                        int res1 = piccReader.apduTransmit(auth_cmd, 11, tmp, sw);
//                        int SNLen = 7;
                        Logger.trace(TAG, "SNLen = " + scRes.SNLen);
                        int res1 = piccReader.m1_keyAuth(0, address, 6, keyBuf, scRes.SNLen, scRes.SN);
                        Logger.trace(TAG, "m1_keyAuth =" + res1);
//                            System.arraycopy(do_auth_cmd_Buf, 0, auth_cmd, 0, 5);
//                        auth_cmd[3] = (byte)address;
//                        res1 = piccReader.apduTransmit(auth_cmd, 6, tmp, sw);
//                        Logger.trace(TAG, "m2_keyAuth =" + res1 + " " + sw[0] + " " + sw[1]);
                        if (res1 >= 0){
//                        if (mReader.mifareReadBlock((byte) address, tmp)) {
                            System.arraycopy(tmp, 0, tmpArray, i * BYTE_IN_BLOCK, BYTE_IN_BLOCK);
                        } else {
                            result = new RfidResult<>(CardReadErrorType.OTHER, "Error read from card");
                            Logger.trace(TAG, functionTitle + "FINISH res=" + result.getErrorMessage() + getTimeString(time));
                            return result;
                        }
                    } else {
                        result = new RfidResult<>(CardReadErrorType.AUTHORIZATION, "ошибка авторизации в секторе " + sector);
                        Logger.trace(TAG, functionTitle + "FINISH res=" + result.getErrorMessage() + getTimeString(time));
                        return result;
                    }
                }

                block++;
            }

            result = new RfidResult<>(tmpArray);
            final byte MODE = 0x00;
            piccReader.deactivate(MODE);
            }
            else Logger.trace(TAG, "ACTIVATE process failed");

//            softClose();
        }
        Logger.trace(TAG, functionTitle + "FINISH res=" + ((result.isOk()) ? "OK:\"" + CommonUtils.bytesToHexWithoutSpaces(result.getResult()) + "\"" : result.getErrorMessage()) + getTimeString(time));
        return result;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, SamAuthorizationStrategy samAuthorizationStrategy) {
        return readFromClassic(startSectorNumber, startBlockNumber, blockCount, samAuthorizationStrategy, null);
//        return null;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        return readFromClassic(startSectorNumber, startBlockNumber, blockCount, null, staticKeyAuthorizationStrategy);
//        return null;
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, SamAuthorizationStrategy samAuthorizationStrategy) {
//        return writeToClassic(startSectorNumber, startBlockNumber, data, cardUid, samAuthorizationStrategy, null);
        return null;
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
//        return writeToClassic(startSectorNumber, startBlockNumber, data, cardUid, null, staticKeyAuthorizationStrategy);
        return null;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber,
                                              SamAuthorizationStrategy samAuthorizationStrategy, boolean isUseSam) {
//        return readFromClassic(sectorNumber, blockNumber, DEFAULT_KEY, samAuthorizationStrategy, isUseSam);
        return null;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber, byte[] key,
                                              SamAuthorizationStrategy samAuthorizationStrategy,
                                              boolean isUseSam) {
        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy = new ForLegacyStaticKeyAuthorizationStrategy(key);
//        return readFromClassic(sectorNumber, blockNumber, 1, isUseSam ? samAuthorizationStrategy : null, staticKeyAuthorizationStrategy);
        return null;
    }

    @Override
    public WriteToCardResult writeToClassic(byte[] data, byte[] cardUID, int sector, int block,
                                            SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {
//        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy = new ForLegacyStaticKeyAuthorizationStrategy(DEFAULT_KEY);
//        return writeToClassic(sector, block, data, cardUID, useSamNxp ? samAuthorizationStrategy : null, staticKeyAuthorizationStrategy);
        return null;
    }

    /**
     * Очищает картозависимые данные, ускоряющие процесс поиска. Вызывать каждый
     * раз перед чтением данных с новой карты
     */
    @Override
    public void clearAuthData() {
        Logger.trace(TAG, "clearAuthData()");
        cAuthData = new AuthData();
    }

    @Override
    public RfidResult<byte[]> readFromUltralight(byte pageNumber, byte startByte, byte length) {
/*        String functionTitle = "readFromUltralight(pageNumber = " + pageNumber +
                ", startByte = " + startByte +
                ", length = " + length + " ";

        Logger.trace(RfidReal.class, functionTitle + "START");
        long time = getCurrentTime();
        RfidResult<byte[]> rfidResult;
        if (open()) {

            byte mfUlType = cAuthData.getLastCardData().getMifareUlIdentifyType();
            byte address = (byte) (pageNumber * 4 + startByte);
            byte[] data = new byte[length];

            if (mReader.mifareUlRead(mfUlType, address, length, data)) {
                rfidResult = new RfidResult<>(data);
            } else {
                rfidResult = new RfidResult<>(CardReadErrorType.OTHER, "Error read Ultralight");
            }
            softClose();
        } else {
            rfidResult = new RfidResult<>(CardReadErrorType.OTHER, "Error open ReaderPort");
        }
        Logger.trace(RfidReal.class, functionTitle + "FINISH res: " + rfidResult.getErrorMessage() + " " + getTimeString(time));
        return rfidResult;*/
        return null;
    }

    @NonNull
    @Override
    public RfidResult<byte[]> readCounterFromUltralightEV1(int counterIndex) {
/*        Logger.trace(getClass(), "readCounterFromUltralightEV1(counterIndex=" + counterIndex + ") START");
        long time = getCurrentTime();
        RfidResult<byte[]> rfidResult;
        if (open()) {
            byte length = 0x3; // Счетчик 3-байтный (24-битный)
            byte[] counterValue = new byte[length];

            if (!mReader.mifareUlEv1ReadCounter((byte) counterIndex, length, counterValue)) {
                final String message = "Read data from UltralightEV1 done Error";
                rfidResult = new RfidResult<>(CardReadErrorType.OTHER, message);
            } else {
                rfidResult = new RfidResult<>(counterValue);
            }

            softClose();
        } else {
            rfidResult = new RfidResult<>(CardReadErrorType.OTHER, "Error open ReaderPort");
        }
        Logger.trace(getClass(), "readCounterFromUltralightEV1(counterIndex=" + counterIndex + ") FINISH res:" + rfidResult.getErrorMessage() + getTimeString(time));
        return rfidResult;*/
        return null;
    }

    /**
     * Производит запист на карты ультралайт
     *
     * @param data    записываеммые данные
     * @param cardUID ид карты на которую гарантированно необходимо произвести
     *                запись, если передали null, то записываем на любую приложенную
     *                карту
     * @param page    номер страницы, с которой необходимо записать данные
     * @return
     */
    @Override
    public WriteToCardResult writeToUltralight(byte[] data, byte[] cardUID, int page) {

/*        String functionTitle = "writeToUltralight(data=" + CommonUtils.bytesToHexWithoutSpaces(data) +
                ", cardUID=" + CommonUtils.bytesToHexWithoutSpaces(cardUID) +
                ", page=" + page + ") ";

        Logger.trace(RfidReal.class, functionTitle + "START");
        long time = getCurrentTime();

        WriteToCardResult result = WriteToCardResult.SUCCESS;

        //здесь просто подаем питание поскольку этот метод выполняется 5мс, в то время как метод isOpened, который запрашивает версию ридера выполняется 12мс
        if (!open()) {
            result = WriteToCardResult.WRITE_ERROR;
        }

        if (result == WriteToCardResult.SUCCESS) {
            CardData cData = getRfidAtr();
            if (cData != null) {
                if (cardUID != null) {
                    if (!Arrays.equals(cData.getCardUID(), cardUID)) {
                        result = WriteToCardResult.UID_DOES_NOT_MATCH;
                    }
                }

                if (result == WriteToCardResult.SUCCESS) {

                    // домнoжаем номер страницы на 4 для определения номера байта с которого
                    // необходимо начать запись
                    if (mReader.mifareUlWrite(cData.getMifareUlIdentifyType(), page, data))
                        result = WriteToCardResult.SUCCESS;
                    else
                        result = WriteToCardResult.WRITE_ERROR;
                }

            } else {
                result = WriteToCardResult.WRITE_ERROR;
            }
        }
        softClose();
        Logger.trace(RfidReal.class, functionTitle + "FINISH res:" + result + getTimeString(time));
        return result;*/
        return null;
    }

    @NonNull
    @Override
    public WriteToCardResult incrementCounterUltralightEV1(int counterIndex, int incrementValue, byte[] cardUid) {

/*        String functionTitle = "incrementCounterUltralightEV1(counterIndex = " + counterIndex +
                ", cardUid = " + CommonUtils.bytesToHexWithSpaces(cardUid) + ") ";
        Logger.trace(RfidReal.class, functionTitle + "START");
        long time = getCurrentTime();
        WriteToCardResult result = WriteToCardResult.SUCCESS;

        if (!open()) {
            result = WriteToCardResult.WRITE_ERROR;
        } else {

            CardData cardData = getRfidAtr();
            if (cardData != null) {
                if (cardUid != null) {
                    if (!Arrays.equals(cardData.getCardUID(), cardUid)) {
                        result = WriteToCardResult.UID_DOES_NOT_MATCH;
                    }
                }

                if (result == WriteToCardResult.SUCCESS) {
                    byte[] incrementValueBytes = new byte[3]; //3 байта = 24 бита, т.к. счётчики 24-битные
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(incrementValue).position(0);
                    byteBuffer.get(incrementValueBytes);
                    if (!mReader.mifareUlEv1IncrementCounter((byte) counterIndex, incrementValueBytes)) {
                        result = WriteToCardResult.WRITE_ERROR;
                    }
                }
            } else {
                result = WriteToCardResult.WRITE_ERROR;
            }
        }
        softClose();
        Logger.trace(RfidReal.class, functionTitle + "FINISH res:" + result + getTimeString(time));
        return result;
*/
        return null;
    }

    @Override
    public WriteToCardResult deleteDataFromClassic(byte[] cardUID, byte startSector, byte startBlock,
                                                   int countBlockForDelete, SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {

/*
        String fTitle = "deleteDataFromClassic(uid card = " + CommonUtils.bytesToHexWithSpaces(cardUID) +
                ", start sector = " + startSector +
                ", start block = " + startBlock +
                ", count block for delete = " + countBlockForDelete +
                ", card type = " + samAuthorizationStrategy + ") ";
        Logger.trace(RfidReal.class, fTitle + "START");
        long time = getCurrentTime();

        WriteToCardResult result = WriteToCardResult.SUCCESS;
        if (open()) {

            byte currentSectorForDelete = startSector;
            byte currentBlockForDelete = startBlock;

            for (int i = 0; i < countBlockForDelete; i++) {
                WriteToCardResult writeToCardResult = writeToClassic(DATA_FOR_DELETE, cardUID,
                        currentSectorForDelete, currentBlockForDelete, samAuthorizationStrategy, useSamNxp);
                currentBlockForDelete++;
                if (currentBlockForDelete == 3) {
                    currentBlockForDelete = 0;
                    currentSectorForDelete++;
                }
                // если блок записался с ошибкой то сохраняем тип ошибки и выходим
                // из цикла
                if (writeToCardResult != WriteToCardResult.SUCCESS) {
                    result = writeToCardResult;
                    break;
                }
            }
            softClose();
        } else
            result = WriteToCardResult.UNKNOWN_ERROR;
        Logger.trace(RfidReal.class, fTitle + "FINISH res: " + result.name() + getTimeString(time));
        return result;*/
        return null;
    }

    @Override
    public boolean open() {
        Logger.trace(TAG, "open() START");
        synchronized (RfidReal_9000S.this) {
            Logger.trace(TAG, "open() 1");
            if (piccReader == null) piccReader = new PiccManager();
            if (piccReader != null) {
                Logger.trace(TAG, "open() 2");
                if (!isOpened()) {
                    Logger.trace(TAG, "open() 3");
                    isOpened = (piccReader.open() == 0);
                    Logger.trace(TAG, "open() = " + isOpened);

                    if (!isOpened()) close();
                    else{
                        mReader.setSlot(Defines.SAM_SLOT_1);
                        is_mReaderOpen = mReader.cscOpen();
                    }
                }
            }
        }
        Logger.trace(TAG, "open() FINISH res=" + isOpened);
        return isOpened;
    }

    @Override
    public void close() {
        Logger.trace(TAG, "close() START");
        if (is_mReaderOpen){
            mReader.cscClose();
            is_mReaderOpen = false;
        }
        if (piccReader != null)
            piccReader.close();
        piccReader = null;
        isOpened = false;
        Logger.trace(TAG, "close() FINISH");
    }

    @Override
    public boolean isOpened() {
        Logger.trace(TAG, "isOpened() FINISH res=" + isOpened);
        return isOpened;
    }

    @Override
    public boolean getModel(String[] model) {
        model[0] = "RFID HF Reader - i9000S";
        return true;
    }

    @Override
    public boolean getFWVersion(String[] version) {
        Logger.trace(TAG, "getFWVersion() START");
        boolean out = getModel(version);
        return out;
    }

    private static class SearchCardRes {
        byte[] com = new byte[1];
        int[] atrLength = new int[1];
        int scanCard = -1;
        byte[] CardType = new byte[2];
        byte[] Atq = new byte[14];
        byte[] atr = new byte[32];
        byte SAK = 1;
        byte sak[] = new byte[1];
        byte SN[] = new byte[10];
        int SNLen = -1;
    }

    private boolean searchCard(SearchCardRes scRes) {
        Logger.trace(TAG, "searchCard() START");
        scRes.scanCard = piccReader.request(scRes.CardType, scRes.Atq);
//        boolean out = mReader.cscSearchCardExt(scRes.com, scRes.atrLength, scRes.atr);
        if (scRes.scanCard > 0) {
            scRes.SNLen = piccReader.antisel(scRes.SN, scRes.sak);
        }
        return (scRes.SNLen > 0);
    }

    private byte[] getUID(SearchCardRes scRes) {
        Logger.trace(TAG, "getUID() START");
        byte[] out = new byte[10];
        System.arraycopy(scRes.SN, 0, out, 0, scRes.SNLen);
        Logger.trace(TAG, "getUID() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out));
        return out;
    }

    private byte[] getATQA(SearchCardRes scRes) {
        Logger.trace(TAG, "getATQA() START");
        byte[] out = new byte[14];
        System.arraycopy(scRes.Atq, 0, out, 0, 14);
        Logger.trace(getClass(), "getATQA() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out));
        return out;
    }

    private byte[] getSAK(SearchCardRes scRes) {
        Logger.trace(TAG, "getSAK() START");
        byte[] out = new byte[1];
        System.arraycopy(scRes.sak, 0, out, 0, 1);
        Logger.trace(getClass(), "getSAK() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out));
        return out;
    }

    private CardData getCardData(SearchCardRes scRes) {
        Logger.trace(TAG, "getCardData() START");
//        byte[] realRfidAttr = new byte[scRes.atrLength[0]];
//        System.arraycopy(scRes.atr, 0, realRfidAttr, 0, scRes.atrLength[0]);
        byte[] realRfidAttr = new byte[32];
        System.arraycopy(scRes.atr, 0, realRfidAttr, 0, 32);
        Logger.info(TAG, "Atq1 = " + scRes.Atq[0] + " " + scRes.Atq[1] + " " + scRes.Atq[2] + " " + scRes.Atq[3] + " " + scRes.Atq[4] + " " + scRes.Atq[5] + " " + scRes.Atq[6] + " " + scRes.Atq[7] + " " + scRes.Atq[8] + " " + scRes.Atq[9]);
        Logger.info(TAG, "Atq2 = " + scRes.Atq[10] + " " + scRes.Atq[11] + " " + scRes.Atq[12] + " " + scRes.Atq[13]);
        Logger.info(TAG, "scRes.scanCard = " + scRes.scanCard);
        Logger.info(TAG, "scRes.CardType = " + scRes.CardType[0] + " "  + scRes.CardType[1]);
        Logger.info(TAG, "scRes.SNLen = " + scRes.SNLen);
        Logger.info(TAG, "scRes.SN = " + scRes.SN[0] + " " + scRes.SN[1] + " " + scRes.SN[2] + " " + Integer.toHexString(scRes.SN[3]) + " " + Integer.toHexString(scRes.SN[4]) + " " + scRes.SN[5] + " " + Integer.toHexString(scRes.SN[6]) + " " + scRes.SN[7] + " " + scRes.SN[8] + " " + scRes.SN[9]);
        Logger.info(TAG, "scRes.sak = " + scRes.sak[0]);
        Logger.info(TAG, "scRes.SAK = " + scRes.SAK);

        Logger.info(TAG, "ATR1 = " + scRes.atr[0] + " " + scRes.atr[1] + " " + scRes.atr[2] + " " + scRes.atr[3] + " " + scRes.atr[4] + " " + scRes.atr[5] + " " + scRes.atr[6] + " " + scRes.atr[7] + " " + scRes.atr[8] + " " + scRes.atr[9]);
        Logger.info(TAG, "ATR2 = " + scRes.atr[10] + " " + scRes.atr[11] + " " + scRes.atr[12] + " " + scRes.atr[13] + " " + scRes.atr[14] + " " + scRes.atr[15] + " " + scRes.atr[16] + " " + scRes.atr[17] + " " + scRes.atr[18] + " " + scRes.atr[19]);
        Logger.info(TAG, "ATR3 = " + scRes.atr[20] + " " + scRes.atr[21] + " " + scRes.atr[22] + " " + scRes.atr[23] + " " + scRes.atr[24] + " " + scRes.atr[25] + " " + scRes.atr[26] + " " + scRes.atr[27] + " " + scRes.atr[28] + " " + scRes.atr[29]);
        Logger.info(TAG, "ATR4 = " + scRes.atr[30] + " " + scRes.atr[31]);

        CardData cardData = new CardData();
        cardData.setRfidAttr(realRfidAttr);
        cardData.setCardUID(getUID(scRes));
//        cardData.setCom(scRes.com);
//        cardData.setMifareUlIdentifyType(getUlIdentifyType());
        cardData.setAtqa(getATQA(scRes));
        cardData.setSak(getSAK(scRes));

        MifareCardTypeRecognizer mifareCardTypeRecognizer = new MifareCardTypeRecognizer();

        //на этом этапе уже можно отличить Ultralight от Classic
        if (mifareCardTypeRecognizer.getMifareCardType(cardData).isUltralight()) {
/*            cardData.setEv1(mifareUlEv1GetVersion());
            if (!(cardData.isEv1())) {
                //если карта не Ev1 нужно заново активировать ее
                boolean res = searchCard(scRes);
                if (!res)
                    Logger.error(TAG, "getCardData() ошибка поиска карты после команды определения принадлежности к Ev1");
            }*/

        }
        //А тут мы уже отличаем разные типы Ultralight
        cardData.setMifareCardType(mifareCardTypeRecognizer.getMifareCardType(cardData));

        Logger.trace(TAG, "getCardData() FINISH");
        return cardData;
    }

    @Override
    public CardData getRfidAtr() {
        Logger.trace(TAG, "getRfidAtr() START");
        CardData cardData = null;
        if (open()) {
            Logger.trace(TAG, "getRfidAtr() Reader is opened");

            // так сделано в приложении-примере PICCManager
            scRes.sak[0] = scRes.SAK;
            boolean res = searchCard(scRes);

            if (res) {
                //get card data ATQA, SAK, UltralightType, parse ATR
                cardData = getCardData(scRes);
                //Если до этого была другая карта - то почистим историю
                if (!cAuthData.getLastCardData().equals(cardData)) {
                    clearAuthData();
                }
                //прихраниваем чтобы знать тип карты
                cAuthData.setLastCardData(new CardData(cardData));
                //инитить SAM после этого нужно заново
                cAuthData.setIsSamActive(false);
                //авторизовываться после этого нужно заново поэтому отключаем
                cAuthData.setIsAuthSuccess(false);
            }
//            softClose();
        }
        Logger.trace(TAG, "getRfidAtr() FINISH " + ((cardData != null) ? "OK" : "NOT FOUND"));
        return cardData;
    }
}
