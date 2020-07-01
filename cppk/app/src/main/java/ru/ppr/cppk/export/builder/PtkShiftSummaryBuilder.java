package ru.ppr.cppk.export.builder;

import android.support.annotation.NonNull;

import ru.ppr.cppk.data.summary.FineSaleStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.export.mapper.CashRegisterMapper;
import ru.ppr.cppk.export.mapper.CashierMapper;
import ru.ppr.cppk.export.model.PtkShiftSummary;
import ru.ppr.cppk.export.model.SalesSum;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Grigoriy Kashka
 */
public class PtkShiftSummaryBuilder {

    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final FineRepository fineRepository;
    private final PrivateSettingsHolder privateSettingsHolder;

    public PtkShiftSummaryBuilder(@NonNull LocalDaoSession localDaoSession,
                                  @NonNull NsiVersionManager nsiVersionManager,
                                  @NonNull FineRepository fineRepository,
                                  @NonNull PrivateSettingsHolder privateSettingsHolder) {
        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.fineRepository = fineRepository;
        this.privateSettingsHolder = privateSettingsHolder;
    }

    public PtkShiftSummary build(ShiftEvent lastEvent) {

        PtkShiftSummary ptkShiftSummary = new PtkShiftSummary();

        ptkShiftSummary.deviceId = privateSettingsHolder.get().getTerminalNumber();
        ptkShiftSummary.shiftNumber = lastEvent.getShiftNumber();
        ptkShiftSummary.openDate = lastEvent.getStartTime();
        CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(lastEvent.getCashRegisterEventId());
        ptkShiftSummary.cashier = new CashierMapper().toExportCashier(localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId()));
        ptkShiftSummary.cashRegister = new CashRegisterMapper().toExportCashRegister(localDaoSession.cashRegisterDao().load(cashRegisterEvent.getCashRegisterId()));
        ptkShiftSummary.isOpened = lastEvent.getStatus() != ShiftEvent.Status.ENDED;

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(nsiVersionManager.getCurrentNsiVersionId()).setShiftId(lastEvent.getShiftId()).build();

        PdStatisticsBuilder.Profit childProfit = pdStatistics.ticketTypeChildStatistics.countAndProfit.profit;
        PdStatisticsBuilder.Profit fullProfit = pdStatistics.ticketTypeFullStatistics.countAndProfit.profit;
        PdStatisticsBuilder.Profit baggageProfit = pdStatistics.ticketTypeBaggageStatistics.countAndProfit.profit;

        FineSaleStatisticsBuilder.Statistics fineStatistics = new FineSaleStatisticsBuilder(localDaoSession, fineRepository).setShiftId(lastEvent.getShiftId()).build();

        ptkShiftSummary.cashSum = new SalesSum();
        ptkShiftSummary.cashlessSum = new SalesSum();

        // Наличные
        ptkShiftSummary.cashSum.ticketsSum = childProfit.tariffCashPaymentSum
                .subtract(childProfit.tariffCashPaymentSumRepeal)
                .add(fullProfit.tariffCashPaymentSum)
                .subtract(fullProfit.tariffCashPaymentSumRepeal);

        ptkShiftSummary.cashSum.feeSum = childProfit.feeCashPaymentSum
                .subtract(childProfit.feeCashPaymentSumRepeal)
                .add(fullProfit.feeCashPaymentSum)
                .subtract(fullProfit.feeCashPaymentSumRepeal)
                .add(baggageProfit.feeCashPaymentSum)
                .subtract(baggageProfit.feeCashPaymentSumRepeal);

        ptkShiftSummary.cashSum.luggageSum = baggageProfit.tariffCashPaymentSum
                .subtract(baggageProfit.tariffCashPaymentSumRepeal);

        ptkShiftSummary.cashSum.finesSum = fineStatistics.countAndProfit.profit.totalCashPaymentSum;

        // По банковской карте
        ptkShiftSummary.cashlessSum.ticketsSum = childProfit.tariffCardPaymentSum
                .subtract(childProfit.tariffCardPaymentSumRepeal)
                .add(fullProfit.tariffCardPaymentSum)
                .subtract(fullProfit.tariffCardPaymentSumRepeal);

        ptkShiftSummary.cashlessSum.feeSum = childProfit.feeCardPaymentSum
                .subtract(childProfit.feeCardPaymentSumRepeal)
                .add(fullProfit.feeCardPaymentSum)
                .subtract(fullProfit.feeCardPaymentSumRepeal)
                .add(baggageProfit.feeCardPaymentSum)
                .subtract(baggageProfit.feeCardPaymentSumRepeal);

        ptkShiftSummary.cashlessSum.luggageSum = baggageProfit.tariffCardPaymentSum
                .subtract(baggageProfit.tariffCardPaymentSumRepeal);

        ptkShiftSummary.cashlessSum.finesSum = fineStatistics.countAndProfit.profit.totalCardPaymentSum;

        return ptkShiftSummary;
    }


}
