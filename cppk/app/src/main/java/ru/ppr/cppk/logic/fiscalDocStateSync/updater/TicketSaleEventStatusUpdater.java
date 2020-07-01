package ru.ppr.cppk.logic.fiscalDocStateSync.updater;

import java.util.Calendar;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * @author Dmitry Nevolin
 */
public class TicketSaleEventStatusUpdater extends FiscalDocStatusUpdater<CPPKTicketSales> {

    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final CPPKTicketSales ticketSaleEvent;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final TicketTypeRepository ticketTypeRepository;

    public TicketSaleEventStatusUpdater(LocalDaoSession localDaoSession,
                                        DocumentNumberProvider documentNumberProvider,
                                        int spndNumber,
                                        Date printDateTime,
                                        NsiVersionManager nsiVersionManager,
                                        CPPKTicketSales ticketSaleEvent,
                                        PdValidityPeriodCalculator pdValidityPeriodCalculator,
                                        TicketTypeRepository ticketTypeRepository) {
        super(localDaoSession, documentNumberProvider, spndNumber, printDateTime);

        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.ticketSaleEvent = ticketSaleEvent;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Override
    protected void updateToBrokenImpl() {
        ticketSaleEvent.setProgressStatus(ProgressStatus.Broken);
        localDaoSession.beginTransaction();
        try {
            localDaoSession.getCppkTicketSaleDao().update(ticketSaleEvent);
            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    @Override
    protected void updateToCheckPrintedImpl(Check check) {
        // CheckPrinted
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(ticketSaleEvent.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        // время продажи
        Date saleDateTime = check.getPrintDatetime(); // с точностью до секунд
        ticketEventBase.setSaletime(saleDateTime); // с точностью до секунд
        // дата начала действия ПД
        Calendar calendar = DateUtils.getStartOfDay(saleDateTime);
        calendar.add(Calendar.DAY_OF_MONTH, ticketEventBase.getStartDayOffset());

        Date validFromDate = calendar.getTime();
        ticketEventBase.setValidFromDate(validFromDate); // с точностью до секунд
        // дата окончания действия ПД
        TicketType ticketType = ticketTypeRepository.load(ticketEventBase.getTypeCode(), nsiVersionManager.getCurrentNsiVersionId());
        int validityPeriodDay = pdValidityPeriodCalculator.calcValidityPeriod(validFromDate, ticketEventBase.getWayType(), ticketType, nsiVersionManager.getCurrentNsiVersionId());

        calendar.add(Calendar.DAY_OF_MONTH, validityPeriodDay);
        calendar.add(Calendar.SECOND, -1); // вычитаем 1 секунду, т.к. действует до 23:25:59
        ticketEventBase.setValidTillDate(calendar.getTime());

        localDaoSession.beginTransaction();
        try {

            localDaoSession.getTicketEventBaseDao().update(ticketEventBase);

            ticketSaleReturnEventBase.setCheckId(check.getId());

            localDaoSession.getTicketSaleReturnEventBaseDao().update(ticketSaleReturnEventBase);

            ticketSaleEvent.setProgressStatus(ProgressStatus.CheckPrinted);

            localDaoSession.getCppkTicketSaleDao().update(ticketSaleEvent);

            BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());

            if (bankTransactionEvent != null) {
                bankTransactionEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);

                localDaoSession.getBankTransactionDao().update(bankTransactionEvent);
            }

            localDaoSession.getCppkTicketSaleDao().update(ticketSaleEvent);
            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

}
