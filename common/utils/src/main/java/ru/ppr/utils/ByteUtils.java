package ru.ppr.utils;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Александр on 17.09.2015.
 */
public class ByteUtils {

    public static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[][] splitArray(byte[] source, int partSize) {
        byte[][] res = new byte[source.length / partSize + (((source.length % partSize) == 0) ? 0 : 1)][];
        for (int i = 0; i < res.length; i++) {
            res[i] = Arrays.copyOfRange(source, i * partSize, (i + 1) * partSize < source.length ? (i + 1) * partSize : source.length);
        }
        return res;
    }

    /**
     * Вырезает из масива байтов кусок
     *
     * @param data       массив байтов
     * @param startIndex индекс, с которого необходимо вырезать масив байтов
     * @param countByte  длина куска, в байтах
     * @return
     */
    public static byte[] getBytesFromData(byte[] data, int startIndex, int countByte) {
        byte[] tmpData = new byte[countByte];
        System.arraycopy(data, startIndex, tmpData, 0, countByte);
        return tmpData;
    }

    /**
     * Конвертирует массив байтов в число типа Long. Передаваемый массив байтов
     * не может быть больше 8
     *
     * @param dataByte массив байтов
     * @param order    порядок следования байтов
     * @return
     */
    public static long convertByteToLong(byte[] dataByte, ByteOrder order) {

        if (dataByte.length > 8)
            throw new IllegalArgumentException();
        byte[] tmpArray = new byte[8];
        System.arraycopy(dataByte, 0, tmpArray, 0, dataByte.length);
        ByteBuffer wrapper = ByteBuffer.wrap(tmpArray).order(order);
        return wrapper.getLong();
    }

    /**
     * Конвертирует массив байтов в число типа int. Передаваемый массив байтов
     * не может быть больше 4
     *
     * @param dataByte массив байтов
     * @param order    порядок следования байтов
     * @return
     */
    public static int convertByteToInt(@NonNull byte[] dataByte, @NonNull ByteOrder order) {

        if (dataByte.length > 4)
            throw new IllegalArgumentException();
        byte[] tmpArray = new byte[4];
        System.arraycopy(dataByte, 0, tmpArray, 0, dataByte.length);
        ByteBuffer wrapper = ByteBuffer.wrap(tmpArray).order(order);
        return wrapper.getInt();
    }

}
