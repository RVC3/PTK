package ru.ppr.cppk.entity.event.model34;

/**
 * Created by Кашка Григорий on 13.12.2015.
 * Абонемент
 */
public class SeasonTicket {

    /**
     * локальный идентификатор
     */
    private long id;
    /**
     * Счетчик проходов (сколько всего прошли по данном абоненменту)
     */
    private int passCount;
    /**
     * Сколько осталось проходов
     */
    private int passLeftCount;
    /**
     * Дни месяца действия абонемента (список чисел месяца с разделителем , )
     */
    private String monthDays;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getPassLeftCount() {
        return passLeftCount;
    }

    public void setPassLeftCount(int passLeftCount) {
        this.passLeftCount = passLeftCount;
    }

    public String getMonthDays() {
        return monthDays;
    }

    public void setMonthDays(String monthDays) {
        this.monthDays = monthDays;
    }
}
