package ru.ppr.cppk.logic.fiscalDocStateSync.updater;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;

/**
 * @author Grigoriy Kashka
 */
public class CloseShiftEventStatusUpdater extends FiscalDocStatusUpdater<FineSaleEvent> {

    private final LocalDaoSession localDaoSession;
    private final ShiftEvent shiftEvent;
    private final ShiftManager shiftManager;

    public CloseShiftEventStatusUpdater(LocalDaoSession localDaoSession,
                                        DocumentNumberProvider documentNumberProvider,
                                        ShiftManager shiftManager,
                                        int spndNumber,
                                        Date printDateTime,
                                        ShiftEvent shiftEvent) {
        super(localDaoSession, documentNumberProvider, spndNumber, printDateTime);

        this.localDaoSession = localDaoSession;
        this.shiftEvent = shiftEvent;
        this.shiftManager = shiftManager;
    }

    @Override
    protected void updateToBrokenImpl() {
        shiftEvent.setProgressStatus(ShiftEvent.ShiftProgressStatus.BROKEN);
        localDaoSession.getShiftEventDao().update(shiftEvent);
    }

    @Override
    protected void updateToCheckPrintedImpl(@NonNull Check check) {
        // CHECK_PRINTED
        shiftEvent.setCheckId(check.getId());// Чек
        shiftEvent.setCloseTime(check.getPrintDatetime());
        shiftEvent.setProgressStatus(ShiftEvent.ShiftProgressStatus.CHECK_PRINTED);// Статус
        localDaoSession.getShiftEventDao().update(shiftEvent);
        shiftManager.refreshState();
    }

}
