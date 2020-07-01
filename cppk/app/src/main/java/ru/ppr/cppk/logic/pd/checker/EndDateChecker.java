package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * Проверка на дату окончания действия ПД
 *
 * @author Grigoriy Kashka
 */
public class EndDateChecker {

    private static final String TAG = Logger.makeLogTag(EndDateChecker.class);

    private final NsiDaoSession nsiDaoSession;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketCategoryChecker ticketCategoryChecker;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;

    @Inject
    EndDateChecker(NsiDaoSession nsiDaoSession,
                   TicketTypeRepository ticketTypeRepository,
                   TicketCategoryChecker ticketCategoryChecker,
                   PdValidityPeriodCalculator pdValidityPeriodCalculator) {
        this.nsiDaoSession = nsiDaoSession;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketCategoryChecker = ticketCategoryChecker;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
    }

    public boolean check(int nsiVersion,
                         @NonNull Date startPdTime,
                         @NonNull TicketWayType wayType,
                         int ticketTypeCode) {
        TicketType ticketType = ticketTypeRepository.load(ticketTypeCode, nsiVersion);
        int validityPeriod = pdValidityPeriodCalculator.calcValidityPeriod(startPdTime, wayType, ticketType, nsiVersion);
        int additionalActionTime = 0; //дополнительное время действия ПД (в часах)
        //если билет разовый, вне зависипости трансфер это или обычный ПД
        if (ticketCategoryChecker.isSingleTicket(ticketType.getTicketCategory(nsiDaoSession).getCode())) {
            // для разовых билетов расчитаем отдельно время действия
            // т.к. для билета туда-обратно время действия +1 рабочий день + задержка действия на следующий день
            additionalActionTime = Dagger.appComponent().commonSettingsStorage().get().getDurationOfPdNextDay();
        }

        Logger.trace(TAG, "checkEndDate() validityPeriod= " + validityPeriod);

        // get start time pd
        Calendar endDateCalendar = Calendar.getInstance();

        endDateCalendar.setTime(startPdTime);
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endDateCalendar.set(Calendar.MINUTE, 0);
        endDateCalendar.set(Calendar.SECOND, 0);
        endDateCalendar.set(Calendar.MILLISECOND, 0);
        endDateCalendar.add(Calendar.DAY_OF_YEAR, validityPeriod);
        endDateCalendar.add(Calendar.HOUR_OF_DAY, additionalActionTime);

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
