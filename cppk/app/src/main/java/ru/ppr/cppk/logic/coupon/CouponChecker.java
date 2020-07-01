package ru.ppr.cppk.logic.coupon;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.ppr.kuznyechik.Kuznyechik;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PtsKey;
import ru.ppr.utils.CommonUtils;

/**
 * @author Dmitry Nevolin
 */
public class CouponChecker {

    private static final String TAG = Logger.makeLogTag(CouponChecker.class);

    private final PtsKeysProvider ptsKeysProvider;
    private final Kuznyechik kuznyechik;

    public CouponChecker(@NonNull PtsKeysProvider ptsKeysProvider, @NonNull Kuznyechik kuznyechik) {
        this.ptsKeysProvider = ptsKeysProvider;
        this.kuznyechik = kuznyechik;
    }

    @NonNull
    public Result check(long couponNumber) {
        Logger.trace(TAG, "check, couponNumber = " + couponNumber);
        int[] parsedCoupon = parseCoupon(couponNumber);
        int timestamp = parsedCoupon[1]; // 21 бит времени, берется из талона
        Logger.trace(TAG, "check, timestamp = " + timestamp);
        int terminalId = parsedCoupon[0]; // 16 бит суррогатного ID ТППД, берется из талона
        Logger.trace(TAG, "check, terminalId = " + terminalId);

        for (PtsKey ptsKey : ptsKeysProvider.provide(terminalId)) {
            int deviceId = (int) (ptsKey.getComplexInstanceId() & 0xFFFFFFFFL); // 4 байта DeviceId
            byte[] key = ptsKey.getKey();
            byte[] kuznyechikKey = new byte[key.length * 2];

            System.arraycopy(key, 0, kuznyechikKey, 0, key.length);
            System.arraycopy(key, 0, kuznyechikKey, key.length, key.length);

            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN); // как на устройстве, формирующем талон
            byteBuffer.putInt(timestamp);
            byteBuffer.putInt(terminalId);
            byteBuffer.putInt(deviceId);

            if (kuznyechik.init(kuznyechikKey, Kuznyechik.Direction.ENCRYPT)) {
                byte[] omac1 = new byte[16];
                // Получаенные 12 байт (16, как размер блока, в старших битах нули), затем шифруем Кузнечиком с использованием
                // 256 битового ключа (получаем удвоением текущего ключа), в режиме выработки имитовставки.
                kuznyechik.omac1(byteBuffer.array(), omac1);
                // В выходном значение 128 бит, берем первые 16 бит (опять же отличается byte order на устройстве, поэтому берем последние 2).
                int mac = (omac1[omac1.length - 1] & 0xFF) + ((omac1[omac1.length - 2] & 0xFF) << 8);
                // После получения 16 значного номера забеливаем его:
                // берем первые 37 бит (16 бит суррогатный ID ТППД | 21 бит время) и делаем XOR c 16 битами имитовставки, таким образом:
                // берем первые 16 бит делаем XOR, затем вторые 16 бит делаем XOR, зачем берем оставшиеся 5 бит и делаем XOR c первыми 5 битами имитовставки
                long actualCouponNumber = ((long) terminalId) + ((long) timestamp << 16);
                // делаем цикличный mac на 37 бит по паттерну выше, и делаем 1 раз xor
                actualCouponNumber = actualCouponNumber ^ (mac + ((long) mac << 16) + ((mac & 0x1FL) << 32));
                // добавляем 16 бит имитовставка (в начало)
                actualCouponNumber = mac + (actualCouponNumber << 16);
                // должно быть хотя бы одно совпадение
                if (couponNumber == actualCouponNumber) {
                    return new Result(true, timestamp, ptsKey);
                }
            } else {
                throw new IllegalStateException("kuznyechik.init == false with key: " + CommonUtils.bytesToHexWithoutSpaces(key));
            }
        }

        return new Result(false, timestamp, null);
    }

    private int[] parseCoupon(long couponNumber) {
        // младшие 16 бит - имитовставка, делаем из них цикличный mac на 37 бит
        long cycledMac = (couponNumber & 0xFFFF) + ((couponNumber & 0xFFFF) << 16) + ((couponNumber & 0x1F) << 32);
        // разбеливаем: средние 16 бит - суррогатный ID ТППД, старшие 21 бит - время
        long data = couponNumber >> 16 & 0x1FFFFFFFFFL ^ cycledMac;
        // в 0 элементе - разбеленый суррогатный ID ТППД, в 1 разбеленный timestamp
        return new int[]{(int) (data & 0xFFFF), (int) (data >> 16 & 0x1FFFFF)};
    }

    /**
     * Результат проверки, возвращает признак валидности
     */
    public static class Result {

        private final boolean valid;
        private final int timestamp;
        private final PtsKey ptsKey;

        public Result(boolean valid, int timestamp, @Nullable PtsKey ptsKey) {
            this.valid = valid;
            this.timestamp = timestamp;
            this.ptsKey = ptsKey;
        }

        public boolean isValid() {
            return valid;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public PtsKey getPtsKey() {
            return ptsKey;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "valid=" + valid +
                    ", timestamp=" + timestamp +
                    ", ptsKey=" + ptsKey +
                    '}';
        }
    }

}
