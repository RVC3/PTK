package ru.ppr.cppk.ui.activity.controlreadbsc.utils;

/**
 * Проверка валидности метки прохода
 */
public interface VerificationPassMark {

    /**
     * Валидность прохода
     */

    boolean isValid(boolean code_station_exit, long intersection_time, boolean validImitovstavka);
}
