package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.logic.utils.DateUtils;

/**
 * Валидатор начала срока действия СТУ.
 *
 * @author Aleksandr Brazhkin
 */
public class ValidityStartDateChecker {

    @Inject
    ValidityStartDateChecker() {

    }

    public boolean isStartDateValid(@NonNull ServiceTicketControlEvent serviceTicketControlEvent) {
        // Услуга действует с самого начала дня, даже если её продали вечером
        Date validFrom = DateUtils.getStartOfDay(serviceTicketControlEvent.getValidFrom()).getTime();
        return !validFrom.after(serviceTicketControlEvent.getControlDateTime());
    }
}
