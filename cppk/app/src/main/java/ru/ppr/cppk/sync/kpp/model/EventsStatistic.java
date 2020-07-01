package ru.ppr.cppk.sync.kpp.model;

import android.support.annotation.Nullable;

/**
 * События, присылаемые ПТК при закрытии смены
 *
 * @author Grigoriy Kashka
 */
public class EventsStatistic {

    /**
     * Количество проверенных билетов
     */
    @Nullable
    public Integer ticketControlEventsCount;

    /**
     * Количество аннулированных билетов
     */
    @Nullable
    public Integer ticketAnnulledEventsCount;

    /**
     * Количество проданных билетов
     */
    @Nullable
    public Integer ticketSaleEventsCount;

    /**
     * количество тестовых ПД
     */
    @Nullable
    public Integer testTicketsEventsCount;

    /**
     * количество событий связаннх с Сменами
     * (за смену минимум 2 события - открытие и закрытие, если есть передача смены - то 3 и больше)
     */
    @Nullable
    public Integer shiftEventsCount;

    @Nullable
    public Integer bankTransactionCashRegisterEventsCount;

    @Nullable
    public Integer ticketPaperRollEventsCountsCount;

    /**
     * Количество событий оформления (оплаты) штрафов
     */
    @Nullable
    public Integer finePaidEventsCount;

    /**
     * Количество событий прохода по служебной карте
     */
    @Nullable
    public Integer serviceTicketControlEventsCount;

}
