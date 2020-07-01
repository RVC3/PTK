package ru.ppr.cppk.ui.activity.controlreadbsc.utils;


import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Класс для проверки метки прохода
 *
 * @author Sergey Kolesnikov
 */

public class PassageMarkTroykaVerificator implements VerificationPassMark {


    @Inject
    PassageMarkTroykaVerificator() {

    }


    /**
     *  Проверяет время нахождения с моментом входа. Не должно привышать 5 часов
     */

    private boolean isCheckTimeStationOut(long intersection_time) {
        long date = System.currentTimeMillis();
        long intersection_millis = intersection_time - TimeUnit.MINUTES.toMillis(5);
        long diff_time = date - intersection_time;
        return intersection_millis < date && diff_time < TimeUnit.HOURS.toMillis(5);
    }


    /**
     *  Валидность нахождения с момента входа
     */

    @Override
    public boolean isValid(boolean isExitStationCode, long intersectionTime, boolean validImitovstavka) {
        return validImitovstavka && isExitStationCode && isCheckTimeStationOut(intersectionTime);
    }

}
