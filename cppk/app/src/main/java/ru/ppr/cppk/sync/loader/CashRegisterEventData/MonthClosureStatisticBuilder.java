package ru.ppr.cppk.sync.loader.CashRegisterEventData;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import ru.ppr.cppk.data.summary.FineSaleStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.MonthClosureStatistics;
import ru.ppr.cppk.sync.kpp.model.WorkingShift;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Grigoriy Kashka
 */
public class MonthClosureStatisticBuilder extends BaseLoader {

    private final NsiVersionManager nsiVersionManager;
    private final FineRepository fineRepository;
    private final FeeTaxOperationsSummaryBuilder feeTaxOperationsSummaryBuilder;
    private final TaxOperationsSummaryBuilder taxOperationsSummaryBuilder;
    private final FeeExemptionOperationsSummaryBuilder feeExemptionOperationsSummaryBuilder;


    public MonthClosureStatisticBuilder(LocalDaoSession localDaoSession,
                                        NsiDaoSession nsiDaoSession,
                                        NsiVersionManager nsiVersionManager,
                                        FineRepository fineRepository,
                                        FeeTaxOperationsSummaryBuilder feeTaxOperationsSummaryBuilder,
                                        TaxOperationsSummaryBuilder taxOperationsSummaryBuilder,
                                        FeeExemptionOperationsSummaryBuilder feeExemptionOperationsSummaryBuilder) {
        super(localDaoSession, nsiDaoSession);
        this.nsiVersionManager = nsiVersionManager;
        this.fineRepository = fineRepository;
        this.feeTaxOperationsSummaryBuilder = feeTaxOperationsSummaryBuilder;
        this.taxOperationsSummaryBuilder = taxOperationsSummaryBuilder;
        this.feeExemptionOperationsSummaryBuilder = feeExemptionOperationsSummaryBuilder;
    }

    /**
     * Сформировать объект статистики
     *
     * @param monthId           - идентификатор месяца
     * @param firstWorkingShift - первое событие открытия смены в месяце
     * @param lastWorkingShift  - последнее событие закрытия смены в месяце
     * @return
     */
    public MonthClosureStatistics build(@NonNull String monthId, @NonNull WorkingShift firstWorkingShift, @NonNull WorkingShift lastWorkingShift) {

        MonthClosureStatistics monthClosureStatistics = new MonthClosureStatistics();

        ////начало подготовки данных
        PdStatisticsBuilder.Statistics pdStatistics;
        TicketTapeStatisticsBuilder.Statistics ticketTapeStatistics;

        pdStatistics = new PdStatisticsBuilder(nsiVersionManager.getCurrentNsiVersionId())
                .setMonthId(monthId)
                .build();

        ticketTapeStatistics = new TicketTapeStatisticsBuilder(localDaoSession)
                .setMonthId(monthId)
                .build();

        FineSaleStatisticsBuilder.Statistics fineSaleStatistics = new FineSaleStatisticsBuilder(localDaoSession, fineRepository)
                .setMonthId(monthId)
                .build();
        ////конец подготовки данных


        //Детские разовые ПД в направлении «Туда-обратно» за вычетом аннулирования
        PdStatisticsBuilder.CountAndProfit cProfit = pdStatistics.ticketTypeChildStatistics.directionThereBackStatistics.presenceOfExemptionStatistics.countAndProfit;
        monthClosureStatistics.twoWayChildTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        //Разовые ПД в направлении «Туда» за вычетом аннулирования
        cProfit = pdStatistics.ticketTypeFullStatistics.directionThereStatistics.presenceOfExemptionStatistics.countAndProfit;
        monthClosureStatistics.oneWayTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        //Разовые ПД в направлении «Туда-обратно» за вычетом аннулирования
        cProfit = pdStatistics.ticketTypeFullStatistics.directionThereBackStatistics.presenceOfExemptionStatistics.countAndProfit;
        monthClosureStatistics.twoWayTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        //Сумма выручки по банковским картам за вычетом аннулированных
        monthClosureStatistics.totalSmartCardExceptAnnulled = pdStatistics.countAndProfit.profit.totalCardPaymentSum.subtract(pdStatistics.countAndProfit.profit.totalCardPaymentSumRepeal);

        //Детские разовые ПД в направлении «Туда» за вычетом аннулирования
        cProfit = pdStatistics.ticketTypeChildStatistics.directionThereStatistics.presenceOfExemptionStatistics.countAndProfit;
        monthClosureStatistics.oneWayChildTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        //Расход билетной ленты, мм
        monthClosureStatistics.tapeLength = (int) ticketTapeStatistics.finishedTicketTapeConsumptionInMillimeters;

        //Количество закончившихся бобин чековой ленты
        monthClosureStatistics.finishedTapesCount = localDaoSession.getTicketTapeEventDao().getFinishedTicketTapeEventsForMonth(monthId, EnumSet.of(ShiftEvent.Status.ENDED), true).size();

        //Сумма выручки по Багажные квитанции за вычетом аннулированных
        monthClosureStatistics.luggageTicketExceptAnnulled = feeTaxOperationsSummaryBuilder.build(pdStatistics.ticketTypeBaggageStatistics.countAndProfit);

        //Статискика по штрафам
        monthClosureStatistics.finesPaid = taxOperationsSummaryBuilder.build(fineSaleStatistics);

        // Трансферы за вычетом аннулирования
        cProfit = pdStatistics.ticketTypeTransferStatistics.countAndProfit;
        monthClosureStatistics.transfers = feeExemptionOperationsSummaryBuilder.build(cProfit);

        //НДС за сборы за оформление ПД за вычетом аннулирования, руб
        monthClosureStatistics.feesTax = pdStatistics.countAndProfit.profit.feeVat.subtract(pdStatistics.countAndProfit.profit.feeVatRepeal);

        //Сумма сборов за вычетом аннулирования, руб
        monthClosureStatistics.feesAmount = pdStatistics.countAndProfit.profit.fee.subtract(pdStatistics.countAndProfit.profit.feeRepeal);

        //Выручка за месяц всего
        monthClosureStatistics.totalIncome = pdStatistics.countAndProfit.profit.total;

        //Выручка за месяц всего, за вычетом аннулирования, руб
        monthClosureStatistics.totalIncomeExceptAnnulled = pdStatistics.countAndProfit.profit.total.subtract(pdStatistics.countAndProfit.profit.totalRepeal);

        //Сумма выручки за месяц наличными за вычетом аннулированных, руб
        monthClosureStatistics.totalCashExceptAnnulled = pdStatistics.countAndProfit.profit.totalCashPaymentSum.subtract(pdStatistics.countAndProfit.profit.totalCashPaymentSumRepeal);

        monthClosureStatistics.monthOpenShift = firstWorkingShift;
        monthClosureStatistics.monthCloseShift = lastWorkingShift;

        return monthClosureStatistics;
    }
}
