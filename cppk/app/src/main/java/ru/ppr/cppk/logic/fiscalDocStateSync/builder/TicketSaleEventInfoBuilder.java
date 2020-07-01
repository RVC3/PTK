package ru.ppr.cppk.logic.fiscalDocStateSync.builder;

import java.util.EnumSet;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocInfoBuilder;

/**
 * @author Dmitry Nevolin
 */
public class TicketSaleEventInfoBuilder extends FiscalDocInfoBuilder<CPPKTicketSales> {

    public TicketSaleEventInfoBuilder(LocalDaoSession localDaoSession, String shiftId) {
        super(() -> localDaoSession.getCppkTicketSaleDao().getSaleEventsForShift(shiftId, EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed), false),
                target -> new FiscalDocInfoBuilder.Item(localDaoSession.getPriceDao().load(localDaoSession.getTicketSaleReturnEventBaseDao().load(target.getTicketSaleReturnEventBaseId()).getFullPriceId()).getPayed()));
    }

}
