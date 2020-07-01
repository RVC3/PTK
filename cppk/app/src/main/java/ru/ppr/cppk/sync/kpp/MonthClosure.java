package ru.ppr.cppk.sync.kpp;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.MonthClosureStatistics;

/**
 * Событие "Закрытие месяца"
 *
 * @author Grigoriy Kashka
 */
public class MonthClosure extends CashRegisterEvent {

    /**
     * Дата и время закрытия месяца, UTC
     */
    public Date closureDateTime;

    /**
     * Номер месяца (нумерация ведется с начала эксплуатации КО)
     */
    public int monthNumber;

    /**
     * Статистика
     */
    public MonthClosureStatistics statistics;

}
