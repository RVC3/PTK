package ru.ppr.cppk.pd.check.write;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * Проверяет разовые ПД и абонементы на период в направлении туда/туда-обратно.
 *
 * @author Artem Ushakov
 */
public class OneOffAndSeasonForPeriodChecker implements Checker {

    private final TicketTypeRepository ticketTypeRepository;
    private final CommonSettings commonSettings;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final NsiVersionManager nsiVersionManager;
    private final TicketCategoryChecker ticketCategoryChecker;

    @Inject
    OneOffAndSeasonForPeriodChecker(TicketTypeRepository ticketTypeRepository,
                                    CommonSettings commonSettings,
                                    PdValidityPeriodCalculator pdValidityPeriodCalculator,
                                    NsiVersionManager nsiVersionManager,
                                    TicketCategoryChecker ticketCategoryChecker) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.commonSettings = commonSettings;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
        this.nsiVersionManager = nsiVersionManager;
        this.ticketCategoryChecker = ticketCategoryChecker;
    }

    @Override
    public boolean performCheck(PD pd, Date date) {
        Tariff tariff = pd.getTariff();

        if (tariff == null) {
            return true;
        }

        TicketType ticketType = ticketTypeRepository.load(tariff.getTicketTypeCode(), tariff.getVersionId());
        /*
         * Если validityPeriod == 1(день) то это означает, что данный пд
         * является разовым. Т.к. время действия разовых пд зависит от
         * направления(туда/туда-обратно), то производим дополнительное
         * вычисление дней действия
         */
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(pd.getSaleDate());
        startDate.add(Calendar.DAY_OF_YEAR, pd.term);

        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        int validityPeriod = pdValidityPeriodCalculator.calcValidityPeriod(startDate.getTime(), pd.wayType, ticketType, nsiVersion);

        int additionalActionTime = 0; //дополнительное время действия ПД (в часах)
        if (ticketCategoryChecker.isSingleTicket(ticketType.getTicketCategoryCode())) {
            additionalActionTime = commonSettings.getDurationOfPdNextDay();
        }

        // get start time pd
        Calendar endDateCalendar = Calendar.getInstance();

        endDateCalendar.setTime(startDate.getTime());
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endDateCalendar.set(Calendar.MINUTE, 0);
        endDateCalendar.set(Calendar.SECOND, 0);
        endDateCalendar.set(Calendar.MILLISECOND, 0);
        endDateCalendar.add(Calendar.DAY_OF_YEAR, validityPeriod);
        endDateCalendar.add(Calendar.HOUR_OF_DAY, additionalActionTime);

        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(date);

        return !(currentDate.equals(endDateCalendar) || currentDate.after(endDateCalendar));
    }
}
