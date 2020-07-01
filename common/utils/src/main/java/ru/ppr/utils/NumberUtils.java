package ru.ppr.utils;

/**
 * Вспомогательные функции для работы с числами.
 *
 * @author Aleksandr Brazhkin
 */
public class NumberUtils {

    /**
     * Возвращает значение, предствленное диапазоном битов во входном значении.
     *
     * @param value     Входное значение
     * @param bitsCount Количество извлекаемых битов
     * @param offset    Смещение
     * @return Значение, предствленное диапазоном битов
     */
    public static long getBits(final long value, final int bitsCount, final int offset) {
        long rightShifted = value >>> offset;
        long mask = (1L << bitsCount) - 1L;
        return rightShifted & mask;
    }

    /**
     * Возвращает значение, предствленное диапазоном битов во входном значении.
     *
     * @param value     Входное значение
     * @param bitsCount Количество извлекаемых битов
     * @param offset    Смещение
     * @return Значение, предствленное диапазоном битов
     */
    public static int getBits(final int value, final int bitsCount, final int offset) {
        int rightShifted = value >>> offset;
        int mask = (1 << bitsCount) - 1;
        return rightShifted & mask;
    }

    /**
     * Устанавливает {@code bitsCount} битов из {@code bits} в {@code valueToUpdate} в позиции {@code offset}.
     *
     * @param valueToUpdate Значение, в котором нужно обновить биты
     * @param bits          Значение, которое содержит исходные биты
     * @param bitsCount     Количество извлекаемых битов из {@code bits}
     * @param offset        Позиция установки {@code bits}
     * @return Обновленное значение {@code valueToUpdate}
     */
    public static int setBits(final int valueToUpdate, final int bits, final int bitsCount, final int offset) {
        int mask = (1 << bitsCount) - 1;
        int clearedValue = valueToUpdate & ~(mask << offset);
        int maskedBits = (bits & mask) << offset;
        return clearedValue | maskedBits;
    }

    /**
     * Compares two {@code long} values numerically.
     * The value returned is identical to what would be returned by:
     * <pre>
     *    Long.valueOf(x).compareTo(Long.valueOf(y))
     * </pre>
     *
     * @param x the first {@code long} to compare
     * @param y the second {@code long} to compare
     * @return the value {@code 0} if {@code x == y};
     * a value less than {@code 0} if {@code x < y}; and
     * a value greater than {@code 0} if {@code x > y}
     * @since 1.7
     */
    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
