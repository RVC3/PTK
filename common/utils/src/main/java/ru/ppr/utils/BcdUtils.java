package ru.ppr.utils;

/**
 * @author Aleksandr Brazhkin
 */
public class BcdUtils {

    /**
     * Произвоит перевод байта записанного в формате BCD в десятичное значение
     *
     * @param bcd
     * @return
     */
    public static int bcdToInt(byte bcd) {

        byte high = (byte) (bcd & 0xf0);
        high >>>= (byte) 4;
        high = (byte) (high & 0x0f);
        byte low = (byte) (bcd & 0x0f);

        int value = (high * 10 + low);

        return value;
    }

    /**
     * Произвоит перевод байтов записанного в формате BCD в десятичное значение
     *
     * @param bcd
     * @return
     */
    public static int bcdToInt(byte[] bcd) {
        int value = 0;
        for (int i = 0; i < bcd.length; i++) {
            value = value * 100 + bcdToInt(bcd[i]);
        }
        return value;
    }

    /**
     * Произвоит перевод массива байтов записанных в формате BCD в числовую строку
     *
     * @param bcd
     * @return
     */
    public static String bcdToString(byte[] bcd) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bcd.length; i++) {
            int value = bcdToInt(bcd[i]);
            if (value < 10) {
                sb.append('0');
            }
            sb.append(value);
        }

        return sb.toString();
    }
}
