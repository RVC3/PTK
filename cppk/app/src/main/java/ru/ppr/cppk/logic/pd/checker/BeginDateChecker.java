package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.logic.utils.DateUtils;

/**
 * Проверка по дате начала действия ПД
 *
 * @author Grigoriy Kashka
 */
public class BeginDateChecker {

    @Inject
    public BeginDateChecker() {
    }

    public boolean check(@NonNull Date startPdTime) {
        Date currentTime = new Date();
        boolean checkState = false;
        Calendar currentCalendar = DateUtils.getStartOfDay(currentTime);
        Calendar startDateCalendar = DateUtils.getStartOfDay(startPdTime);
        if (currentCalendar.after(startDateCalendar) || currentCalendar.equals(startDateCalendar))
            checkState = true;
        return checkState;
    }

}
