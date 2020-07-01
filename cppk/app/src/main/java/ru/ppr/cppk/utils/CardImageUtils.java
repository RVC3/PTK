package ru.ppr.cppk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.MD5Utils;

public class CardImageUtils {

    /**
     * Читает заданный блок с образа карты MifareClassic
     */
    public static byte[] readBlockFromMifareClassic(byte[] image, byte sectorNumber, byte blockNumber) {
        int startByte = sectorNumber * 16 * 4 + blockNumber * 16 + getImageUid(image).length;
        int byteCount = 16;

        byte[] out = new byte[byteCount];

        if (image.length >= startByte + byteCount)
            System.arraycopy(image, startByte, out, 0, byteCount);

        Logger.trace(CardImageUtils.class, "Read from card image with sectorNumber - " + sectorNumber + " and block number - " + blockNumber);
        Logger.trace(CardImageUtils.class, "Read data from card image - " + CommonUtils.byteArrayToString(out));

        return out;
    }

    /**
     * Читает несколько блоков с образа карты MifareClassic (будет пропускать
     * блоки №3 с ключами доступа)
     */
    public static byte[] readDataFromMifareClassic(byte[] image, byte sectorIndex, byte blockIndex, byte countBlock) {
        byte[] out = new byte[countBlock * 16];

        int i = 0;
        byte currentSector = sectorIndex;
        byte currentBlock = blockIndex;

        while (i < countBlock) {
            if (currentBlock == 3) {
                currentSector++;
                currentBlock = 0;
            }
            byte[] blockData = readBlockFromMifareClassic(image, currentSector, currentBlock);
            System.arraycopy(blockData, 0, out, 16 * i, 16);
            i++;
            currentBlock++;
        }

        Logger.trace(CardImageUtils.class, "readDataFromMifareClassic: Read " + countBlock + " blocks from mifare classic image.\nSector index - " + sectorIndex + ", block index - " + blockIndex);
        Logger.trace(CardImageUtils.class, "readDataFromMifareClassic: Reading data - " + CommonUtils.byteArrayToString(out));
        return out;
    }

    /**
     * Читает данные с образа Ultralight
     *
     * @param image     данные образа
     * @param startByte стартовый байт
     * @param lenght    длинна считываемых данных
     * @return
     */
    public static byte[] readDataFromUltralight(byte[] image, int startByte, int lenght) {
        byte[] data = new byte[lenght];
        System.arraycopy(image, startByte + 9, data, 0, lenght);
        return data;
    }

    /**
     * Возвращает заданные байты из исходного массива данных
     */
    public static byte[] getBytes(byte[] data, byte startByte, byte byteCount) {
        byte[] out = new byte[byteCount];
        if (data.length >= startByte + byteCount)
            System.arraycopy(data, startByte, out, 0, byteCount);
        return out;
    }

    /**
     * Преобразует образ в строку с разбивкой на сектора (Mifare Classic)
     */
    public static String getImageStringForOut(byte[] image) {
        String imageString = addSybolToString(MD5Utils.convertHashToString(image), " ", 32);
        imageString = addSybolToString(imageString, "\n", 132);
        imageString = imageString.replaceAll("\n ", "\n");
        return imageString;
    }

    /**
     * Добавляет в исходную строку символ, через каждые blockSize символов,
     * возвращает новую строку
     */
    private static String addSybolToString(String str, String symbol, int blockSize) {
        StringBuilder out = new StringBuilder(str);
        int idx = str.length() - blockSize;
        while (idx > 0) {
            out.insert(idx, symbol);
            idx = idx - blockSize;
        }
        return out.toString();
    }

    /**
     * Запишет данные образа в файл
     */
    public static File writeImageToFile(byte[] image) {
        File folder = new File(PathsConstants.IMAGE);
        if (folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (File file : fileList) {
                    if (file.isFile())
                        file.delete();
                }
            }
        }
        File imageFile = new File(PathsConstants.IMAGE +"/"+ DateFormatOperations.getUtcString(new Date()) + ".bin");
        try {
            FileOutputStream fos = new FileOutputStream(imageFile.getPath());
            fos.write(image);
            fos.close();
        } catch (java.io.IOException e) {
            Logger.error(CardImageUtils.class.getSimpleName(), "Ошибка записи образа в файл", e);
        }
        return imageFile;
    }

    /**
     * вернет футер образа не имеющий отношение к самим данным
     *
     * @param image
     * @return
     */
    public static byte[] getImageUid(byte[] image) {
        int uidLength = image.length % 16;
        byte[] uid = new byte[uidLength];
        System.arraycopy(image, 0, uid, 0, uidLength);
        return uid;
    }

}
