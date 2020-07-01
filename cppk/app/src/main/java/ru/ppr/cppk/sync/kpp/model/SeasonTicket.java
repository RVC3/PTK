package ru.ppr.cppk.sync.kpp.model;

/**
 * Абонемент
 *
 * @author Grigoriy Kashka
 */
public class SeasonTicket {

    /**
     * Счетчик проходов (сколько всего прошли по данном абоненменту)
     */
    public int PassCount;
    /**
     * Сколько осталось проходов
     */
    public int PassLeftCount;
    /**
     * Дни месяца действия абонемента (список чисел месяца с разделителем , )
     */
    public String MonthDays;

}
