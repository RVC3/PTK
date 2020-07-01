package ru.ppr.cppk.logic;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;

/**
 * Провайдер следующего сквозного номера документа.
 *
 * @author Aleksandr Brazhkin
 */
public class DocumentNumberProvider {

    private final LocalDaoSession mLocalDaoSession;

    @Inject
    public DocumentNumberProvider(LocalDaoSession localDaoSession) {
        this.mLocalDaoSession = localDaoSession;
    }

    /**
     * Возвращает номер под которым надо распечатать новый билет.
     * Данный номер определяется по следующей формуле:
     * количество событий продажи в рамках текущего месяца
     * + количество событий аннулирвоания в рамках текущего месяца
     * + количество напечатанных тестовых билетов в рамках текущего месяца
     * + количество оформленных услуг
     * + количество штрафов
     * + 1
     *
     * @return Номер документа
     */
    public int getNextDocumentNumber() {

        MonthEvent monthEvent = mLocalDaoSession.getMonthEventDao().getLastMonthEvent();
        if (monthEvent == null || monthEvent.getStatus() == MonthEvent.Status.CLOSED) {
            throw new IllegalStateException("Month is null or closed");
        }

        return mLocalDaoSession.getCppkTicketSaleDao().getCountSalePdForMonth(monthEvent)
                + mLocalDaoSession.getCppkTicketReturnDao().getCountRepealPdInMonth(monthEvent)
                + mLocalDaoSession.getTestTicketDao().getCountTestPdForMonth(monthEvent)
                + mLocalDaoSession.getCppkServiceSaleDao().getCountServicePdForMonth(monthEvent)
                + mLocalDaoSession.getFineSaleEventDao().getEventsCountForMonth(monthEvent, EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED))
                + 1;
    }
}
