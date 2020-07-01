package ru.ppr.cppk.legacy;

import ru.ppr.utils.CommonUtils;

public class EcpUtils {

    /**
     * Вернет рабочее значение ключа ЭЦП
     */
    public static long getEcpKeyNumberFromHex(byte[] keyNumberInHex) {

        long key = 0;

        if (keyNumberInHex != null) {
            try {
                int intKey = java.nio.ByteBuffer.wrap(keyNumberInHex).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                key = EcpUtils.convertIntToLong(intKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    /**
     * Вернет CashRegister.Id (DeviceId ) в виде целого числа
     */
    public static long getDeviceIdFromHex(byte[] DeviceIdInHex) {
        return getEcpKeyNumberFromHex(DeviceIdInHex);
    }

    /**
     * Вернет CashRegister.Id (DeviceId ) в виде HEX
     */
    public static byte[] getDeviceIdFromLong(long deviceIdLong) {
        return getEcpKeyNumberFromLong(deviceIdLong);
    }

    /**
     * Вернет значение ключа ЭЦП в HEX
     */
    public static byte[] getEcpKeyNumberFromLong(long keyNumberLong) {
        byte[] out = new byte[4];
        int keyNumberInt = EcpUtils.convertLongToInt(keyNumberLong);
        byte[] longHex = CommonUtils.generateByteArrayFromInt(keyNumberInt);
        System.arraycopy(longHex, 0, out, 0, 4);
        return out;
    }

    public static int convertLongToInt(long value) {
        if (value >> 32 != 0) {
            throw new IllegalArgumentException("Could not safe convert long to int");
        }
        int res = (int) value;
        return res;
    }

    public static long convertIntToLong(int value) {
        return value & 0xFFFFFFFFL;
    }
}
