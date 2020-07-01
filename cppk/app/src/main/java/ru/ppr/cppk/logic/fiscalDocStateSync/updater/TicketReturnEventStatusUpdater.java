package ru.ppr.cppk.logic.fiscalDocStateSync.updater;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;

/**
 * @author Dmitry Nevolin
 */
public class TicketReturnEventStatusUpdater extends FiscalDocStatusUpdater<CPPKTicketReturn> {

    private final LocalDaoSession localDaoSession;
    private final CPPKTicketReturn ticketReturnEvent;

    public TicketReturnEventStatusUpdater(LocalDaoSession localDaoSession,
                                          DocumentNumberProvider documentNumberProvider,
                                          int spndNumber,
                                          Date printDateTime,
                                          CPPKTicketReturn ticketReturnEvent) {
        super(localDaoSession, documentNumberProvider, spndNumber, printDateTime);

        this.localDaoSession = localDaoSession;
        this.ticketReturnEvent = ticketReturnEvent;
    }

    @Override
    protected void updateToBrokenImpl() {
        ticketReturnEvent.setProgressStatus(ProgressStatus.Broken);
        localDaoSession.getCppkTicketReturnDao().update(ticketReturnEvent);
    }

    @Override
    protected void updateToCheckPrintedImpl(Check check) {
        // CheckPrinted
        ticketReturnEvent.setCheckId(check.getId());
        ticketReturnEvent.setRecallDateTime(check.getPrintDatetime());
        ticketReturnEvent.setProgressStatus(ProgressStatus.CheckPrinted);

        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketReturnEvent.getBankTransactionCashRegisterEventId());

        if (bankTransactionEvent != null) {
            bankTransactionEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);

            localDaoSession.getBankTransactionDao().update(bankTransactionEvent);
        }

        localDaoSession.getCppkTicketReturnDao().update(ticketReturnEvent);
    }

}
