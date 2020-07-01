package ru.ppr.ingenico.utils;

import android.support.annotation.NonNull;

/**
 * Класс-утилита для выполнения кодирования/декодирования рациональных чисел в форме BCD.
 * Подробное о BCD смотреть по ссылке https://en.wikipedia.org/wiki/Binary-coded_decimal
 *
 * @author Dmitry Nevolin
 */
public class BCD {

    private static final byte MASK_LSB = 0xf; //0b00001111
    private static final byte MASK_MSB = (byte) 0xf0; //0b11110000

    private static final byte[] SPECIAL_VALUES = new byte[]{
            0xa, //0b00001010
            0xb, //0b00001011
            0xc, //0b00001100
            0xd, //0b00001101
            0xe, //0b00001110
            0xf  //0b00001111
    };

    /**
     * Конвертирует число в BCD представлении в {@code int}
     *
     * @param source массив байтов, содержащий число в BCD представлении
     * @return число в десятичном представлении
     */
    public static int toInt(@NonNull byte[] source) {
        int result = 0;
        int radix = 1;

        for (int i = source.length - 1; i >= 0; i--) {
            int lsb = source[i] & MASK_LSB;

            if (!isSpecial(lsb)) {
                result += lsb * radix;
                radix *= 10;
            }

            int msb = ((source[i] & MASK_MSB) >> 4) & MASK_LSB;

            if (!isSpecial(msb)) {
                result += msb * radix;
                radix *= 10;
            }
        }

        return result;
    }

    /**
     * Конвертирует число в BCD представлении в {@code String}
     *
     * @param source массив байтов, содержащий число в BCD представлении
     * @return число в десятичном представлении в строковой форме
     */
    public static String toString(@NonNull byte[] source) {
        String result = "";

        for (byte tmp : source) {
            int msb = ((tmp & MASK_MSB) >> 4) & MASK_LSB;

            if (!isSpecial(msb))
                result += msb;

            int lsb = tmp & MASK_LSB;

            if (!isSpecial(lsb))
                result += lsb;
        }

        return result;
    }

    /**
     * Проверяет, является ли комбинация бит запрещенной для BCD.
     *
     * @param value число, для которого проверяются последние 4 бита.
     * @return {@code true}, если комбинация бит является запрещенной, {@code false} иначе
     */
    private static boolean isSpecial(int value) {
        for (byte tmp : SPECIAL_VALUES)
            if (value == tmp)
                return true;

        return false;
    }

}
