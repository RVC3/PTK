package ru.ppr.cppk.pd.check.write;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.logic.pd.SeasonTicketForDaysValidityDaysCalculator;
import ru.ppr.cppk.logic.utils.DateUtils;

/**
 * Проверяет на валидность абонементы на даты, для продажи
 *
 * @author Grigoriy Kashka
 */
public class SeasonForDaysTicketForWriteChecker implements Checker {

    private final CommonSettings commonSettings;
    private final SeasonTicketForDaysValidityDaysCalculator seasonTicketForDaysValidityDaysCalculator;

    @Inject
    public SeasonForDaysTicketForWriteChecker(CommonSettings commonSettings,
                                              SeasonTicketForDaysValidityDaysCalculator seasonTicketForDaysValidityDaysCalculator) {
        this.commonSettings = commonSettings;
        this.seasonTicketForDaysValidityDaysCalculator = seasonTicketForDaysValidityDaysCalculator;
    }

    @Override
    public boolean performCheck(PD pd, Date date) {
        Date lastDay = seasonTicketForDaysValidityDaysCalculator.getLastValidityDay(pd.getStartPdDate(), (int) pd.actionDays);
        if (lastDay == null)
            return false;

        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(date);

        Calendar endCalendar = DateUtils.getEndOfDay(lastDay);
        endCalendar.add(Calendar.HOUR_OF_DAY, commonSettings.getDurationOfPdNextDay());

        return currentDate.equals(endCalendar) || currentDate.before(endCalendar);
    }
}
