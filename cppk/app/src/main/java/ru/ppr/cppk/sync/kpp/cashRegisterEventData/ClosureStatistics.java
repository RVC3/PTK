package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import android.support.annotation.Nullable;

import java.math.BigDecimal;

/**
 * Статистика по закрытию смены/месяца
 *
 * @author Grigoriy Kashka
 */
public class ClosureStatistics {
    /**
     * Детские разовые ПД в направлении «Туда-обратно» за вычетом аннулирования
     * с каждого неотозванного билета сюда попадает значение : цена + сбор
     */
    public FeeExemptionOperationsSummary twoWayChildTicketSaleExceptAnnulled;

    /**
     * Разовые ПД в направлении «Туда» за вычетом аннулирования
     * <p>
     * ticketCategoryCode == 1
     * TicketKind.Full или TicketKind.WithExemption
     * TicketWayType.OneWay
     * с каждого неотозванного билета сюда попадает значение : цена + сбор
     */
    public FeeExemptionOperationsSummary oneWayTicketSaleExceptAnnulled;

    /**
     * Разовые ПД в направлении «Туда-обратно» за вычетом аннулирования
     * <p>
     * ticketCategoryCode == 1
     * TicketKind.Full или TicketKind.WithExemption
     * TicketWayType.TwoWay
     * с каждого неотозванного билета сюда попадает значение : цена + сбор
     */
    public FeeExemptionOperationsSummary twoWayTicketSaleExceptAnnulled;


    /**
     * Сумма выручки по банковским картам за вычетом аннулированных
     * Пока всегда 0, т.к. нет реализации оплаты банковской картой
     * А так, если смотреть по наличным, логика тут следующая:
     * с каждого неотозванного
     * билета сюда попадает значение : цена + сбор
     */
    public BigDecimal totalSmartCardExceptAnnulled;

    /**
     * Детские разовые ПД в направлении «Туда» за вычетом аннулирования
     */
    //ticketCategoryCode == 1
    //TicketKind.Child
    //TicketWayType.OneWay
    //с каждого неотозванного билета сюда попадает значение : цена + сбор
    public FeeExemptionOperationsSummary oneWayChildTicketSaleExceptAnnulled;

    /**
     * Расход билетной ленты за смену/месяц (Метраж полностью израсходованных бобин), миллиметры
     */
    public int tapeLength;

    /**
     * Количество полностью израсходованных бобин чековой ленты
     * Это количество бобин, которое в принипе побывало в принтере за смену
     */
    public int finishedTapesCount;


    /**
     * Сумма выручки по Багажные квитанции за вычетом аннулированных
     * ticketCategoryCode == 2
     * с каждого неотозванной квитанции сюда попадает значение : цена + сбор
     */
    public FeeTaxOperationsSummary luggageTicketExceptAnnulled;

    /**
     * Оформленные (оплаченные) штрафы
     */
    public TaxOperationsSummary finesPaid;

    /**
     * Трансферы
     */
    public FeeExemptionOperationsSummary transfers;

    /**
     * НДС за сборы за оформление ПД, руб
     */
    @Nullable
    public BigDecimal feesTax;

    /**
     * Сумма сборов, руб
     * c каждого билета (независимо от каких либо условий, в т.ч статуса аннулирования)
     * сюда попадает сумма сбора
     */
    @Nullable
    public BigDecimal feesAmount;

    /**
     * Выручка за месяц всего
     */
    public BigDecimal totalIncome;

    /**
     * Выручка за месяц всего, за вычетом аннулирования, руб
     */
    public BigDecimal totalIncomeExceptAnnulled;

    /**
     * Сумма выручки за месяц наличными за вычетом аннулированных, руб
     */
    public BigDecimal totalCashExceptAnnulled;
}
