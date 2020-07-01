package ru.ppr.ingenico.utils;

/**
 * Класс-утилита для выполнения операций с числами на уровне битов.
 *
 * @author Dmitry Nevolin
 */
public class BitUtils {

    /**
     * Конвертирует 2 байта с указанной позиции в переданном массиве в {@code int}.
     *
     * @param bytes  Массив байтов, откуда брать 2 байта
     * @param offset Позиция в массиве, с которой брать 2 байта
     * @return десятичное представление двухбайтового числа
     */
    public static int convertToUInt16(byte[] bytes, int offset) {
        if (bytes.length == 0 || bytes.length <= offset + 1)
            return -1;

        return (bytes[offset] & 0xff) + ((bytes[offset + 1] & 0xff) * 256);
    }

    /**
     * Конвертирует переданное число в 2-х байтовый массив.
     *
     * @param value число для форматирования
     * @return представление числа в виде двух байтов
     */
    public static byte[] convertToBytes(int value) {
        return new byte[]{
                (byte) (value),
                (byte) (value / 256),
        };
    }

}
