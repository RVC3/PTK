package ru.ppr.cppk.sync.loader.CashRegisterEventData;

import android.support.annotation.NonNull;

import ru.ppr.cppk.data.summary.FineSaleStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.ShiftClosureStatistics;
import ru.ppr.cppk.sync.kpp.model.EventsStatistic;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Grigoriy Kashka
 */
public class ShiftClosureStatisticsBuilder extends BaseLoader {

    private final NsiVersionManager nsiVersionManager;
    private final FineRepository fineRepository;
    private final FeeTaxOperationsSummaryBuilder feeTaxOperationsSummaryBuilder;
    private final TaxOperationsSummaryBuilder taxOperationsSummaryBuilder;
    private final FeeExemptionOperationsSummaryBuilder feeExemptionOperationsSummaryBuilder;


    public ShiftClosureStatisticsBuilder(LocalDaoSession localDaoSession,
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

    public ShiftClosureStatistics build(@NonNull String shiftId, EventsStatistic eventsStatistic) {
        ShiftClosureStatistics statistics = new ShiftClosureStatistics();

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(nsiVersionManager.getCurrentNsiVersionId())
                .setShiftId(shiftId)
                .setBuildWithMonthStatistics(false)
                .build();

        TicketTapeStatisticsBuilder.Statistics ticketTapeStatistics = new TicketTapeStatisticsBuilder(localDaoSession)
                .setShiftId(shiftId)
                .build();

        FineSaleStatisticsBuilder.Statistics fineSaleStatistics = new FineSaleStatisticsBuilder(localDaoSession, fineRepository)
                .setShiftId(shiftId)
                .setBuildWithMonthStatistics(false)
                .build();

        // Это количество бобин, которое в принипе побывало в принтере за смену
        statistics.finishedTapesCount = ticketTapeStatistics.ticketTapeEvents.size();

        statistics.currentTapeLengthInMillimeters = ticketTapeStatistics.paperConsumption == null ? 0 : ticketTapeStatistics.paperConsumption.intValue();

        statistics.totalSmartCardExceptAnnulled = pdStatistics.countAndProfit.profit.totalCardPaymentSum.subtract(pdStatistics.countAndProfit.profit.totalCardPaymentSumRepeal);

        statistics.luggageTicketExceptAnnulled = feeTaxOperationsSummaryBuilder.build(pdStatistics.ticketTypeBaggageStatistics.countAndProfit);

        //Статистика по штрафам
        statistics.finesPaid = taxOperationsSummaryBuilder.build(fineSaleStatistics);

        PdStatisticsBuilder.CountAndProfit cProfit = pdStatistics.ticketTypeFullStatistics.directionThereStatistics.presenceOfExemptionStatistics.countAndProfit;

        statistics.oneWayTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        cProfit = pdStatistics.ticketTypeFullStatistics.directionThereBackStatistics.presenceOfExemptionStatistics.countAndProfit;

        statistics.twoWayTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        cProfit = pdStatistics.ticketTypeChildStatistics.directionThereStatistics.presenceOfExemptionStatistics.countAndProfit;

        statistics.oneWayChildTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        cProfit = pdStatistics.ticketTypeChildStatistics.directionThereBackStatistics.presenceOfExemptionStatistics.countAndProfit;

        statistics.twoWayChildTicketSaleExceptAnnulled = feeExemptionOperationsSummaryBuilder.build(cProfit);

        // Трансферы за вычетом аннулирования
        statistics.transfers = feeExemptionOperationsSummaryBuilder.build(pdStatistics.ticketTypeTransferStatistics.countAndProfit);

        //НДС за сборы за оформление ПД за вычетом аннулирования, руб
        statistics.feesTax = pdStatistics.countAndProfit.profit.feeVat.subtract(pdStatistics.countAndProfit.profit.feeVatRepeal);

        //Сумма сборов за вычетом аннулирования, руб
        statistics.feesAmount = pdStatistics.countAndProfit.profit.fee.subtract(pdStatistics.countAndProfit.profit.feeRepeal);

        //Расход билетной ленты, мм
        statistics.tapeLength = (int) ticketTapeStatistics.finishedTicketTapeConsumptionInMillimeters;

        // Выручка за период всего
        statistics.totalIncome = pdStatistics.countAndProfit.profit.total;

        // Выручка за период всего, за вычетом аннулирования, руб
        statistics.totalIncomeExceptAnnulled = statistics.totalIncome.subtract(pdStatistics.countAndProfit.profit.totalRepeal);

        // Сумма выручки за период наличными за вычетом аннулированных, руб
        statistics.totalCashExceptAnnulled = pdStatistics.countAndProfit.profit.totalCashPaymentSum.subtract(pdStatistics.countAndProfit.profit.totalCashPaymentSumRepeal);

        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                .setShiftId(shiftId)
                .build();

        if (shiftInfoStatistics.firstDocument != null) {
            statistics.firstDocumentNumber = shiftInfoStatistics.firstDocument.number;
        }
        if (shiftInfoStatistics.lastDocument != null) {
            statistics.lastDocumentNumber = shiftInfoStatistics.lastDocument.number;
        }

        statistics.totalEventsCount = eventsStatistic.ticketAnnulledEventsCount +
                eventsStatistic.ticketSaleEventsCount +
                eventsStatistic.testTicketsEventsCount +
                eventsStatistic.ticketControlEventsCount +
                eventsStatistic.shiftEventsCount +
                eventsStatistic.bankTransactionCashRegisterEventsCount +
                eventsStatistic.ticketPaperRollEventsCountsCount +
                eventsStatistic.finePaidEventsCount +
                eventsStatistic.serviceTicketControlEventsCount;

        return statistics;
    }
}
