package ru.ppr.cppk.logic.pd.checker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.logic.pd.SeasonTicketForDaysValidityDaysCalculator;
import ru.ppr.cppk.logic.utils.DateUtils;

/**
 * Валидирует абонемент на даты для контроля
 *
 * @author Grigoriy Kashka
 */
public class SeasonForDaysTicketForControlChecker {

    private final SeasonTicketForDaysValidityDaysCalculator seasonTicketForDaysValidityDaysCalculator;
    private final CommonSettingsStorage commonSettingsStorage;

    @Inject
    public SeasonForDaysTicketForControlChecker(SeasonTicketForDaysValidityDaysCalculator seasonTicketForDaysValidityDaysCalculator,
                                                CommonSettingsStorage commonSettingsStorage) {
        this.seasonTicketForDaysValidityDaysCalculator = seasonTicketForDaysValidityDaysCalculator;
        this.commonSettingsStorage = commonSettingsStorage;
    }

    /**
     * Вернет true если билет валиден на текущую дату
     *
     * @param dates
     * @return
     */
    public boolean check(List<Date> dates, Date dateForCheck) {
        for (Date date : dates) {

            Calendar currentDate = Calendar.getInstance();
            currentDate.setTime(dateForCheck);

            Calendar beginCalendar = DateUtils.getStartOfDay(date);
            beginCalendar.add(Calendar.MILLISECOND, -1);

            Calendar endCalendar = DateUtils.getEndOfDay(date);
            endCalendar.add(Calendar.HOUR_OF_DAY, commonSettingsStorage.get().getDurationOfPdNextDay());
            endCalendar.add(Calendar.MILLISECOND, 1);

            if (currentDate.after(beginCalendar) && currentDate.before(endCalendar))
                return true;

        }
        return false;
    }

    /**
     * Вернет true если билет валиден на текущую дату
     *
     * @param pdStartDate - время начала действия ПД
     * @param days        - дни действия ПД
     * @return
     */
    public boolean check(Date pdStartDate, int days, Date dateForCheck) {
        List<Date> daysList = seasonTicketForDaysValidityDaysCalculator.getValidityDays(pdStartDate, days);
        return check(daysList, dateForCheck);
    }

}

