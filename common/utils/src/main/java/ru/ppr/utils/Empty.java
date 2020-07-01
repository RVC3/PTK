package ru.ppr.utils;

/**
 * Алтернатива {@link Void}.
 * RxJava 2.x no longer accepts null values and the following will yield NullPointerException immediately or as a signal to downstream.
 * @author Aleksandr Brazhkin
 */
public enum Empty {
    INSTANCE
}
