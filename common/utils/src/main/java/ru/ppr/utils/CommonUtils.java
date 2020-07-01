package ru.ppr.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * Created by Артем on 13.01.2016.
 */
public class CommonUtils {

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

    /**
     * Конвертирует строку, представляющую набор байтов в 16с.с. в массив байтов
     *
     * @param s строка, представляющая массив байтов в 16 с.с.
     * @return массив байтов
     */
    public static byte[] hexStringToByteArray(String s) {
        if (s == null || s.isEmpty())
            return new byte[0];
        String tmpString = s.replace(" ", "");
        int len = tmpString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(tmpString.charAt(i), 16) << 4) + Character.digit(tmpString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Удаляет символы отличные от латинских и кирилических букв
     *
     * @param string исходные данные содержащие необработанные ФИО
     * @return обработанные данные, содержащие только латинские и кирилические
     * буквы
     */
    public static String makeCorrectString(String string) {
        char[] charsFIO = string.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < charsFIO.length; i++) {
            String letterString = Character.toString(charsFIO[i]);
            if (letterString.matches("^[а-яА-ЯёЁa-zA-Z0-9]+$"))
                builder.append(letterString);
        }
        return builder.toString();
    }

    /**
     * Конвертирует байты в шестнадцатиричную строку. При необходимости добавляет пробелы между байтами
     *
     * @param data           данные
     * @param withWhiteSpace флаг добавления пробелом между байтами
     * @return
     */
    @Deprecated
    public static String byteArrayToString(@Nullable byte[] data, boolean withWhiteSpace) {
        if (data == null) return "";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            String string = Integer.toHexString(data[i] & 0xFF);
            if (string.length() == 1)
                string = "0".concat(string);

            builder.append(string);
            if (withWhiteSpace) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * Конвертирует байты в шестнадцатиричную строку с пробелами
     *
     * @param data
     * @return
     */
    @Deprecated
    public static String byteArrayToString(@Nullable byte[] data) {

        return byteArrayToString(data, true);
    }

    @Deprecated
    public static String byteArrayToStringWithoutSpace(@Nullable byte[] data) {

        if (data == null) return "";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            String string = Integer.toHexString(data[i] & 0xFF);
            if (string.length() == 1)
                string = "0".concat(string);
            builder.append(string);
        }

        return builder.toString();
    }

    /**
     * Вернет побайтные десятичные значения разделенные запятой.
     *
     * @param hexArray
     * @return
     */
    public static String getDecStringFromByteArray(byte[] hexArray) {
        String returnVal = "";
        if (hexArray == null)
            return null;
        for (int i = 0; i < hexArray.length; i++) {
            returnVal += hexArray[i] & 0xFF;
            if (i < hexArray.length - 1)
                returnVal += ",";
        }
        return returnVal;
    }

    /**
     * Возвращает hex строку. Медленный, следует использовать {@link #bytesToHexWithoutSpaces(byte[]) bytesToHexWithoutSpaces}
     */
    @Deprecated
    public static String getHexString(byte[] md5Bytes) {
        String returnVal = "";
        if (md5Bytes == null)
            return null;
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    final static char space = ' ';

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null)
            return "";

        return bytesToHex(bytes, bytes.length);
    }

    private static String bytesToHex(byte[] bytes, int length) {
        if (bytes == null)
            return "";

        char[] hexChars = new char[length * 2];

        for (int i = 0; i < length; i++) {
            int value = bytes[i] & 0xFF;
            int index = i * 2;

            hexChars[index++] = hexArray[value >>> 4];
            hexChars[index] = hexArray[value & 0x0F];
        }

        return new String(hexChars);
    }

    public static String byteToHex(byte value) {
        char[] hexChars = new char[2];

        int intValue = value & 0xFF;
        hexChars[0] = hexArray[intValue >>> 4];
        hexChars[1] = hexArray[intValue & 0x0F];

        return new String(hexChars);
    }

    public static String bytesToHexWithSpaces(byte[] bytes) {
        if (bytes == null)
            return "";

        return bytesToHexWithSpaces(bytes, bytes.length);
    }

    public static String bytesToHexWithoutSpaces(byte[] bytes) {
        if (bytes == null)
            return "";
        return bytesToHexWithSpaces(bytes, bytes.length).replace(" ", "");
    }

    private static String bytesToHexWithSpaces(byte[] bytes, int length) {
        if (bytes == null)
            return "";

        char[] hexChars = new char[length * 3];

        for (int i = 0; i < length; i++) {
            int value = bytes[i] & 0xFF;
            int index = i * 3;

            hexChars[index++] = hexArray[value >>> 4];
            hexChars[index++] = hexArray[value & 0x0F];
            hexChars[index] = space;
        }

        return new String(hexChars);
    }

    public static byte[] generateByteArrayFromInt(int number) {

        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(number);
        return buffer.array();
    }

    /**
     * Генерирует массив байтов из числа. Порядок LITTLE_ENDIAN
     *
     * @param number число, которое необходимо представить массивом
     * @return
     */
    public static byte[] generateByteArrayFromLong(long number) {

        return generateByteArrayFromLong(number, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Генерирует массив байтов из числа. Порядок задается пользователем
     *
     * @param number число, которое необходимо представить массивом
     * @param order  порядок следования байтов
     * @return
     */
    public static byte[] generateByteArrayFromLong(long number, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(number);
        return buffer.array();
    }

    /**
     * вырезает подмассив
     *
     * @param data
     * @param startIndex
     * @param counByte
     * @return
     */
    public static byte[] getByteFromData(byte[] data, int startIndex, int counByte) {
        byte[] tmpData = new byte[counByte];
        System.arraycopy(data, startIndex, tmpData, 0, counByte);
        return tmpData;
    }

    /**
     * Произвоит перевод байта записанного в формате BCD в числовую строку
     *
     * @param bcd
     * @return
     */
    public static String BCDtoString(byte bcd) {

        StringBuilder sb = new StringBuilder();

        byte high = (byte) (bcd & 0xf0);
        high >>>= (byte) 4;
        high = (byte) (high & 0x0f);
        byte low = (byte) (bcd & 0x0f);

        sb.append(high);
        sb.append(low);

        return sb.toString();
    }

    /**
     * Произвоит перевод массива байтов записанных в формате BCD в числовую
     * строку
     *
     * @param bcd
     * @return
     */
    public static String BCDtoString(byte[] bcd) {

        StringBuilder sb = new StringBuilder();

        for (byte aBcd : bcd) {
            sb.append(BCDtoString(aBcd));
        }

        return sb.toString();
    }

    /**
     * Конвертирует интовое значение в boolean 1- true, остальное false
     *
     * @param value
     * @return
     */
    public static boolean convertInToBoolean(int value) {
        return value == 1;
    }

    /**
     * Проверяет, есть ли среди объектов null
     *
     * @param objects
     * @return
     */
    public static boolean nullIn(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет корректность номер СНИЛС
     *
     * @param snilsNumber
     * @return
     */
    public static boolean checkSnils(String snilsNumber) {

        if (snilsNumber == null) {
            return false;
        }

        String number = snilsNumber.trim().replace("-", "").replace(" ", "");

        boolean result = true;

        if (number.length() == Constants.SNILS_LENGHT) {

            int controlNumber = Integer.valueOf(number.substring(9, 11));
            int outerNumber = Integer.valueOf(number.substring(0, 9));

            if (outerNumber == 0) {
                // Запретить вводить все нули при вводе номера снилс
                // http://agile.srvdev.ru/browse/CPPKPP-40868
                return false;
            }

            if (outerNumber > Constants.SNILS_NUMBER_AFTER_CAN_CHECK) {
                //берем заново подстроку т.к. если переводить из outerNumber в String то будут отброшены не значащие нули
                String outerNumberString = number.substring(0, 9);
                //вычисляем сумму
                int sum = 0;
                for (int i = outerNumberString.length() - 1, j = 1; i >= 0; i--, j++) {
                    int digit = Integer.valueOf(Character.toString(outerNumberString.charAt(i)));
                    sum += j * digit;
                }

                //делим, пока больше 101
                while (sum > 101) {
                    sum = sum % 101;
                }

                //если сумма равна 100 или 101, то контрольное число
                if (sum == 101 || sum == 100) {
                    sum = 0;
                }

                //проверяем сумму с контрольным числом
                if (sum == controlNumber) {
                    result = true;
                } else {
                    result = false;
                }

            } else {
                result = true;
            }

        } else {
            result = false;
        }

        return result;
    }

    /**
     * Конвертирует массив байтов в текст
     *
     * @param data
     * @return
     */
    public static String getStringFromBytes(byte[] data) {
        String name = "error";
        try {
            name = IOUtils.toString(data, "Cp1251");
        } catch (IOException e) {
            e.printStackTrace();
            name = e.getMessage();
        }
        return name;
    }

    /**
     * Конвертирует байты в BitSet
     *
     * @param data
     * @return
     */
    public static BitSet toBitSet(@NonNull byte[] data) {

        if (data.length == 0) {
            return new BitSet();
        }

        long value = convertByteToLong(data, ByteOrder.LITTLE_ENDIAN);
        return toBitSet(value);
    }

    /**
     * Конвертирует значение типа long в битсет
     *
     * @param value
     * @return
     */
    public static BitSet toBitSet(long value) {

        BitSet bitSet = new BitSet();

        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bitSet.set(index);
            }
            ++index;
            value = value >>> 1;
        }

        return bitSet;
    }

    /**
     * Конвертирует BitSet в значение long
     *
     * @param bits
     * @return
     */
    public static long fromBitset(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    /**
     * Склеивает 2 битсета
     *
     * @param head           левая часть
     * @param tail           правая часть
     * @param offset         количество битов, которые будут взяты из правой части
     * @param countBitAppend количество битов, которые будут взяты из левой части
     * @return
     */
    public static BitSet appendBitSetToRight(BitSet head, BitSet tail, int offset, int countBitAppend) {

        if (head == null || tail == null) {
            throw new IllegalArgumentException("Incorrent paramers - bitset is null");
        }

        BitSet bitSet = tail.get(0, offset);
        for (int i = 0; i < countBitAppend; i++) {
            bitSet.set(offset + i, head.get(i));
        }
        return bitSet;
    }

    /**
     * Конвертирует BitSet в бинарную строку
     */
    public static String getString(BitSet bs) {
        return Long.toBinaryString(CommonUtils.fromBitset(bs));
    }


    /**
     * Конвертирует бинарную строку в BitSet
     */
    public static BitSet fromString(String s) {
        BitSet t = new BitSet(s.length());
        int lastBitIndex = s.length() - 1;
        for (int i = lastBitIndex; i >= 0; i--) {
            if (s.charAt(i) == '1') {
                t.set(lastBitIndex - i);
            }
        }
        return t;
    }
}
