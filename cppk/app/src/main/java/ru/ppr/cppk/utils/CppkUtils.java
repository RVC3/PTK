package ru.ppr.cppk.utils;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import ru.ppr.utils.CommonUtils;

public class CppkUtils {

    /**
     * Ковертирует то что мы читаем с карты как rfidAttr в то что приходит в
     * качестве CrystallNumber в таблице стоплистов
     *
     * @param cardUID
     * @return
     */
    public static String convertCardUIDToStopListNumber(byte[] cardUID) {
        byte[] crystalNumber = Arrays.copyOf(cardUID, 8);
        ByteBuffer buffer = ByteBuffer.wrap(crystalNumber).order(ByteOrder.LITTLE_ENDIAN);
        return String.valueOf(buffer.getLong());
    }

    /**
     * Сравнивает rfidAttr карт
     *
     * @param first  рфид атрибут первой карты
     * @param second рфид атрибут второй карты
     * @return
     */
    public static boolean equalsRfidAttr(@NonNull String first, @NonNull String second) {
        byte[] firstArray = CommonUtils.hexStringToByteArray(first);
        byte[] secondArray = CommonUtils.hexStringToByteArray(second);
        return equalsRfidAttr(firstArray, secondArray);
    }

    /**
     * Сравнивает rfidAttr карт
     *
     * @param first  рфид атрибут первой карты
     * @param second рфид атрибут второй карты
     * @return
     */
    public static boolean equalsRfidAttr(@NonNull byte[] first, @NonNull byte[] second) {
        return Arrays.equals(first, second);
    }

    public static boolean equalsRfidCrystalNumber(@NonNull String first, @NonNull String second) {
        return first.equalsIgnoreCase(second);
    }

    /**
     * Выполняет проверку нахождения обоих дат в 1 дне
     *
     * @param checkDate   дата для првоерки
     * @param currentDate текущая дата
     * @return true если даты находятся в 1 дне, иначе false
     */
    public static boolean datesInOneDay(Date checkDate, Date currentDate) {

        Calendar checkCalendar = Calendar.getInstance();
        checkCalendar.setTime(checkDate);

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        return checkCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                && checkCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                && checkCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);
    }
}
