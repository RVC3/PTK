package ru.ppr.cppk.logic.fiscalDocStateSync.builder;

import java.util.EnumSet;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocInfoBuilder;

/**
 * @author Dmitry Nevolin
 */
public class FineSaleEventInfoBuilder extends FiscalDocInfoBuilder<FineSaleEvent> {

    public FineSaleEventInfoBuilder(LocalDaoSession localDaoSession, String shiftId) {
        super(() -> localDaoSession.getFineSaleEventDao().getFineSaleEventsForShift(shiftId, EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED)),
                target -> new FiscalDocInfoBuilder.Item(target.getAmount()));
    }

}
