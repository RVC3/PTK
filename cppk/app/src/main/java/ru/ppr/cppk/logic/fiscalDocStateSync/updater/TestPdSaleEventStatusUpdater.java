package ru.ppr.cppk.logic.fiscalDocStateSync.updater;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;

/**
 * Класс помошник, для обновление статуса события печати тестового ПД в процессе синхронизации
 *
 * @author Grigoriy Kashka
 */
public class TestPdSaleEventStatusUpdater extends FiscalDocStatusUpdater<TestTicketEvent> {

    private final LocalDaoSession localDaoSession;
    private final TestTicketEvent testTicketEvent;

    public TestPdSaleEventStatusUpdater(LocalDaoSession localDaoSession,
                                        DocumentNumberProvider documentNumberProvider,
                                        int spndNumber,
                                        Date printDateTime,
                                        TestTicketEvent testTicketEvent) {
        super(localDaoSession, documentNumberProvider, spndNumber, printDateTime);

        this.localDaoSession = localDaoSession;
        this.testTicketEvent = testTicketEvent;
    }

    @Override
    protected void updateToBrokenImpl() {
        testTicketEvent.setStatus(TestTicketEvent.Status.BROKEN);

        localDaoSession.getTestTicketDao().update(testTicketEvent);
    }

    @Override
    protected void updateToCheckPrintedImpl(Check check) {
        // CHECK_PRINTED
        testTicketEvent.setCheckId(check.getId());// Чек
        testTicketEvent.setTicketTapeEventId(localDaoSession.getTicketTapeEventDao().getInstalledTicketTape().getId());// Билетная лента
        testTicketEvent.setStatus(TestTicketEvent.Status.CHECK_PRINTED);// Статус
        localDaoSession.getTestTicketDao().update(testTicketEvent);

    }

}
