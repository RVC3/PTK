package ru.ppr.cppk.logic.fiscalDocStateSync.updater;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;

/**
 * @author Dmitry Nevolin
 */
public class FineSaleEventStatusUpdater extends FiscalDocStatusUpdater<FineSaleEvent> {

    private final LocalDaoSession localDaoSession;
    private final FineSaleEvent fineSaleEvent;

    public FineSaleEventStatusUpdater(LocalDaoSession localDaoSession,
                                      DocumentNumberProvider documentNumberProvider,
                                      int spndNumber,
                                      Date printDateTime,
                                      FineSaleEvent fineSaleEvent) {
        super(localDaoSession, documentNumberProvider, spndNumber, printDateTime);

        this.localDaoSession = localDaoSession;
        this.fineSaleEvent = fineSaleEvent;
    }

    @Override
    protected void updateToBrokenImpl() {
        fineSaleEvent.setStatus(FineSaleEvent.Status.BROKEN);

        localDaoSession.getFineSaleEventDao().update(fineSaleEvent);
    }

    @Override
    protected void updateToCheckPrintedImpl(Check check) {
        // CHECK_PRINTED
        fineSaleEvent.setCheckId(check.getId());// Чек
        fineSaleEvent.setTicketTapeEventId(localDaoSession.getTicketTapeEventDao().getInstalledTicketTape().getId());// Билетная лента
        fineSaleEvent.setStatus(FineSaleEvent.Status.CHECK_PRINTED);// Статус
        // Банковская транзакция
        if (fineSaleEvent.getPaymentMethod() == PaymentType.INDIVIDUAL_BANK_CARD) {
            BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(fineSaleEvent.getBankTransactionEventId());

            if (bankTransactionEvent != null) {
                bankTransactionEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);

                localDaoSession.getBankTransactionDao().update(bankTransactionEvent);
            }
        }

        localDaoSession.getFineSaleEventDao().update(fineSaleEvent);

    }

}
