package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import ru.ppr.cppk.sync.kpp.model.WorkingShift;

/**
 * Итоги месяца
 *
 * @author Grigoriy Kashka
 */
public class MonthClosureStatistics extends ClosureStatistics {
    /**
     * Первая смена в месяце
     */
    public WorkingShift monthOpenShift;

    /**
     * Последняя смена в месяце
     */
    public WorkingShift monthCloseShift;
}
