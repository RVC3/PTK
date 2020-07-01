package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ServiceFee;

/**
 * Проверка на дату окончания действия услуги
 *
 * @author Dmitry Nevolin
 */
public class ServiceFeeEndDateChecker {

    private static final String TAG = Logger.makeLogTag(ServiceFeeEndDateChecker.class);

    @Inject
    ServiceFeeEndDateChecker() {

    }

    public boolean check(@NonNull Date startPdTime, @NonNull ServiceFee serviceFee) {
        // Если null значит срок действия неограничен (http://agile.srvdev.ru/browse/CPPKPP-36175)
        if (serviceFee.getValidityPeriod() == null) {
            return true;
        }

        int validityPeriod = serviceFee.getValidityPeriod();

        Logger.trace(TAG, "check() validityPeriod= " + validityPeriod);

        // get start time pd
        Calendar endDateCalendar = Calendar.getInstance();

        endDateCalendar.setTime(startPdTime);
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endDateCalendar.set(Calendar.MINUTE, 0);
        endDateCalendar.set(Calendar.SECOND, 0);
        endDateCalendar.set(Calendar.MILLISECOND, 0);
        endDateCalendar.add(Calendar.DAY_OF_YEAR, validityPeriod);

        Calendar currentTime = Calendar.getInstance();

        Logger.trace(TAG, "checkEndDate() startFromDate=" + startPdTime);
        Logger.trace(TAG, "checkEndDate() currentTime=" + currentTime.getTimeInMillis() + " endDateCalendar=" + endDateCalendar.getTimeInMillis());

        if (currentTime.equals(endDateCalendar) || currentTime.after(endDateCalendar)) {
            Logger.trace(TAG, "checkEndDate() PD time is end");
            return false;
        }

        return true;
    }

}
