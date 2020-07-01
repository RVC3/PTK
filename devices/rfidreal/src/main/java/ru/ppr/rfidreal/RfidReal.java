package ru.ppr.rfidreal;

import android.content.Context;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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

/**
 * Реализация считывателя смарт-карт, работающая на встроенном ридере Coppercic C-One.
 *
 * @author Ушаков Артем.
 */
public class RfidReal implements IRfid {

    private static final int BYTE_IN_BLOCK = 16;

    /**
     * Время проходящее с момента подачи питиния на ридер, до его реального включения, ms
     */
    public static final long POWER_ON_DELAY = 600;

    /**
     * Время проходящее с момента отключения питания ридера, до его реального выключения, ms
     */
    public static final long POWER_OFF_DELAY = 1000;

    /**
     * Максимальное количество попыток проверок включенности ридера после команды power-> open
     */
    public static final int MAX_CHECK_OPEN_COUNT = 5;

    /**
     * Задержка между попытками получить версию идера для контроля реальной включенности ридера
     */
    public static final int CHECK_OPEN_ITEM_DELAY = 100;

    public static final int BLOCK_IN_THE_SECTOR = 4;

    public static final String TAG = RfidReal.class.getSimpleName();

    /**
     * Ключ транспортных карт
     */
    private static final byte[] DEFAULT_KEY = {(byte) 0xA5, (byte) 0xA4, (byte) 0xA3, (byte) 0xA2,
            (byte) 0xA1, (byte) 0xA0};
    /**
     * Ключ А от секторов 7,8,9 от зеленой карты
     */
    private static final byte[] DEFAULT_KEY_GREEN_CARD = {(byte) 0xAA, (byte) 0xF9, (byte) 0xE7,
            (byte) 0x9A, (byte) 0x1E, (byte) 0xFB};

    private static final byte[] DATA_FOR_DELETE = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private AscReader mReader = null;

    /**
     * Флаг наличия питания на ножках
     */
    private boolean isPowerOn = false;

    /**
     * Флаг активности (включенности) ридера
     */
    private boolean isOpened = false;

    /**
     * Набор параметров для авторизации на карте. Необходимо обнулять при
     * смене карты.
     */
    private volatile AuthData cAuthData = new AuthData();

    /**
     * Таймер отложенного выключения питания ридера
     */
    private final Timer timer;
    private TimerTask timerTask;

    private final Config config;

    public RfidReal(Context context, Config config) {
        timer = new Timer("RfidRealPower");
        mReader = new AscReader(context);
        this.config = config;
    }

    /**
     * Powers on RfidReal reader and opens the serial communication.
     */
    @Override
    public boolean open() {
        Logger.trace(TAG, "open() START");
        long time = getCurrentTime();
        boolean out = false;
        synchronized (RfidReal.this) {
            cancelPowerOffTimer();
            if (mReader != null) {
                power(true, POWER_ON_DELAY);
                boolean resOpen = isOpened;
                if (!isOpened)
                    resOpen = mReader.cscOpen();
                if (resOpen)
                    out = checkOpenState();
                //если так и не взлетело - выключим ридер
                if (!out) {
                    hardClose(POWER_OFF_DELAY);
                }
            }
        }
        Logger.trace(TAG, "open() FINISH res=" + out + getTimeString(time));
        return out;
    }

    /**
     * Сбрасывает таймер отложенного выключения
     */
    private void cancelPowerOffTimer() {
        Logger.trace(TAG, "cancelPowerOffTimer()");
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
            timer.purge();
        }
    }

    @Override
    public void close() {
        Logger.trace(TAG, "close() START");
        long time = getCurrentTime();
        hardClose(POWER_OFF_DELAY);
        Logger.trace(TAG, "close() FINISH" + getTimeString(time));
    }

    @Override
    public boolean isOpened() {
        Logger.trace(TAG, "isOpened() START");
        long time = getCurrentTime();
        boolean out = isOpened;
        Logger.trace(TAG, "isOpened() FINISH res=" + out + getTimeString(time));
        return out;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, SamAuthorizationStrategy samAuthorizationStrategy) {
        return readFromClassic(startSectorNumber, startBlockNumber, blockCount, samAuthorizationStrategy, null);
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        return readFromClassic(startSectorNumber, startBlockNumber, blockCount, null, staticKeyAuthorizationStrategy);
    }

    private RfidResult<byte[]> readFromClassic(int startSectorNumber,
                                               int startBlockNumber,
                                               int blockCount,
                                               SamAuthorizationStrategy samAuthorizationStrategy,
                                               StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {

        String functionTitle = "readFromClassic(startSectorNumber=" + startSectorNumber +
                ", startBlockNumber=" + startBlockNumber +
                ", blockCount=" + blockCount + ") ";

        Logger.trace(RfidReal.class, functionTitle + "START");
        long time = getCurrentTime();
        RfidResult<byte[]> result = new RfidResult<>(CardReadErrorType.OTHER, "ошибка открытия ридера");
        if (open()) {

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
                        if (mReader.mifareSamNxpReadBlock((byte) address, tmp)) {
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
                        if (mReader.mifareReadBlock((byte) address, tmp)) {
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

            softClose();
        }
        Logger.trace(TAG, functionTitle + "FINISH res=" + ((result.isOk()) ? "OK:\"" + CommonUtils.bytesToHexWithoutSpaces(result.getResult()) + "\"" : result.getErrorMessage()) + getTimeString(time));
        return result;
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, SamAuthorizationStrategy samAuthorizationStrategy) {
        return writeToClassic(startSectorNumber, startBlockNumber, data, cardUid, samAuthorizationStrategy, null);
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        return writeToClassic(startSectorNumber, startBlockNumber, data, cardUid, null, staticKeyAuthorizationStrategy);
    }

    private WriteToCardResult writeToClassic(int startSectorNumber,
                                             int startBlockNumber,
                                             byte[] data,
                                             byte[] cardUid,
                                             SamAuthorizationStrategy samAuthorizationStrategy,
                                             StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        String functionTitle = "writeToClassic(data=" + CommonUtils.bytesToHexWithoutSpaces(data) +
                ", cardUID=" + CommonUtils.bytesToHexWithoutSpaces(cardUid) +
                ", startSectorNumber=" + startSectorNumber +
                ", startBlockNumber=" + startBlockNumber + " ";

        Logger.trace(RfidReal.class, functionTitle + "START");
        long time = getCurrentTime();
        WriteToCardResult result = WriteToCardResult.SUCCESS;
        if (!open())
            result = WriteToCardResult.WRITE_ERROR;
        else {
            //удалось открыть ридер
            SearchCardRes scRes = new SearchCardRes();
            if (searchCard(scRes)) {
                //команда поиска ломает возможность записи, нужно убить авторизацию
                mReader.mifareSamNxpKillAuthentication();
                //и сбросить этот флажек чтобы авторизация выолнилась методом Auth а не ReAuth
                cAuthData.setIsAuthSuccess(false);
                if (!checkUID(cardUid, scRes))
                    result = WriteToCardResult.UID_DOES_NOT_MATCH;
            } else
                result = WriteToCardResult.CAN_NOT_SEARCH_CARD;

            if (result == WriteToCardResult.SUCCESS) { //удалось открыть ридер

                int currentSector = startSectorNumber;
                int currentBlock = startBlockNumber;

                // определяем количество блоков, необходимых для записи данных
                double tmp = Math.ceil(data.length / (double) BYTE_IN_BLOCK);
                int countBlock = Double.valueOf(tmp).intValue();

                // Расширяем массив незначащими нулями, чтобы не высчитывать в цикле
                // количество байт которое нужно скопировать в массив для записи
                // и не получить выход за пределы массива
                byte[] extendedData = new byte[countBlock * BYTE_IN_BLOCK];
                System.arraycopy(data, 0, extendedData, 0, data.length);

                // в цикле записываем каждый блок отдельно
                for (int i = 0; i < countBlock; i++) {

                    // готовим данные
                    byte[] writeData = new byte[BYTE_IN_BLOCK];
                    System.arraycopy(extendedData, i * BYTE_IN_BLOCK, writeData, 0,
                            BYTE_IN_BLOCK);

                    // чтобы не записать в блок с ключами, корректируем номер блока
                    // и сектора
                    if (currentBlock == 3) {
                        currentBlock = 0;
                        currentSector++;
                    }

                    byte address = (byte) (currentSector * BLOCK_IN_THE_SECTOR + currentBlock);

                    if (samAuthorizationStrategy != null) {
                        if (authorizeWithSamAuthorizationStrategy(currentSector, false, samAuthorizationStrategy)) {
                            // for SAM
                            if (!mReader.mifareSamNxpWriteBlock(address, writeData)) {
                                result = WriteToCardResult.WRITE_ERROR;
                            }
                        } else {
                            result = WriteToCardResult.WRITE_ERROR;
                            return result;
                        }
                    } else {
                        if (authorizeWithStaticKeyAuthorizationStrategy(currentSector, false, staticKeyAuthorizationStrategy)) {
                            // for NO SAM
                            if (!mReader.mifareWriteBlock(address, writeData)) {
                                result = WriteToCardResult.WRITE_ERROR;
                            }
                        } else {
                            result = WriteToCardResult.WRITE_ERROR;
                        }
                    }

                    if (result == WriteToCardResult.SUCCESS) {
                        // увеличиваем номер блока для записи
                        currentBlock++;
                    } else {
                        break;
                    }
                }
            }
            softClose();
        }
        Logger.trace(RfidReal.class, functionTitle + "FINISH res: " + result.name() + getTimeString(time));
        return result;
    }

    /**
     * Производит чтение с карты Mifare Classic по стандартному ключу
     *
     * @param sectorNumber номер сектора для чтения
     * @param blockNumber  номер блока для чтения
     * @return считанные данные
     */
    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber,
                                              SamAuthorizationStrategy samAuthorizationStrategy, boolean isUseSam) {
        return readFromClassic(sectorNumber, blockNumber, DEFAULT_KEY, samAuthorizationStrategy, isUseSam);
    }

    /**
     * Производит чтение с карты Mifare Classic с заданным ключом
     *
     * @param sectorNumber             - номер сектора для чтения
     * @param blockNumber              - номер блока для чтения
     * @param key                      - ключ, с помощью которого авторизовываемся в секторе, может
     *                                 быть null, если читаем с помощью sam
     * @param samAuthorizationStrategy - тип считываемой карты, например CPPK, может быть null
     * @return считанные данные
     */
    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber, byte[] key,
                                              SamAuthorizationStrategy samAuthorizationStrategy,
                                              boolean isUseSam) {
        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy = new ForLegacyStaticKeyAuthorizationStrategy(key);
        return readFromClassic(sectorNumber, blockNumber, 1, isUseSam ? samAuthorizationStrategy : null, staticKeyAuthorizationStrategy);
    }


    /**
     * Проверит приложили ли карту с этим uid
     *
     * @param uid
     * @return
     */
    private boolean checkUID(byte[] uid, SearchCardRes scRes) {
        Logger.trace(RfidReal.class, "checkUID(uid=" + CommonUtils.bytesToHexWithoutSpaces(uid) + ") START");
        long time = getCurrentTime();
        boolean out = false;
        byte[] newUID = getUID(scRes);
        if (newUID != null) {
            if (uid != null) {
                if (Arrays.equals(newUID, uid)) {
                    out = true;
                }
            } else out = true;
        }
        Logger.trace(RfidReal.class, "checkUID(uid=" + CommonUtils.bytesToHexWithoutSpaces(uid) + ") FINISH res=" + out + getTimeString(time));
        return out;
    }


    /**
     * ПРоизводит запись на карту Mifare Classic 1k
     *
     * @param data                     - массив данных для записи
     * @param cardUID                  - ид карты на которую гарантированно необходимо произвести
     *                                 запись, если передаем null, то записываем на любую поднесенную
     *                                 карту
     * @param sector                   - ceктор с которого стоит начинать запись
     * @param block                    - блок с которого стоит начинать запись
     * @param samAuthorizationStrategy - тип считываемой карты, например CPPK, может быть null
     * @return код ошибки
     */
    @Override
    public WriteToCardResult writeToClassic(byte[] data, byte[] cardUID, int sector, int block,
                                            SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {
        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy = new ForLegacyStaticKeyAuthorizationStrategy(DEFAULT_KEY);
        return writeToClassic(sector, block, data, cardUID, useSamNxp ? samAuthorizationStrategy : null, staticKeyAuthorizationStrategy);
    }

    /**
     * Очищает картозависимые данные, ускоряющие процесс поиска. Вызывать каждый
     * раз перед чтением данных с новой карты
     */
    @Override
    public void clearAuthData() {
        Logger.trace(getClass(), "clearAuthData()");
        cAuthData = new AuthData();
    }

    /**
     * Производит чтение с карт льтралайт
     *
     * @param pageNumber номер страницы
     * @param startByte  стартовый байт в странице
     * @param length     длина данных, которые необходимо считать
     * @return
     */
    @Override
    public RfidResult<byte[]> readFromUltralight(byte pageNumber, byte startByte, byte length) {
        String functionTitle = "readFromUltralight(pageNumber = " + pageNumber +
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
        return rfidResult;
    }

    private class UlDataToWrite {
        byte mfUlType;
        byte address;
        byte length;
        byte[] data;
        byte[] readCountBytes = new byte[1];
        byte[] tmpArray = new byte[1];

        private String getString() {
            return "{mfUlType=" + CommonUtils.byteToHex(mfUlType) + ", " +
                    "address=" + CommonUtils.byteToHex(address) + ", " +
                    "length=" + CommonUtils.byteToHex(length) + ", " +
                    "data=" + CommonUtils.bytesToHexWithoutSpaces(data) + ", " +
                    "readCountBytes=" + CommonUtils.bytesToHexWithoutSpaces(readCountBytes) + ", " +
                    "tmpArray=" + CommonUtils.bytesToHexWithoutSpaces(tmpArray) + "}";
        }
    }

    @NonNull
    @Override
    public RfidResult<byte[]> readCounterFromUltralightEV1(int counterIndex) {
        Logger.trace(getClass(), "readCounterFromUltralightEV1(counterIndex=" + counterIndex + ") START");
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
        return rfidResult;
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

        String functionTitle = "writeToUltralight(data=" + CommonUtils.bytesToHexWithoutSpaces(data) +
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
        return result;
    }

    @NonNull
    @Override
    public WriteToCardResult incrementCounterUltralightEV1(int counterIndex, int incrementValue, byte[] cardUid) {

        String functionTitle = "incrementCounterUltralightEV1(counterIndex = " + counterIndex +
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

    }

    /**
     * Возвращает номер типа карты
     *
     * @return
     */
    private byte getUlIdentifyType() {
        Logger.trace(RfidReal.class, "getUlIdentifyType() START");
        long time = getCurrentTime();
        byte[] uiType = new byte[1];
        if (!mReader.isTimeoutError())
            mReader.mifareUlIdentifyType(uiType);
        Logger.trace(RfidReal.class, "getUlIdentifyType() FINISH return: " + uiType[0] + getTimeString(time));
        return uiType[0];
    }

    /**
     * Определяет принадлежность Ultralight карты к Ev1 семейству
     *
     * @return
     */
    private boolean mifareUlEv1GetVersion() {
        Logger.trace(RfidReal.class, "mifareUlEv1GetVersion() START");
        boolean isEv1 = false;
        long time = getCurrentTime();
        byte[] status = new byte[1];
        byte[] information = new byte[8];
        if (!mReader.isTimeoutError())
            isEv1 = mReader.mifareUlEv1GetVersion(status, information);
        Logger.trace(RfidReal.class, "mifareUlEv1GetVersion() FINISH return: " + status[0] + getTimeString(time));
        return isEv1;
    }

    @Override
    public WriteToCardResult deleteDataFromClassic(byte[] cardUID, byte startSector, byte startBlock,
                                                   int countBlockForDelete, SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {


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
        return result;
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

    /**
     * Переинициализирует SAM модуль
     */
    private boolean initSAM(byte samSlotNumber) {
        boolean res = selectAndResetSAM(samSlotNumber);
        //выполним набор обязательных команд после резета SAM
        if (res) {
            res = res && mReader.cscEnterHuntPhaseParameters();
            if (res)
                res = res && searchCard();
        }
        return res;
    }

    /**
     * Метод удаляет авторизацю, должен быть вызван каждый раз после неудачной авторизации, чтобы следующая авторизация прошла успешно
     */
    private boolean killAuthAndResearch() {
        boolean resKill = false;
        boolean resEHPP = false;
        boolean resSearch = false;
        if (!mReader.isTimeoutError()) {
            resKill = mReader.mifareSamNxpKillAuthentication();
            //эти 2 команды нужно выполнять вне зависимости от результата выполнения предыдущей, только если не было ошибки таймаута
            if (!mReader.isTimeoutError()) {
                resEHPP = mReader.cscEnterHuntPhaseParameters();
                if (!mReader.isTimeoutError())
                    resSearch = searchCard();
            }
        }
        return resKill && resEHPP && resSearch;
    }

    /**
     * Поиск карты. Необходимая команда для осуществления авторизации
     */
    private boolean searchCard() {
        return searchCard(new SearchCardRes());
    }

    private enum AuthType {
        mifareSamNxpAuthenticate, mifareSamNxpReAuthenticate;
    }

    private AuthType getAuthType(AuthData authData) {
        if (authData.isAuthSuccess() && !authData.isReAuthDisabled())
            return AuthType.mifareSamNxpReAuthenticate;
        return AuthType.mifareSamNxpAuthenticate;
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
        long time = getCurrentTime();

        boolean out = false;

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
                    killAuthAndResearch();
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

    private boolean authorizeWithStaticKeyAuthorizationStrategy(int sectorNumber, boolean read, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        String fTitle = "authorizeWithStaticKeyAuthorizationStrategy(sectorNumber=" + sectorNumber + " ," + "read=" + read + ") ";

        Logger.trace(RfidReal.class, fTitle + "START");
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

    private boolean getFirmwareVersion(StringBuilder firmwareVersion) {
        Logger.trace(getClass(), "getFirmwareVersion() START");
        long time = getCurrentTime();
        boolean out = mReader.cscVersionCsc(firmwareVersion);
        Logger.trace(getClass(), "getFirmwareVersion() FINISH version = \"" + firmwareVersion + "\"" + getTimeString(time));
        return out;
    }

    /**
     * Closes reader.
     */
    private void closeReader() {
        Logger.trace(this.getClass(), "closeReader START");
        long time = getCurrentTime();
        mReader.cscClose();
        isOpened = false;
        Logger.trace(this.getClass(), "closeReader FINISH" + getTimeString(time));
    }

    /**
     * Подает и отключает питание ридера
     *
     * @param on    - флаг включить/выключить
     * @param sleep - задержка после подачи команды
     */
    private void power(boolean on, long sleep) {
        String powerType = (on) ? "ON" : "OFF";
        Logger.trace(this.getClass(), "power(" + powerType + ", " + sleep + ") START isPowerOn=" + isPowerOn);
        long time = getCurrentTime();
        if (isPowerOn != on) {
            mReader.setPower(on);
            sleepTread(sleep);
            if (!on) {
                isOpened = false;
                //if (SharedPreferencesUtils.isErrorToastsEnabled(mContext))
                //    handler.post(() -> Toaster.showToast(mContext, "RFID Power OFF"));

            }
            isPowerOn = on;
        }
        Logger.trace(this.getClass(), "power(" + powerType + ", " + sleep + ") FINISH isPowerOn=" + isPowerOn + getTimeString(time));
    }

    private void sleepTread(long timeout) {
        if (timeout > 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean getFWVersion(String[] version) {
        Logger.trace(getClass(), "getFWVersion() START");
        long time = getCurrentTime();
        boolean out = false;
        StringBuilder fwVersion = new StringBuilder();
        if (open()) {
            if (getFirmwareVersion(fwVersion)) {
                version[0] = fwVersion.toString();
                out = true;
            }
            softClose();
        }
        Logger.trace(getClass(), "getFWVersion() FINISH version = \"" + fwVersion + "\" res: " + ((out) ? "OK" : "ERROR") + getTimeString(time));
        return out;
    }


    /**
     * Возвращает ATR карты (используется для поиска и подготовки карты к
     * работе) выполняется около 160 мс. Используется для поиска карты в таком виде, потому,
     * что до инициализации и настройки SAM нужно дергать функцию ATQA и SAK
     *
     * @return cardData если карта найдена, или null если карта не найдена
     */
    @Override
    public CardData getRfidAtr() {
        Logger.trace(getClass(), "getRfidAtr() START");
        long time = getCurrentTime();
        CardData cardData = null;
        if (open()) {

            SearchCardRes scRes = new SearchCardRes();
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
            softClose();
        }
        Logger.trace(getClass(), "getRfidAtr() FINISH" + getTimeString(time) + " " + ((cardData != null) ? "OK" : "NOT FOUND"));
        return cardData;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

    /**
     * SELECTS and resets SAM
     *
     * @return
     */
    private boolean selectAndResetSAM(byte samSlot) {
        String fTitle = "selectAndResetSAM(" + samSlot + ") ";
        Logger.trace(getClass(), fTitle + "START");

        long time = getCurrentTime();

        boolean out = false;
        byte fidi = 0x00;

        int res = mReader.cscConfigIoExt((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00);

        if (!mReader.isTimeoutError()) {

            // Выбор слота
            byte samNb = samSlot;

            mReader.cscWriteIoExt((byte) 0x80, (byte) 0x80);

            if (!mReader.isTimeoutError()) {
                res = mReader.cscSelectSam(samNb, Defines.SAM_PROT_ISO_7816_T0);
                if (!mReader.isTimeoutError()) {

                    // Resets SAM
                    byte[] lpATR = new byte[256];
                    int[] lpcbATR = new int[1];

                    res = mReader.cscResetSam((byte) (samNb + 1), lpATR, lpcbATR);

                    // SAM can't be reseted, return the error
                    if (res != Defines.RCSC_Ok) {
                        if (samNb != 4) {
                            Logger.trace(RfidReal.class, fTitle + "Error resetting SAM " + samNb + 1);
                        }
                    } else {
                        fidi = lpATR[2];
                    }

                    if (res == Defines.RCSC_Ok) {
                        byte[] status = new byte[2];
                        res = mReader.cscSetSamBaudratePps((byte) 0x01, fidi, status);

                        //            Logger.trace(RfidReal.class, fTitle+"cscSetSamBaudratePps: " + Defines.errorLookUp(res));
                        //            Logger.trace(RfidReal.class, "cscSetSamBaudratePps status: " + String.format("%02X%02X", status[0], status[1]));
                        //            Logger.trace(RfidReal.class, fTitle+"cscSetSamBaudratePps status: " + CommonUtils.bytesToHexWithSpaces(new byte[]{status[0], status[1]}, 2));
                    }

                    if (res == Defines.RCSC_Ok) {
                        out = true;
                    }
                }
            }
        }

        Logger.trace(getClass(), fTitle + "FINISH res=" + ((out) ? "OK" : "FAILD") + getTimeString(time));
        softClose();
        return out;
    }


    private boolean searchCard(SearchCardRes scRes) {
        Logger.trace(getClass(), "searchCard() START");
        long time = getCurrentTime();
        boolean out = mReader.cscSearchCardExt(scRes.com, scRes.atrLength, scRes.atr);
        Logger.trace(getClass(), "searchCard() FINISH res=" + ((out) ? "OK" : "FAILD") + getTimeString(time));
        return out;
    }

    private CardData getCardData(SearchCardRes scRes) {
        Logger.trace(getClass(), "getCardData() START");
        long time = getCurrentTime();
        byte[] realRfidAttr = new byte[scRes.atrLength[0]];
        System.arraycopy(scRes.atr, 0, realRfidAttr, 0, scRes.atrLength[0]);
        CardData cardData = new CardData();
        cardData.setRfidAttr(realRfidAttr);
        cardData.setCardUID(getUID(scRes));
        cardData.setCom(scRes.com);
        cardData.setMifareUlIdentifyType(getUlIdentifyType());
        cardData.setAtqa(getATQA());
        cardData.setSak(getSAK());

        MifareCardTypeRecognizer mifareCardTypeRecognizer = new MifareCardTypeRecognizer();

        //на этом этапе уже можно отличить Ultralight от Classic
        if (mifareCardTypeRecognizer.getMifareCardType(cardData).isUltralight()) {
            cardData.setEv1(mifareUlEv1GetVersion());
            if (!(cardData.isEv1())) {
                //если карта не Ev1 нужно заново активировать ее
                boolean res = searchCard(scRes);
                if (!res)
                    Logger.error(TAG, "getCardData() ошибка поиска карты после команды определения принадлежности к Ev1");
            }

        }
        //А тут мы уже отличаем разные типы Ultralight
        cardData.setMifareCardType(mifareCardTypeRecognizer.getMifareCardType(cardData));

        Logger.trace(getClass(), "getCardData() FINISH" + getTimeString(time));
        return cardData;
    }

    private byte[] getATQA() {
        Logger.trace(getClass(), "getATQA() START");
        long time = getCurrentTime();
        byte[] out = CardTypeParcer.getATQA(mReader);
        Logger.trace(getClass(), "getATQA() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out) + "\"" + getTimeString(time));
        return out;
    }

    private byte[] getUID(SearchCardRes scRes) {
        Logger.trace(getClass(), "getUID() START");
        long time = getCurrentTime();
        byte[] out = RfidAttrParser.getSerialNumber(scRes.com[0], (byte) scRes.atrLength[0], scRes.atr);
        Logger.trace(getClass(), "getUID() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out) + "\"" + getTimeString(time));
        return out;
    }

    private byte[] getSAK() {
        Logger.trace(getClass(), "getSAK() START");
        long time = getCurrentTime();
        byte[] out = CardTypeParcer.getSAK(mReader);
        Logger.trace(getClass(), "getSAK() FINISH res=\"" + CommonUtils.bytesToHexWithoutSpaces(out) + "\"" + getTimeString(time));
        return out;
    }

    /**
     * Opens reader.
     */
    private boolean checkOpenState() {
        Logger.trace(this.getClass(), "checkOpenState() START isOpened=" + isOpened);
        long time = getCurrentTime();
        int iteration = 1;
        while (!isOpened && MAX_CHECK_OPEN_COUNT >= iteration) {
            Logger.trace(this.getClass(), "checkOpenState() START iteration=" + iteration);
            long startIterationTime = System.currentTimeMillis();
            isOpened = getFirmwareVersion(new StringBuilder());
            if (!isOpened && (System.currentTimeMillis() - startIterationTime) < CHECK_OPEN_ITEM_DELAY) {
                sleepTread(CHECK_OPEN_ITEM_DELAY);
            }
            iteration++;
        }
        Logger.trace(this.getClass(), "checkOpenState() FINISH res=" + isOpened + getTimeString(time));
        return isOpened;
    }

    private static class SearchCardRes {
        byte[] com = new byte[1];
        int[] atrLength = new int[1];
        byte[] atr = new byte[32];
    }

    /**
     * запускает отложенное отключение ридера если это разрешено в конфиге
     */
    public void softClose() {
        synchronized (RfidReal.this) {
            Logger.trace(TAG, "softClose(config=[" + config.toString() + "])");
            if (mReader.isTimeoutError())
                hardClose(0);
            else if (config.isAutoPowerOffEnabled() && timerTask == null) {
                startPowerOffTimer();
            }
        }
    }

    private void startPowerOffTimer() {
        Logger.trace(TAG, "startPowerOffTimer(" + config.getAutoPowerOffDelay() + ")");
        if (timerTask != null) {
            timerTask.cancel();
            timer.purge();
            timerTask = null;
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (RfidReal.this) {
                    Logger.trace(TAG, "timerTask started");
                    if (timerTask == null) {
                        // Допустим, такая ситуация:
                        // 1. Таймер сработал, но мы ещё не успели зайти в synchronized block
                        // 2. Управление передается другому потоку, который дергает open
                        // 3. Метод open заходит в synchronized block и отменяет таймер, теперь timerTask == null
                        // 4. Метод open выходит из synchronized block'а
                        // 5. Мы здесь заходим в synchronized block и хотим выключить RFID, но его только что включили
                        // Поэтому логика такая:
                        // timerTask == null, значит, кто-то отменил его во время вызова данного метода
                        Logger.trace(TAG, "timerTask == null, cancel");
                        return;
                    }
                    if (timerTask != this) {
                        // Допустим, такая ситуация:
                        // 1. Таймер сработал, но мы ещё не успели зайти в synchronized block
                        // 2. Управление передается другому потоку, который дергает softClose
                        // 3. Метод softClose заходит в synchronized block отменяет текуйщий таймер и запускает новый, теперь timerTask != this
                        // 4. Метод softClose выходит из synchronized block'а
                        // 5. Мы здесь заходим в synchronized block и хотим выключить RFID, но уже запущен новый таймер.
                        //    Нельзя сейчас взять и просто обнулить ссылку timerTask, потому что мы не будем знать, что у нас работает ещё один таймер.
                        //    Если такая ситуация случается, похоже проблема где-то на уровне логики. Нет смысла запускать ещё один таймер, если один уже работает.
                        // Поэтому логика такая:
                        // timerTask != this, значит, кто-то запустил новый таймер во время вызова данного метода. Оставим отключение RFID ему.
                        Logger.trace(TAG, "duplicate timer start");
                        return;
                    }
                    hardClose(0);
                    timer.purge();
                    timerTask = null;
                    Logger.trace(TAG, "timerTask completed");
                }
            }
        };
        timer.schedule(timerTask, config.getAutoPowerOffDelay());
    }

    private void hardClose(long timeout) {
        Logger.trace(TAG, "hardClose(timeout=" + timeout + ") START");
        long timeStart = getCurrentTime();
        cancelPowerOffTimer();
        closeReader();
        power(false, timeout);
        Logger.trace(TAG, "hardClose(timeout=" + timeout + ") FINISH" + getTimeString(timeStart));
    }

    @Override
    public boolean getModel(String[] model) {
        model[0] = "RFID HF Reader - ASK UCM108";
        return true;
    }

    private byte mapKeyName(int code) {
        if (code == 2) {
            return (byte) 0x0B;
        } else {
            return (byte) 0x0A;
        }
    }

    private byte mapSamSlotNumber(int code) {
        if (code == 1) {
            return Defines.SAM_SLOT_2;
        } else {
            return Defines.SAM_SLOT_1;
        }
    }

    /**
     * Настройки считывателя смарт-карт.
     */
    public static class Config {
        /**
         * Задержка автоматического выключения ридера после обращения к нему, ms
         */
        public static final long RFID_REAL_AUTO_POWER_OFF_DELAY = 10000;
        /**
         * Флаг автоматического выключения ридера после обращения к нему
         */
        public static final boolean RFID_REAL_AUTO_POWER_OFF_ENABLED = false;

        /**
         * Задержка перед автоматическим отключением питания ридера
         */
        private final long autoPowerOffDelay;

        /**
         * Разрешение на автоматической отключение питание ридера после чтения, записи
         */
        private final boolean autoPowerOffEnabled;

        public Config(long autoPowerOffDelay, boolean autoPowerOffEnabled) {
            this.autoPowerOffDelay = autoPowerOffDelay;
            this.autoPowerOffEnabled = autoPowerOffEnabled;
        }

        public long getAutoPowerOffDelay() {
            return autoPowerOffDelay;
        }

        public boolean isAutoPowerOffEnabled() {
            return autoPowerOffEnabled;
        }

        @Override
        public String toString() {
            return "autoPowerOffDelay=" + autoPowerOffDelay + ", autoPowerOffEnabled=" + autoPowerOffEnabled;
        }
    }
}
