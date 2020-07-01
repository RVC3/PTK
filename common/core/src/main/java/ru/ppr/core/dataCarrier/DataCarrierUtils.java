package ru.ppr.core.dataCarrier;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.logger.Logger;

/**
 * Вспомогательные функции для декодеров ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class DataCarrierUtils {

    private static final String TAG = Logger.makeLogTag(DataCarrierUtils.class);

    private static final String ENCODING_WIN_1251 = "windows-1251";
    private static final String ENCODING_ACII = "ASCII";

    private static final String[] ZEROS = new String[]{
            "00000000",
            "0000000",
            "000000",
            "00000",
            "0000",
            "000",
            "00",
            "0"
    };

    /**
     * Возвращет булево значение по индексу бита в байте.
     *
     * @param value  Байт
     * @param bitPos Индекс бита
     * @return Значение бита
     */
    public static boolean byteToBoolean(byte value, int bitPos) {
        int trimLow = bitPos;
        boolean result = (((value & 0xff) >>> trimLow) & 0x01) == 0x01;
        return result;
    }

    /**
     * Конвертирует {@code byte} в {@code int} с потерей знака.
     *
     * @param value Байт
     * @return 4-х байтовое представление.
     */
    public static int byteToInt(byte value) {
        return 0xff & value;
    }

    /**
     * Конвертирует диапазон бит из байта в {@code int}.
     *
     * @param value       Байт
     * @param firstBitPos Индекс первого бита
     * @param length      Количество битов
     * @return 4-х байтовое представление.
     */
    public static int byteToInt(byte value, int firstBitPos, int length) {
        return bytesToInt(new byte[]{value}, firstBitPos, length, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Конвертирует массив байтов в {@code int}.
     *
     * @param bytes     Массив байтов
     * @param byteOrder Порядок сортировки байтов
     * @return 4-х байтовое представление.
     */
    public static int bytesToInt(byte[] bytes, ByteOrder byteOrder) {
        return bytesToInt(bytes, 0, bytes.length * Byte.SIZE, byteOrder);
    }

    /**
     * Конвертирует диапазон битов из массива байтов в {@code int}.
     *
     * @param bytes       Массив байтов
     * @param firstBitPos Индекс первого бита
     * @param length      Количество битов
     * @param byteOrder   Порядок сортировки байтов
     * @return 4-х байтовое представление.
     */
    public static int bytesToInt(byte[] bytes, int firstBitPos, int length, ByteOrder byteOrder) {
        return (int) bytesToLong(bytes, firstBitPos, length, byteOrder);
    }

    /**
     * Конвертирует массив байтов в {@code long}.
     *
     * @param bytes     Массив байтов
     * @param byteOrder Порядок сортировки байтов
     * @return 8-и байтное представление.
     */
    public static long bytesToLong(byte[] bytes, ByteOrder byteOrder) {
        return bytesToLong(bytes, 0, bytes.length * Byte.SIZE, byteOrder);
    }

    public static long byteToInt(byte[] bytes, int length) {
        int val = 0;
        if(length>4) throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

    public static int toInt( byte[] bytes, int length ) {
        int result = 0;
        for (int i=0; i<length; i++) {
            result = ( result << 8 ) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    /**
     * Конвертирует диапазон битов из массива байтов в {@code long}.
     *
     * @param bytes       Массив байтов
     * @param firstBitPos Индекс первого бита
     * @param length      Количество битов
     * @param byteOrder   Порядок сортировки байтов
     * @return 8-и байтное представление.
     */
    public static long bytesToLong(byte[] bytes, int firstBitPos, int length, ByteOrder byteOrder) {
        long result = 0;
        int lastBitPos = firstBitPos + length - 1;
        for (int i = 0; i < bytes.length; i++) {
            int startBitPos = Byte.SIZE * i;
            int endBitPos = startBitPos + Byte.SIZE - 1;
            if (firstBitPos > endBitPos) {
                continue;
            }
            int trimHigh = 0;
            int realIndex = byteOrder == ByteOrder.LITTLE_ENDIAN ? bytes.length - i - 1 : i;
            long temp = bytes[realIndex] & 0xff;
            if (firstBitPos <= endBitPos && firstBitPos >= startBitPos) {
                trimHigh = firstBitPos;
                temp = temp >>> firstBitPos;
            }
            if (lastBitPos >= endBitPos) {
                result = result << Byte.SIZE;
                result = result | temp;
            }
            if (lastBitPos < endBitPos) {
                int trimLow = Long.SIZE - Byte.SIZE + endBitPos - lastBitPos + trimHigh;
                temp = (temp << trimLow) >>> trimLow;
                result = (result << (Long.SIZE - trimLow));
                result = result | temp;
            }
        }
        return result;
    }

    /**
     * Конвертирует диапазон битов из массива байтов в {@code String}.
     *
     * @param bytes       Массив байтов
     * @param firstBitPos Индекс первого бита
     * @param length      Количество битов
     * @return Бинараная строка
     */
    public static String bytesToBits(byte[] bytes, int firstBitPos, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte value : bytes) {
            String binaryValue = Integer.toBinaryString(value);
            if (binaryValue.length() < 8) {
                stringBuilder.append(ZEROS[binaryValue.length()]);
            }
            stringBuilder.append(binaryValue);
        }
        return stringBuilder.substring(firstBitPos, firstBitPos + length);
    }

    /**
     * Конвертирует бинараную строку в {@code int}.
     *
     * @param bits Бинараная строка
     * @return 4-х байтовое представление.
     */
    public static int bitsToInt(String bits) {
        return Integer.valueOf(bits, 2);
    }

    /**
     * Конвертирует Unix time в {@link Date}
     *
     * @param timeInSeconds Время в секундах
     * @return Дата
     */
    public static Date unixTimestampToDate(long timeInSeconds) {
        return new Date(timeInSeconds * 1000);
    }

    /**
     * Конвертирует  {@link Date} в Unix time
     *
     * @param date Дата
     * @return Время в секундах
     */
    public static long dateToUnixTimestamp(Date date) {
        return date.getTime() / 1000;
    }

    /**
     * Конвертирует массив байтов в строку в кодировке Win1251.
     *
     * @param data Массив байтов
     * @return Строка
     */
    public static String bytesToStringWin1251(byte[] data) {
        String str = null;
        try {
            str = new String(data, ENCODING_WIN_1251);
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }

        return str;
    }

    /**
     * Конвертирует строку в кодировке Win1251 в массив байтов.
     *
     * @param string Строка
     * @return Массив байтов
     */
    public static byte[] stringWin1251ToBytes(String string) {
        byte[] bytes = null;
        try {
            bytes = string.getBytes(ENCODING_WIN_1251);
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }
        return bytes;
    }

    /**
     * Вырезает из масива байтов кусок.
     *
     * @param data       массив байтов
     * @param startIndex индекс, с которого необходимо вырезать масив байтов
     * @param countByte  длина куска, в байтах
     * @return
     */
    public static byte[] subArray(byte[] data, int startIndex, int countByte) {
        byte[] tmpData = new byte[countByte];
        System.arraycopy(data, startIndex, tmpData, 0, countByte);
        return tmpData;
    }

    /**
     * Пишет байты в указанной позиции массива байтов.
     *
     * @param src     Байты для копирования
     * @param dest    Массив байтов
     * @param destPos Стартовый индекс записи
     */
    public static void writeBytes(byte[] src, byte[] dest, int destPos) {
        System.arraycopy(src, 0, dest, destPos, src.length);
    }

    /**
     * Пишет булево значение в указанной позиции массива байтов.
     *
     * @param value     Булево значение
     * @param data      Массив байтов
     * @param byteIndex Индекс байта в массиве
     * @param bitIndex  Индекс бита в байте
     */
    public static void writeBoolean(boolean value, byte[] data, int byteIndex, int bitIndex) {
        if (value) {
            data[byteIndex] = (byte) (data[byteIndex] | (1 << bitIndex));
        } else {
            data[byteIndex] = (byte) (data[byteIndex] & ~(1 << bitIndex));
        }
    }

    /**
     * Пишет значение типа int в указанной позиции массива байтов.
     *
     * @param value          Значение типа int
     * @param data           Массив байтов
     * @param startByteIndex Стартовый индекс байта в массиве
     * @param byteCount      Количество байтов для значения
     * @param byteOrder      Порядок сортировки байтов при записи числа
     */
    public static void writeInt(int value, byte[] data, int startByteIndex, int byteCount, ByteOrder byteOrder) {
        writeInt(value, data, startByteIndex, 0, byteCount * Byte.SIZE, byteOrder);
    }

    /**
     * Пишет значение типа int в указанной позиции массива байтов.
     *
     * @param value          Значение типа int
     * @param data           Массив байтов
     * @param startByteIndex Стартовый индекс байта в массиве
     * @param startBitIndex  Стартовый индекс бита в стартовом байте
     * @param bitLength      Количество битов для значения
     * @param byteOrder      Порядок сортировки байтов при записи числа
     */
    public static void writeInt(int value, byte[] data, int startByteIndex, int startBitIndex, int bitLength, ByteOrder byteOrder) {
        writeLong(value, data, startByteIndex, startBitIndex, bitLength, byteOrder);
    }

    /**
     * Пишет значение типа long в указанной позиции массива байтов.
     *
     * @param value          Значение типа long
     * @param data           Массив байтов
     * @param startByteIndex Стартовый индекс байта в массиве
     * @param byteCount      Количество байтов для значения
     * @param byteOrder      Порядок сортировки байтов при записи числа
     */
    public static void writeLong(long value, byte[] data, int startByteIndex, int byteCount, ByteOrder byteOrder) {
        writeLong(value, data, startByteIndex, 0, byteCount * Byte.SIZE, byteOrder);
    }

    /**
     * Массив байтов в hex строку
     * @param a byte array
     * @return hex strings
     */
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b)).append(",");
        }
        return sb.toString();
    }


    /**
     * Функция вытаскивает значения по указанным диапазонам
     * TODO функция специфичная,найти правильное название
     * @param data  данные
     * @param index начало
     * @param count длинна
     * @return значение
     */
    public static int getValue(byte[] data, int index, int count) {
        byte[] tempData = new byte[data.length];
        System.arraycopy(data, 0, tempData, 0, data.length);
        BitArray bitAr = new BitArray(tempData.length * 8, tempData);
        final boolean[] tempBoolData = bitAr.toBooleanArray();
        boolean[] resTemp = new boolean[count];
        System.arraycopy(tempBoolData, index, resTemp, 0, count);
        final boolean[] reverseTemp = reverseBooleans(resTemp);
        int result = 0;
        for (int i = 0; i < reverseTemp.length; i++) {
            if (reverseTemp[i])
                result = result | (1 << i);
        }
        return result;
    }

    private static boolean[] reverseBooleans(boolean[] data) {
        int length = data.length;
        boolean[] result = new boolean[length];
        if (length == 0)
            return result;

        for (int i = 0; i < length; i++) {
            result[i] = data[length - i - 1];
        }
        return result;
    }


    /**
     * Пишет значение типа long в указанной позиции массива байтов.
     *
     * @param value          Значение типа long
     * @param data           Массив байтов
     * @param startByteIndex Стартовый индекс байта в массиве
     * @param startBitIndex  Стартовый индекс бита в стартовом байте
     * @param bitLength      Количество битов для значения
     * @param byteOrder      Порядок сортировки байтов при записи числа
     */
    public static void writeLong(long value, byte[] data, int startByteIndex, int startBitIndex, int bitLength, ByteOrder byteOrder) {

        long clearedValue = (value << (Long.SIZE - bitLength)) >>> (Long.SIZE - bitLength);
        if (value != clearedValue) {
            Logger.warning(TAG, "value = " + value + " is out of bounds of " + bitLength + " bits");
        }

        int endByteIndex = startByteIndex + (int) Math.ceil((1.0 * startBitIndex + bitLength) / Byte.SIZE) - 1;

        int offset;
        int destBytesCount;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            offset = startBitIndex;
        } else {
            offset = startBitIndex + bitLength > Byte.SIZE ? Byte.SIZE - (startBitIndex + bitLength) % Byte.SIZE : startBitIndex;
            offset = offset == Byte.SIZE ? 0 : offset;
        }
        // Если есть смещение, придется записать части байта из буфера в отдельные элементы массива data
        destBytesCount = offset == 0 || startBitIndex + bitLength <= Byte.SIZE ? 1 : 2;

        int maxBytesCount = Long.SIZE / Byte.SIZE;

        int currentBitLength = 0;
        for (int i = 0; i < maxBytesCount; i++) {

            if (currentBitLength == bitLength) {
                // Если нужное количество битов записано, завершаем операцию
                break;
            }

            for (int j = 0; j < destBytesCount; j++) {

                if (currentBitLength == bitLength) {
                    // Если нужное количество битов записано, завершаем операцию
                    break;
                }

                // Достаем текущее значение
                int byteIndex = byteOrder == ByteOrder.BIG_ENDIAN ? endByteIndex - i - j : startByteIndex + i + j;
                int byteValue = data[byteIndex] & 0xff;

                // Вычисляем диапазон битов, куда будем писать значение
                int startPos;
                int endPos;
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    if (j == 0) {
                        startPos = offset;
                        endPos = currentBitLength + (Byte.SIZE - offset) < bitLength ? Byte.SIZE : bitLength - currentBitLength + startPos;
                    } else {
                        startPos = 0;
                        endPos = currentBitLength + offset < bitLength ? offset : bitLength - currentBitLength + startPos;
                    }
                } else {
                    if (j == 0) {
                        if (i == 0) {
                            startPos = (startBitIndex + bitLength) > Byte.SIZE ? 0 : offset;
                            endPos = Byte.SIZE - offset;
                        } else {
                            startPos = offset;
                            endPos = currentBitLength + (Byte.SIZE - offset) < bitLength ? Byte.SIZE : bitLength - currentBitLength + startPos;
                        }
                    } else {
                        if (i == 0) {
                            startPos = 0;
                            endPos = currentBitLength + offset < bitLength ? offset : bitLength - currentBitLength + startPos;
                        } else {
                            startPos = 0;
                            endPos = currentBitLength + offset < bitLength ? offset : bitLength - currentBitLength + startPos;
                        }
                    }
                }
                int length = endPos - startPos;

                // Очищаем данные в диапазоне назначения
                int clearMask = 0xff;
                clearMask = (clearMask << (Integer.SIZE - endPos)) >>> (Integer.SIZE - endPos);
                clearMask = (clearMask >>> startPos) << startPos;
                clearMask = ~clearMask;
                byteValue = byteValue & clearMask;

                // Пишем данные в диапазон битов
                long bits = value;
                int trimHigh = Long.SIZE - currentBitLength - length;
                bits = (bits << trimHigh) >>> trimHigh;
                int trimLow = currentBitLength;
                bits = bits >>> trimLow;
                bits = bits << startPos;

                // Смело кастим bits к int, потому что нужыные биты среди младших восьми
                byteValue = byteValue | (int) bits;
                data[byteIndex] = (byte) byteValue;

                // Обновляем количество записанных битов
                currentBitLength += length;
            }
        }
    }

}
