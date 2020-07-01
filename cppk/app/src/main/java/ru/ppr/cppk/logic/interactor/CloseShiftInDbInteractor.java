package ru.ppr.cppk.logic.interactor;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.NsiVersionManager;

/**
 * Операция закрытия смены в локальной БД ПТК.
 *
 * @author Aleksandr Brazhkin
 */
public class CloseShiftInDbInteractor {

    private final ShiftManager shiftManager;
    private final PaperUsageCounter paperUsageCounter;
    private final NsiVersionManager nsiVersionManager;
    private final LocalDaoSession localDaoSession;

    public CloseShiftInDbInteractor(ShiftManager shiftManager, PaperUsageCounter paperUsageCounter, NsiVersionManager nsiVersionManager, LocalDaoSession localDaoSession) {
        this.shiftManager = shiftManager;
        this.paperUsageCounter = paperUsageCounter;
        this.nsiVersionManager = nsiVersionManager;
        this.localDaoSession = localDaoSession;
    }

    public void closeShift() {

        if (shiftManager.isShiftClosed()) {
            throw new IllegalStateException("Shift is already closed");
        }

        Date currentDate = new Date();

        if (currentDate.before(shiftManager.getStartShiftEvent().getStartTime())) {
            throw new IllegalStateException("Shift couldn't be finished before opening");
        }

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(nsiVersionManager.getCurrentNsiVersionId())
                .setShiftId(shiftManager.getCurrentShiftId())
                .build();

        BigDecimal cashInFR = pdStatistics.countAndProfit.profit.total.subtract(pdStatistics.countAndProfit.profit.totalRepeal);
        PaperUsage paperUsage = paperUsageCounter.getPaperUsage(PaperUsage.ID_SHIFT);
        int spndNumber = localDaoSession.getCheckDao().getLastCheck().getSnpdNumber() + 1;

        localDaoSession.beginTransaction();
        try {
            ShiftEvent closeShiftEvent = shiftManager.createCloseShiftEvent(paperUsage.getPaperLength(), paperUsage.isRestarted(), cashInFR);
            shiftManager.closeShiftEventUpdateToComplete(currentDate, closeShiftEvent.getId(), spndNumber);
            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }
}
