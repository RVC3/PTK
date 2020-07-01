package ru.ppr.cppk.logic.interactor;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.nsi.entity.TicketType;

/**
 * Калькулятор количества дней действия ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdValidityPeriodCalculator {

    private final TicketTypeValidityPeriodCalculator ticketTypeValidityPeriodCalculator;
    private final SinglePdValidityPeriodCalculator singlePdValidityPeriodCalculator;
    private final TicketCategoryChecker ticketCategoryChecker;

    @Inject
    PdValidityPeriodCalculator(TicketTypeValidityPeriodCalculator ticketTypeValidityPeriodCalculator,
                               SinglePdValidityPeriodCalculator singlePdValidityPeriodCalculator,
                               TicketCategoryChecker ticketCategoryChecker) {
        this.ticketTypeValidityPeriodCalculator = ticketTypeValidityPeriodCalculator;
        this.singlePdValidityPeriodCalculator = singlePdValidityPeriodCalculator;
        this.ticketCategoryChecker = ticketCategoryChecker;
    }

    /**
     * Вычисляет количество дней действия ПД.
     *
     * @param pdStartDate Дата начала действия ПД
     * @param wayType     Направление ПД
     * @param ticketType  Тип ПД
     * @param nsiVersion  Версия НСИ
     * @return Количество дней действия ПД
     */
    public int calcValidityPeriod(@NonNull Date pdStartDate, @NonNull TicketWayType wayType, @NonNull TicketType ticketType, int nsiVersion) {
        int validityPeriod = ticketTypeValidityPeriodCalculator.getValidityPeriod(ticketType, pdStartDate);
        if (ticketCategoryChecker.isSingleTicket(ticketType.getTicketCategoryCode())) {
            // Т.к. время действия разовых ПД зависит от направления(туда/туда-обратно),
            // производим дополнительные вычисления
            return singlePdValidityPeriodCalculator.calcValidityPeriod(pdStartDate, wayType, nsiVersion);
        } else {
            return validityPeriod;
        }
    }
}
