package ru.ppr.cppk.sync.loader.model;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.sync.kpp.model.EventsStatistic;

/**
 * @author Grigoriy Kashka
 */
public class EventsStatisticLoader {

    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;
    private final LocalDaoSession localDaoSession;

    public EventsStatisticLoader(LocalDaoSession localDaoSession,
                                 ServiceTicketControlEventRepository serviceTicketControlEventRepository) {
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
        this.localDaoSession = localDaoSession;
    }

    public EventsStatistic load(@NonNull String shiftId, boolean closeShiftEvent) {

        EventsStatistic eventsStatistic = new EventsStatistic();

        // реальными данными заполняем только для события закрытия смены
        if (closeShiftEvent) {

            // количество аннулированных ПД
            eventsStatistic.ticketAnnulledEventsCount = localDaoSession.getCppkTicketReturnDao().getRecallCountForShift(shiftId);

            // количество проданных ПД
            eventsStatistic.ticketSaleEventsCount = localDaoSession.getCppkTicketSaleDao().getSaleCountForShift(shiftId);

            // количество проверенных ПД
            eventsStatistic.ticketControlEventsCount = localDaoSession.getCppkTicketControlsDao().getControlsCountForShift(shiftId);

            // количество тестовых ПД
            eventsStatistic.testTicketsEventsCount = localDaoSession.getTestTicketDao().getCountTestPdForShift(shiftId, EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED));

            // количество событий по сменам
            eventsStatistic.shiftEventsCount = localDaoSession.getShiftEventDao().getShiftEventsCountByPeriod(shiftId);

            // количество банковских транзакций
            eventsStatistic.bankTransactionCashRegisterEventsCount = localDaoSession.getBankTransactionDao().getTransactionCount(shiftId);

            // количество событий замены ленты
            eventsStatistic.ticketPaperRollEventsCountsCount = localDaoSession.getTicketTapeEventDao().getTicketTapeCountForShift(shiftId);

            // Количество событий оформления (оплаты) штрафов
            eventsStatistic.finePaidEventsCount = localDaoSession.getFineSaleEventDao().getFinePaidEventsCountForShift(shiftId, EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED));

            // Количество событий контроля сервисных карт
            eventsStatistic.serviceTicketControlEventsCount = serviceTicketControlEventRepository.getServiceTicketControlEventsCountForShift(shiftId, EnumSet.of(ServiceTicketControlEvent.Status.COMPLETED));
        } else {
            // количество аннулированных ПД
            eventsStatistic.ticketAnnulledEventsCount = 0;

            // количество проданных ПД
            eventsStatistic.ticketSaleEventsCount = 0;

            // количество проверенных ПД
            eventsStatistic.ticketControlEventsCount = 0;

            // количество тестовых ПД
            eventsStatistic.testTicketsEventsCount = 0;

            // количество событий по сменам
            eventsStatistic.shiftEventsCount = 1;

            // количество банковских транзакций
            eventsStatistic.bankTransactionCashRegisterEventsCount = 0;

            // количество событий замены ленты
            eventsStatistic.ticketPaperRollEventsCountsCount = 0;

            // Количество событий оформления (оплаты) штрафов
            eventsStatistic.finePaidEventsCount = 0;

            // Количество событий прохода по служебной карте
            eventsStatistic.serviceTicketControlEventsCount = 0;
        }

        return eventsStatistic;
    }
}
