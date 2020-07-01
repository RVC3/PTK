package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import java.util.Calendar;

import javax.inject.Inject;

import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.logic.utils.DateUtils;

/**
 * Валидатор окончания срока действия СТУ.
 *
 * @author Aleksandr Brazhkin
 */
public class ValidityEndDateChecker {

    private final CommonSettings commonSettings;

    @Inject
    ValidityEndDateChecker(CommonSettings commonSettings) {
        this.commonSettings = commonSettings;
    }

    public boolean isEndDateValid(@NonNull ServiceTicketControlEvent serviceTicketControlEvent) {
        if (serviceTicketControlEvent.getValidTo() == null) {
            return true;
        }
        // Услуга действует до конца дня + несколько часов заданных в общих настройках
        Calendar endCalendar = DateUtils.getEndOfDay(serviceTicketControlEvent.getValidTo());
        endCalendar.add(Calendar.HOUR_OF_DAY, commonSettings.getDurationOfPdNextDay());
        return !endCalendar.getTime().before(serviceTicketControlEvent.getControlDateTime());
    }
}
