package ru.ppr.cppk.helpers;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.ppr.nsi.entity.TicketTypesValidityTimes;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketTypeValidityTimeChecker {

    public TicketTypeValidityTimeChecker() {

    }

    public boolean isTicketTypeAllowedForTime(@NonNull List<TicketTypesValidityTimes> ticketTypesValidityTimesList,
                                              @NonNull Date currentDate) {

        if (ticketTypesValidityTimesList.isEmpty()) {
            return true;
        }

        Calendar validFromCalendar = Calendar.getInstance();
        Calendar validToCalendar = Calendar.getInstance();

        Calendar current = Calendar.getInstance();
        current.setTime(currentDate);

        for (TicketTypesValidityTimes ticketTypesValidityTimes : ticketTypesValidityTimesList) {
            int validFromSecond = ticketTypesValidityTimes.getValidFrom();
            int validToSecond = ticketTypesValidityTimes.getValidTo();

            validFromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            validFromCalendar.set(Calendar.MINUTE, 0);
            validFromCalendar.set(Calendar.SECOND, 0);
            validFromCalendar.set(Calendar.MINUTE, 0);
            validFromCalendar.add(Calendar.SECOND, validFromSecond);

            validToCalendar.set(Calendar.HOUR_OF_DAY, 0);
            validToCalendar.set(Calendar.MINUTE, 0);
            validToCalendar.set(Calendar.SECOND, 0);
            validToCalendar.set(Calendar.MINUTE, 0);
            validToCalendar.add(Calendar.SECOND, validToSecond);

            if (current.after(validFromCalendar) && current.before(validToCalendar)) {
                return true;
            }
        }

        return false;
    }
}
