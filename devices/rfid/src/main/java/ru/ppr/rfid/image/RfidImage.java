package ru.ppr.rfid.image;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.CardReadErrorType;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.MifareCardType;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

/**
 * Реализация считывателя смарт-карт, работающая с файлом-образом карты.
 *
 * @author Artem Ushakov
 */
@LoggerAspect.IncludeClass
public class RfidImage implements IRfid {

    public static final int BYTE_IN_BLOCK = 16;

    private static final String TAG = Logger.makeLogTag(RfidImage.class);

    public static final int BLOCK_IN_THE_SECTOR = 4;
    private static final byte[] DATA_FOR_DELETE = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final byte ULC = 0;
    private static final byte Claccic1K = 1;
    private static final byte Claccic4K = 2;
    private static final byte PlusS2K = 3;
    private static final byte ULEV1 = 4;

    // Сергей Лебедев:

    // Предлагаю такой вариант:
    // для Ev1 счетчик 2 лежит на странице 42, а счетчик 3 - на странице 43.
    // Соответственно, в образе это выглядит так, как будто к нынешнему образу дописали два счетчика по 4 байта.
    // Для совместимости со старыми образами.
    // Если данных в образе не хватает, то считаем счетчики 2 и 3 равными нулю.

    private static final byte EV_1_HW_COUNTER_1_PAGE_NUMBER = 41;
    private static final byte EV_1_HW_COUNTER_2_PAGE_NUMBER = 42;
    private static final byte EV_1_HW_COUNTER_3_PAGE_NUMBER = 43;

    private static final int PAGE_SIZE = 4;

    private final Config config;

    public RfidImage(Config config) {
        this.config = config;
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public void close() {
        /* NOP */
    }

    @Override
    public boolean isOpened() {
        return true;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, SamAuthorizationStrategy samAuthorizationStrategy) {
        return readBlocksFromClassic(startSectorNumber, startBlockNumber, blockCount);
    }

    @Override
    public RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        return readBlocksFromClassic(startSectorNumber, startBlockNumber, blockCount);
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, SamAuthorizationStrategy samAuthorizationStrategy) {
        return writeBlocksToClassic(startSectorNumber, startBlockNumber, data, cardUid);
    }

    @Override
    public WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy) {
        return writeBlocksToClassic(startSectorNumber, startBlockNumber, data, cardUid);
    }

    private RfidResult<byte[]> readBlocksFromClassic(int startSectorNumber, int startBlockNumber, int blockCount) {

        Logger.trace(TAG, "readBlocksFromClassic: startSectorNumber - " + startSectorNumber +
                ", startBlockNumber " + startBlockNumber +
                ", blockCount - " + blockCount + " START");

        RfidResult<byte[]> result;
        File file = getFile();

        if (file == null) {
            result = new RfidResult<>(CardReadErrorType.OTHER, "Error open file");
            return result;
        }

        int rfidAttrLen;
        try {
            rfidAttrLen = calculateRfidAtrLength(file);
        } catch (IOException e) {
            Logger.error(TAG, e);
            result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
            return result;
        }

        int block = startBlockNumber;
        int sector = startSectorNumber;

        byte[] tmpArray = new byte[blockCount * BYTE_IN_BLOCK];

        for (int i = 0; i < blockCount; i++) {
            if (block >= 3) {
                block = 0;
                sector++;
            }
            Logger.trace(TAG, "readBlocksFromClassic: sector - " + sector + ", block - " + block);

            int address = (sector * BLOCK_IN_THE_SECTOR + block) * BYTE_IN_BLOCK + rfidAttrLen;
            try {
                byte[] tmp = readBlockFromFile(file, address);
                System.arraycopy(tmp, 0, tmpArray, i * BYTE_IN_BLOCK, BYTE_IN_BLOCK);
            } catch (IOException e) {
                Logger.trace(TAG, "readBlocksFromClassic: Error read data from sector " + sector + ", block " + block, e);
                result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
                return result;
            }

            block++;
        }

        result = new RfidResult<>(tmpArray);

        Logger.trace(TAG, "readBlocksFromClassic: startSectorNumber - " + startSectorNumber +
                ", startBlockNumber " + startBlockNumber +
                ", blockCount - " + blockCount + " FINISH res: " + CommonUtils.bytesToHexWithoutSpaces(tmpArray));
        return result;
    }

    private WriteToCardResult writeBlocksToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid) {
        CardData cardData = getRfidAtr();

        if (cardData == null) {
            WriteToCardResult result = WriteToCardResult.CAN_NOT_SEARCH_CARD;
            Logger.error(RfidImage.class, "Write data to card done with error " + result.name());
            return result;
        }

        if (cardUid != null) {
            if (!Arrays.equals(cardData.getCardUID(), cardUid)) {
                WriteToCardResult result = WriteToCardResult.UID_DOES_NOT_MATCH;
                Logger.error(RfidImage.class, "Write data to card done with error " + result.name());
                return result;
            }
        }

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

        // получаем образ
        File file = getFile();

        if (file != null) {

            try {
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

                    int address = (currentSector * BLOCK_IN_THE_SECTOR + currentBlock)
                            * BYTE_IN_BLOCK + calculateRfidAtrLength(file);
                    writeBlockToFile(file, address, writeData);
                    // увеличиваем номер блока для записи
                    currentBlock++;
                }
            } catch (IOException e) {
                Logger.error(TAG, "Error write pd to image - ", e);
                return WriteToCardResult.WRITE_ERROR;
            }
        } else {
            Logger.error(RfidImage.class, "Error open file");
            return WriteToCardResult.UNKNOWN_ERROR;
        }

        return WriteToCardResult.SUCCESS;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber,
                                              SamAuthorizationStrategy samAuthorizationStrategy, boolean isUseSam) {

        RfidResult<byte[]> result;
        File file = getFile();

        if (file != null) {
            int rfidAttrLen;
            try {
                rfidAttrLen = calculateRfidAtrLength(file);
            } catch (IOException e) {
                Logger.error(TAG, e);
                result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
                return result;
            }
            int address = (sectorNumber * BLOCK_IN_THE_SECTOR + blockNumber) * BYTE_IN_BLOCK + rfidAttrLen;
            try {
                result = new RfidResult<>(readBlockFromFile(file, address));
            } catch (IOException e) {
                Logger.error(TAG, "Error read data from image - ", e);
                result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
            }
        } else {
            result = new RfidResult<>(CardReadErrorType.OTHER, "Error open file");
        }
        return result;
    }

    @Override
    public RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber, byte[] key,
                                              SamAuthorizationStrategy samAuthorizationStrategy,
                                              boolean isUseSam) {
        return readFromClassic(sectorNumber, blockNumber, samAuthorizationStrategy, isUseSam);
    }

    @Override
    public WriteToCardResult writeToClassic(byte[] data, byte[] cardUID, int sector, int block,
                                            SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {
        return writeBlocksToClassic(sector, block, data, cardUID);
    }

    @Override
    public void clearAuthData() {
        /* NOP */
    }

    @Override
    public RfidResult<byte[]> readFromUltralight(byte pageNumber, byte startByte, byte length) {

        RfidResult<byte[]> result;
        File file = getFile();

        if (file != null) {
            int rfidAttrLen;
            try {
                rfidAttrLen = calculateRfidAtrLength(file);
            } catch (IOException e) {
                Logger.error(TAG, e);
                result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
                return result;
            }
            int address = (pageNumber * 4) + startByte + rfidAttrLen;

            try {
                result = new RfidResult<>(readBlockFromFile(file, address, length));
            } catch (IOException e) {
                Logger.error(TAG, "Error read data from image - ", e);
                result = new RfidResult<>(CardReadErrorType.OTHER, e.getMessage());
            }

        } else {
            result = new RfidResult<>(CardReadErrorType.OTHER, "Error open file");
        }

        return result;
    }

    @NonNull
    @Override
    public RfidResult<byte[]> readCounterFromUltralightEV1(int counterIndex) {
        // Сейчас так сделаны образы
        byte pageNumber;
        byte length = 0x3; // Счетчик 3-байтный (24-битный)
        if (counterIndex == 0) {
            pageNumber = EV_1_HW_COUNTER_1_PAGE_NUMBER;
        } else if (counterIndex == 1) {
            pageNumber = EV_1_HW_COUNTER_2_PAGE_NUMBER;
        } else if (counterIndex == 2) {
            pageNumber = EV_1_HW_COUNTER_3_PAGE_NUMBER;
        } else {
            return new RfidResult<>(CardReadErrorType.OTHER, "CounterIndex = " + counterIndex + " is out of bounds");
        }

        int pageCount = getUltralightImagePageCount();
        if (pageNumber < pageCount) {
            return readFromUltralight(pageNumber, (byte) 0, length);
        } else {
            return new RfidResult<>(new byte[length]);
        }
    }

    @Override
    public WriteToCardResult writeToUltralight(byte[] data, byte[] cardUID, int address) {

        File image = getFile();

        if (image != null) {

            CardData cardData = getRfidAtr();
            if (cardData != null) {
                if (cardUID != null) {
                    if (!Arrays.equals(cardData.getCardUID(), cardUID)) {
                        return WriteToCardResult.UID_DOES_NOT_MATCH;
                    }
                }

                try {
                    // домнoжаем номер страницы на 4 для определения номера байта с которого
                    // необходимо начать запись
                    writeBlockToFile(image, address * 4 + calculateRfidAtrLength(image), data);
                } catch (IOException e) {
                    Logger.error(TAG, "Error write data to ultralight image - ", e);
                }
            } else {
                return WriteToCardResult.WRITE_ERROR;
            }
        } else {
            return WriteToCardResult.CAN_NOT_SEARCH_CARD;
        }
        return WriteToCardResult.SUCCESS;
    }

    @NonNull
    @Override
    public WriteToCardResult incrementCounterUltralightEV1(int counterIndex, int incrementValue, byte[] cardUid) {

        // Сейчас так сделаны образы
        byte pageNumber;
        if (counterIndex == 0) {
            pageNumber = EV_1_HW_COUNTER_1_PAGE_NUMBER;
        } else if (counterIndex == 1) {
            pageNumber = EV_1_HW_COUNTER_2_PAGE_NUMBER;
        } else if (counterIndex == 2) {
            pageNumber = EV_1_HW_COUNTER_3_PAGE_NUMBER;
        } else {
            return WriteToCardResult.UNKNOWN_ERROR;
        }

        int pageCount = getUltralightImagePageCount();
        if (pageNumber >= pageCount) {
            // Делаем вид, что запись прошла успешно
            return WriteToCardResult.SUCCESS;
        }

        RfidResult<byte[]> readResult = readCounterFromUltralightEV1(counterIndex);

        if (readResult.isOk()) {
            ByteBuffer longByteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            longByteBuffer.put(readResult.getResult());
            long counterValue = longByteBuffer.getLong(0);

            ByteBuffer intByteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            intByteBuffer.putInt((int) counterValue + incrementValue);

            return writeToUltralight(intByteBuffer.array(), cardUid, pageNumber);
        } else {
            return WriteToCardResult.WRITE_ERROR;
        }
    }

    @Override
    public WriteToCardResult deleteDataFromClassic(byte[] cardUID, byte startSector, byte startBlock,
                                                   int countBlockForDelete,
                                                   SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp) {

        Logger.trace(RfidImage.class, "Delete data from card: uid card - " + CommonUtils.bytesToHexWithSpaces(cardUID) +
                ",\nstart sector - " + startSector +
                ",\nstart block - " + startBlock +
                ",\ncount block for delete - " + countBlockForDelete +
                ",\ncard type - " + samAuthorizationStrategy);

        WriteToCardResult result = WriteToCardResult.SUCCESS;

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

            // если блок записался с ошибкой то сохраняем тип ошибки и выходим из цикла
            if (writeToCardResult != WriteToCardResult.SUCCESS) {
                result = writeToCardResult;
                break;
            }
        }
        Logger.info(RfidImage.class, "Delete done with result " + result.name());
        return result;
    }

    @Override
    public boolean getFWVersion(String[] version) {
        version[0] = "FileVersion";
        return true;
    }

    @Override
    public CardData getRfidAtr() {

        File image = getFile();
        CardData cardData = null;

        if (image != null) {

            try {
                byte[] rfidAtr = new byte[calculateRfidAtrLength(image)];
                RandomAccessFile randomAccessFile = new RandomAccessFile(image, "r");
                randomAccessFile.readFully(rfidAtr);
                cardData = new CardData();
                cardData.setAtqa(new byte[2]);
                cardData.setCom(new byte[1]);
                cardData.setSak(new byte[1]);
                cardData.setMifareUlIdentifyType((byte) 0);
                cardData.setRfidAttr(rfidAtr);
                cardData.setCardUID(parseUidCard(rfidAtr));
                MifareCardType cardType;
                switch (rfidAtr[0]) {

                    case 0x18:
                        cardType = MifareCardType.Mifare_Classic_1K;
                        break;

                    case 0x07:
                        if (rfidAtr[0] == 0x01) cardType = MifareCardType.Mifare_Classic_1K;
                        else cardType = MifareCardType.UltralightC;
                        break;

                    case ULC:
                        cardType = MifareCardType.UltralightC;
                        break;
                    case Claccic1K:
                        cardType = MifareCardType.Mifare_Classic_1K;
                        break;
                    case Claccic4K:
                        cardType = MifareCardType.Mifare_Classic_4K;
                        break;
                    case PlusS2K:
                        cardType = MifareCardType.Mifare_Plus_2K;
                        break;
                    case ULEV1:
                        cardType = MifareCardType.UltralightEV1;
                        break;

                    default:
                        cardType = MifareCardType.Unknown;
                        break;
                }
                cardData.setMifareCardType(cardType);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return cardData;
    }

    private long readImageSize() {
        File image = getFile();
        return image == null ? 0 : image.length();
    }

    private byte[] parseUidCard(byte[] rfidAtr) {
        // rfidAtr[0] - Тип карты
        // rfidAtr[1] - Длина cardUid
        byte[] crystalNumber = new byte[rfidAtr[1]];
        System.arraycopy(rfidAtr, 2, crystalNumber, 0, rfidAtr[1]);
        return crystalNumber;
    }

    private byte[] readBlockFromFile(@NonNull File file, int startPosition) throws IOException {

//        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//        byte[] buffer = new byte[BYTE_IN_BLOCK];
//        randomAccessFile.skipBytes(startPosition);
//        randomAccessFile.readFully(buffer);
//        randomAccessFile.close();
        return readBlockFromFile(file, startPosition, BYTE_IN_BLOCK);
    }

    private byte[] readBlockFromFile(@NonNull File file, int startPosition, int length) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[length];
        randomAccessFile.skipBytes(startPosition);
        randomAccessFile.readFully(buffer);
        randomAccessFile.close();
        return buffer;
    }


    private void writeBlockToFile(@NonNull File file, int startPosition, byte[] data) throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        byte[] firstFilePart = new byte[startPosition];
        byte[] endFilePart = new byte[(int) (file.length() - startPosition - data.length)];

        randomAccessFile.readFully(firstFilePart);
        randomAccessFile.skipBytes(data.length);
        randomAccessFile.readFully(endFilePart);
        randomAccessFile.seek(0);
        randomAccessFile.write(firstFilePart);
        randomAccessFile.seek(firstFilePart.length);
        randomAccessFile.write(data);
        randomAccessFile.seek(firstFilePart.length + data.length);
        randomAccessFile.write(endFilePart);
        randomAccessFile.setLength(firstFilePart.length + data.length + endFilePart.length);
        randomAccessFile.close();
    }

    private int calculateRfidAtrLength(@NonNull File file) throws IOException {
        byte[] cardUidLength = readBlockFromFile(file, 1, 1);
        return cardUidLength[0] + 2;
    }

    @Nullable
    private File getFile() {
        final FluentIterable<File> files = Files.fileTreeTraverser().preOrderTraversal(config.getImageDir());
        File outFile = null;

        if (!files.isEmpty()) {

            for (File file : files) {
                if (file.isFile()) {
                    outFile = file;
                }
            }

        } else {
            Logger.error(RfidImage.class, "Empty image dir");
        }
        return outFile;
    }

    @Override
    public boolean getModel(String[] model) {
        model[0] = "FileRfidReader";
        return true;
    }

    private int getUltralightImagePageCount() {
        try {
            File image = getFile();

            if (image == null) {
                return 0;
            }

            long imageSize = image.length();
            int rfidAttrLen = calculateRfidAtrLength(image);

            return (int) ((imageSize - rfidAttrLen) / PAGE_SIZE);

        } catch (IOException e) {
            Logger.error(TAG, e);
            return 0;
        }
    }

    /**
     * Настройки считывателя смарт-карт.
     */
    public static class Config {

        /**
         * Путь до папки с образами.
         */
        private final File imageDir;

        public Config(File imageDir) {
            this.imageDir = imageDir;
        }

        public File getImageDir() {
            return imageDir;
        }

        @Override
        public String toString() {
            return "imageDir=" + imageDir;
        }
    }
}
