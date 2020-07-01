package ru.ppr.cppk.utils;

/**
 * Created by Александр on 08.06.2016.
 */
public class ArrayUtils {

    public static long[] toPrimitives(Long... longs) {

        if (longs == null) {
            return null;
        }

        long[] primitives = new long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            primitives[i] = longs[i];
        }

        return primitives;
    }

    public static long[] splitToLongs(String longsAsString) {
        if (longsAsString == null) {
            return null;
        }
        String[] longsAsStringArray = longsAsString.split(",");
        int length = longsAsStringArray.length;
        long[] longs = new long[length];
        for (int i = 0; i < length; i++) {
            longs[i] = Long.parseLong(longsAsStringArray[i]);
        }
        return longs;
    }

    public static String concatToString(long[] longs) {
        if (longs == null) {
            return null;
        }
        int length = longs.length;
        StringBuilder longsAsString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                longsAsString.append(",");
            }
            longsAsString.append(longs[i]);
        }
        return longsAsString.toString();
    }
}
