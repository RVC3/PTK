package ru.ppr.utils;

import java.math.BigInteger;

/**
 * Функции для работы с unsigned Long.
 * Copy-past методов из Java 8.
 *
 * @author Aleksandr Brazhkin
 */
public class UnsignedLongUtils {

    /**
     * All possible chars for representing a number as a String
     */
    private final static char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    /**
     * Parses the string argument as an unsigned {@code long} in the
     * radix specified by the second argument.  An unsigned integer
     * maps the values usually associated with negative numbers to
     * positive numbers larger than {@code MAX_VALUE}.
     * <p>
     * The characters in the string must all be digits of the
     * specified radix (as determined by whether {@link
     * java.lang.Character#digit(char, int)} returns a nonnegative
     * value), except that the first character may be an ASCII plus
     * sign {@code '+'} ({@code '\u005Cu002B'}). The resulting
     * integer value is returned.
     * <p>
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
     * <p>
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     * <p>
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a plus sign
     * {@code '+'} ({@code '\u005Cu002B'}) provided that the
     * string is longer than length 1.
     * <p>
     * <li>The value represented by the string is larger than the
     * largest unsigned {@code long}, 2<sup>64</sup>-1.
     * <p>
     * </ul>
     *
     * @param s     the {@code String} containing the unsigned integer
     *              representation to be parsed
     * @param radix the radix to be used while parsing {@code s}.
     * @return the unsigned {@code long} represented by the string
     * argument in the specified radix.
     * @throws NumberFormatException if the {@code String}
     *                               does not contain a parsable {@code long}.
     * @since 1.8
     */
    public static long parseUnsignedLong(String s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                        NumberFormatException(String.format("Illegal leading minus sign " +
                        "on unsigned string %s.", s));
            } else {
                if (len <= 12 || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                        (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                    return Long.parseLong(s, radix);
                }

                // No need for range checks on len due to testing above.
                long first = Long.parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    throw new NumberFormatException("Bad digit at end of " + s);
                }
                long result = first * radix + second;
                if (compareUnsigned(result, first) < 0) {
                    /*
                     * The maximum unsigned value, (2^64)-1, takes at
                     * most one more digit to represent than the
                     * maximum signed value, (2^63)-1.  Therefore,
                     * parsing (len - 1) digits will be appropriately
                     * in-range of the signed parsing.  In other
                     * words, if parsing (len -1) digits overflows
                     * signed parsing, parsing len digits will
                     * certainly overflow unsigned parsing.
                     *
                     * The compareUnsigned check above catches
                     * situations where an unsigned overflow occurs
                     * incorporating the contribution of the final
                     * digit.
                     */
                    throw new NumberFormatException(String.format("String value %s exceeds " +
                            "range of unsigned long.", s));
                }
                return result;
            }
        } else {
            throw forInputString(s);
        }
    }

    /**
     * Compares two {@code long} values numerically treating the values
     * as unsigned.
     *
     * @param x the first {@code long} to compare
     * @param y the second {@code long} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     * than {@code 0} if {@code x < y} as unsigned values; and
     * a value greater than {@code 0} if {@code x > y} as
     * unsigned values
     * @since 1.8
     */
    private static int compareUnsigned(long x, long y) {
        return compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
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
    private static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Factory method for making a <code>NumberFormatException</code>
     * given the specified input which caused the error.
     *
     * @param s the input causing the error
     */
    private static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }

    /**
     * Returns a string representation of the first argument as an
     * unsigned integer value in the radix specified by the second
     * argument.
     * <p>
     * <p>If the radix is smaller than {@code Character.MIN_RADIX}
     * or larger than {@code Character.MAX_RADIX}, then the radix
     * {@code 10} is used instead.
     * <p>
     * <p>Note that since the first argument is treated as an unsigned
     * value, no leading sign character is printed.
     * <p>
     * <p>If the magnitude is zero, it is represented by a single zero
     * character {@code '0'} ({@code '\u005Cu0030'}); otherwise,
     * the first character of the representation of the magnitude will
     * not be the zero character.
     * <p>
     * <p>The behavior of radixes and the characters used as digits
     * are the same as {@link Long#toString(long, int) toString}.
     *
     * @param i     an integer to be converted to an unsigned string.
     * @param radix the radix to use in the string representation.
     * @return an unsigned string representation of the argument in the specified radix.
     * @see Long#toString(long, int)
     * @since 1.8
     */
    public static String toUnsignedString(long i, int radix) {
        if (i >= 0)
            return Long.toString(i, radix);
        else {
            switch (radix) {
                case 2:
                    return Long.toBinaryString(i);

                case 4:
                    return toUnsignedString0(i, 2);

                case 8:
                    return Long.toOctalString(i);

                case 10:
                    /*
                     * We can get the effect of an unsigned division by 10
                     * on a long value by first shifting right, yielding a
                     * positive value, and then dividing by 5.  This
                     * allows the last digit and preceding digits to be
                     * isolated more quickly than by an initial conversion
                     * to BigInteger.
                     */
                    long quot = (i >>> 1) / 5;
                    long rem = i - quot * 10;
                    return Long.toString(quot) + rem;

                case 16:
                    return Long.toHexString(i);

                case 32:
                    return toUnsignedString0(i, 5);

                default:
                    return toUnsignedBigInteger(i).toString(radix);
            }
        }
    }

    /**
     * Return a BigInteger equal to the unsigned value of the
     * argument.
     */
    private static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L)
            return BigInteger.valueOf(i);
        else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            // return (upper << 32) + lower
            return (BigInteger.valueOf(toUnsignedLong(upper))).shiftLeft(32).
                    add(BigInteger.valueOf(toUnsignedLong(lower)));
        }
    }

    /**
     * Format a long (treated as unsigned) into a String.
     *
     * @param val   the value to format
     * @param shift the log2 of the base to format in (4 for hex, 3 for octal, 1 for binary)
     */
    private static String toUnsignedString0(long val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Long.SIZE - Long.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedLong(val, shift, buf, 0, chars);
        return new String(buf);
    }

    /**
     * Format a long (treated as unsigned) into a character buffer.
     *
     * @param val    the unsigned long to format
     * @param shift  the log2 of the base to format in (4 for hex, 3 for octal, 1 for binary)
     * @param buf    the character buffer to write to
     * @param offset the offset in the destination buffer to start at
     * @param len    the number of characters to write
     * @return the lowest character location used
     */
    private static int formatUnsignedLong(long val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = digits[((int) val) & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }

    /**
     * Converts the argument to a {@code long} by an unsigned
     * conversion.  In an unsigned conversion to a {@code long}, the
     * high-order 32 bits of the {@code long} are zero and the
     * low-order 32 bits are equal to the bits of the integer
     * argument.
     * <p>
     * Consequently, zero and positive {@code int} values are mapped
     * to a numerically equal {@code long} value and negative {@code
     * int} values are mapped to a {@code long} value equal to the
     * input plus 2<sup>32</sup>.
     *
     * @param x the value to convert to an unsigned {@code long}
     * @return the argument converted to {@code long} by an unsigned
     * conversion
     * @since 1.8
     */
    private static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }
}
