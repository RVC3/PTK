package ru.ppr.utils;

/**
 * @author Aleksandr Brazhkin
 */
public class ObjectUtils {

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
