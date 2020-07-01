package ru.ppr.cppk.utils;

import java.util.Collection;

/**
 * Created by Александр on 08.06.2016.
 */
public class CollectionUtils {

    public static long[] toPrimitives(Collection<Long> longCollection) {

        if (longCollection == null) {
            return null;
        }

        int i = 0;
        long[] primitives = new long[longCollection.size()];
        for (Long aLong : longCollection) {
            primitives[i++] = aLong;
        }

        return primitives;
    }
}
