package ru.ppr.cppk.sync.kpp;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.ShiftClosureStatistics;
import ru.ppr.cppk.sync.kpp.model.EventsStatistic;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterWorkingShift extends CashRegisterEvent {

    /**
     * Дата закрытия смены
     * заполняется только для события закрытия смены
     */
    @Nullable
    public Date shiftEndDateTime;

    public Date operationDateTime;
    /**
     * Статус события характеризующего смену
     * <p>
     * [Description("Открыта")]
     * Started = 0,
     * <p>
     * [Description("Передана")]
     * Transferred = 5,
     * <p>
     * [Description("Закрыта")]
     * Ended = 10
     */
    public int status;

    /**
     * Количество событий по каждому типу
     */
    public EventsStatistic eventsStatistic;

    /**
     * Статистика по смене
     * заполняется только для события закрытия смены
     */
    public ShiftClosureStatistics shiftClosureStatistics;

    /**
     * Был ли сброс счётчика билетной ленты
     */
    public boolean paperCounterHasRestarted;

}
