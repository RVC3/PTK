package ru.ppr.inpas.lib.utils;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Вспомогательный класс для работы с байтами.
 */
public class ByteUtils {
    private static final String TAG = InpasLogger.makeTag(ByteUtils.class);

    /**
     * Преобразование byte в int.
     *
     * @param value преобразуемое значение.
     * @return преобразованное значение.
     */
    public static int byteToInt(final byte value) {
        return value & 0xFF;
    }


    /**
     * Преобразование int в byte.
     *
     * @param value преобразуемое значение.
     * @return преобразованное значение.
     */
    public static byte intToByte(final int value) {
        return (byte) (value & 0xFF);
    }

    /**
     * Метод для проверки совпадения CRC16.
     *
     * @param data данные для вычисления CRC16.
     * @return результат проверки совпадения CRC16.
     */
    public static boolean isValidCrc16(@NonNull final byte[] data) {
        final int length = byteToInt(data, 1, 2);
        final byte crc1 = (byte) (data[data.length - 1] & 255);
        final byte crc2 = (byte) (data[data.length - 2] & 255);

        try {
            computeCrc16(data, 0, length + 3, length + 3);
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
        }

        return ((crc1 == (byte) (data[data.length - 1] & 255))
                && (crc2 == (byte) (data[data.length - 2] & 255)));
    }

    /**
     * Преобразование массива byte в int.
     *
     * @param data   данные для преобразования.
     * @param offset смещение внутри данных.
     * @param size   количество элементов для преобразования.
     * @return преобразованное значение.
     */
    public static int byteToInt(@NonNull final byte[] data, final int offset, final int size) {
        int result = 0;
        int x = offset + size - 1;

        for (int y = size; y > 0; --y) {
            result <<= 8;
            int n = data[x--];
            if (n < 0) {
                n += 256;
            }

            result |= n;
        }

        return result;
    }

    /**
     * Метод для вычисления CRC16 согласно реализации от INPAS.
     *
     * @param data       данные для вычисления CRC16.
     * @param dataOffset смещение внутри данных.
     * @param dataSize   количество элементов для преобразования.
     * @param crcOffset  смещение внутри данных куда записываются контрольные суммы.
     */
    public static void computeCrc16(@NonNull final byte[] data, int dataOffset, int dataSize, final int crcOffset) {
        int s;
        for (s = 0; dataSize > 0; ++dataOffset) {
            byte b = data[dataOffset];

            for (int j = 0; j < 8; ++j) {
                boolean x16 = ((b & 128) == 0 || (s & '耀') == 0) && ((b & 128) != 0 || (s & '耀') != 0);
                boolean x15 = (!x16 || (s & 16384) == 0) && (x16 || (s & 16384) != 0);
                boolean x2 = (!x16 || (s & 2) == 0) && (x16 || (s & 2) != 0);
                s <<= 1;
                b = (byte) (b << 1);
                if (x16) {
                    s |= 1;
                }

                if (x2) {
                    s |= 4;
                } else {
                    s &= '\ufffb';
                }

                if (x15) {
                    s |= '耀';
                } else {
                    s &= 32767;
                }
            }

            --dataSize;
        }

        data[crcOffset + 1] = (byte) (s & 255);
        s >>= 8;
        data[crcOffset] = (byte) (s & 255);
    }

}