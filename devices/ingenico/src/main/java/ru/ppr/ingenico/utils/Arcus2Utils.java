package ru.ppr.ingenico.utils;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

/**
 * @author Aleksandr Brazhkin
 */
public class Arcus2Utils {

    private static final byte SOH = 0x01;

    public static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");
    public static final Charset TERMINAL_OUT_CHARSET = Charset.forName("CP1251");

    private static final String ETH_PING = "eth_ping";

    /**
     * Проверяет, является ли сообщение ping'ом
     *
     * @param bytes Сообщение
     * @return {@code true} если это ping, {@code false} иначе
     */
    public static boolean isPing(byte[] bytes) {
        return new String(bytes, DEFAULT_CHARSET).equals(ETH_PING);
    }

    /**
     * Возвращает сообщение 'ping'
     *
     * @return ping
     */
    public static byte[] getPing() {
        return ETH_PING.getBytes(DEFAULT_CHARSET);
    }

    /**
     * Упаковывает данные по протоколу Arcus2
     *
     * @param data Данные для упаковки
     * @return Упакованные данные
     */
    public static byte[] packDefault(@NonNull byte[] data) {
        byte[] length = new byte[]{(byte) (data.length / 256), (byte) (data.length)};
        byte[] result = new byte[1 + length.length + data.length];

        //начальный байт пакета
        result[0] = SOH;
        //2 байта, содержащих размер передаваемых данных
        result[1] = length[0];
        result[2] = length[1];

        //передаваемые данные
        System.arraycopy(data, 0, result, 3, data.length);

        return result;
    }

    /**
     * Конвертирует строку в массив байтов
     *
     * @param s строка
     * @return Байтовое представление строки
     */
    public static byte[] convertStringToBytes(@NonNull String s) {
        return s.getBytes(DEFAULT_CHARSET);
    }
}
