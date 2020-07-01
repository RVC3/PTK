package ru.ppr.cppk.logic.fiscalDocStateSync.builder;

import java.util.EnumSet;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocInfoBuilder;

/**
 * @author Dmitry Nevolin
 */
public class TicketReturnEventInfoBuilder extends FiscalDocInfoBuilder<CPPKTicketReturn> {

    public TicketReturnEventInfoBuilder(Holder<LocalDaoSession> localDaoSessionHolder, String shiftId) {
        super(() -> localDaoSessionHolder.get().getCppkTicketReturnDao().getReturnEventsForShift(shiftId, EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed), false),
                target -> new FiscalDocInfoBuilder.Item(localDaoSessionHolder.get().getPriceDao().load(target.getPriceId()).getPayed()));
    }

}
