package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TicketTapeEventDao;
import ru.ppr.cppk.sync.kpp.TicketPaperRollEvent;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TicketPaperRollEventLoader extends BaseLoader {

    private final EventLoader eventLoader;
    private final CashRegisterEventLoader cashRegisterEventLoader;
    private final WorkingShiftEventLoader workingShiftEventLoader;
    private final TicketTapeStatisticsBuilder ticketTapeStatisticsBuilder;

    public TicketPaperRollEventLoader(LocalDaoSession localDaoSession,
                                      NsiDaoSession nsiDaoSession,
                                      EventLoader eventLoader,
                                      CashRegisterEventLoader cashRegisterEventLoader,
                                      WorkingShiftEventLoader workingShiftEventLoader,
                                      TicketTapeStatisticsBuilder ticketTapeStatisticsBuilder) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
        this.cashRegisterEventLoader = cashRegisterEventLoader;
        this.workingShiftEventLoader = workingShiftEventLoader;
        this.ticketTapeStatisticsBuilder = ticketTapeStatisticsBuilder;
    }

    public static class Columns {
        static final Column SERIES = new Column(0, TicketTapeEventDao.Properties.Series);
        static final Column NUMBER = new Column(1, TicketTapeEventDao.Properties.Number);
        static final Column START_DATE_TIME = new Column(2, TicketTapeEventDao.Properties.StartTime);
        static final Column END_DATE_TIME = new Column(3, TicketTapeEventDao.Properties.EndTime);
        static final Column PAPER_CONSUMPTION = new Column(4, TicketTapeEventDao.Properties.PaperConsumption);
        static final Column PAPER_COUNTER_HAS_RESTARTED = new Column(5, TicketTapeEventDao.Properties.IsPaperCounterRestarted);
        static final Column TICKET_TAPE_ID = new Column(6, TicketTapeEventDao.Properties.TicketTapeId);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(7, TicketTapeEventDao.Properties.CashRegisterWorkingShiftId);
        static final Column CASH_REGISTER_EVENT_ID = new Column(8, TicketTapeEventDao.Properties.CashRegisterEventId);

        public static Column[] all = new Column[]{
                SERIES,
                NUMBER,
                START_DATE_TIME,
                END_DATE_TIME,
                PAPER_CONSUMPTION,
                PAPER_COUNTER_HAS_RESTARTED,
                TICKET_TAPE_ID,
                CASH_REGISTER_WORKING_SHIFT_ID,
                CASH_REGISTER_EVENT_ID
        };
    }

    public TicketPaperRollEvent load(Cursor cursor, Offset offset) {

        TicketPaperRollEvent ticketPaperRollEvent = new TicketPaperRollEvent();

        ticketPaperRollEvent.series = cursor.getString(offset.value + Columns.SERIES.index);
        ticketPaperRollEvent.number = cursor.getInt(offset.value + Columns.NUMBER.index);
        int index = offset.value + Columns.END_DATE_TIME.index;
        boolean isStartEvent = cursor.isNull(index);
        ticketPaperRollEvent.operationDateTime = new Date(cursor.getLong(isStartEvent ? (offset.value + Columns.START_DATE_TIME.index) : index));
        ticketPaperRollEvent.paperConsumption = cursor.getLong(offset.value + Columns.PAPER_CONSUMPTION.index);
        ticketPaperRollEvent.paperCounterHasRestarted = cursor.getInt(offset.value + Columns.PAPER_COUNTER_HAS_RESTARTED.index) == 1;
        index = offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index;
        long cashRegisterWorkingShiftId = cursor.isNull(index) ? -1 : cursor.getLong(index);
        long cashRegisterEventId = cursor.getLong(offset.value + Columns.CASH_REGISTER_EVENT_ID.index);
        String ticketTapeId = cursor.getString(offset.value + Columns.TICKET_TAPE_ID.index);

        offset.value += Columns.all.length;

        eventLoader.fill(ticketPaperRollEvent, cursor, offset);

        //заполним мелкие модельки отдельными запросами
        cashRegisterEventLoader.fill(ticketPaperRollEvent, cashRegisterEventId);
        workingShiftEventLoader.fill(ticketPaperRollEvent, cashRegisterWorkingShiftId);

        //region подготовка данных статистики
        TicketTapeStatisticsBuilder.Statistics statistics = ticketTapeStatisticsBuilder.setTicketTapeId(ticketTapeId).build();
        TicketTapeStatisticsBuilder.TicketTapeInfo ticketTapeInfo = statistics.ticketTapeEvents.get(0);

        int testTicketsCount = statistics.testTicketsCount;
        int ticketsCount = statistics.salesCount;
        int serviceSaleReceiptsCount = statistics.servicesCount;
        int cancellationReceiptsCount = statistics.returnsCount;
        int totalTicketsCount = testTicketsCount + ticketsCount + serviceSaleReceiptsCount + cancellationReceiptsCount;

        int testShiftRegisterReportsCount = statistics.reportsCount.testShiftSheetCount;
        int shiftRegisterReportsCount = statistics.reportsCount.shiftSheetCount;
        int ettRegisterReportsCount = statistics.reportsCount.salesForEttLogCount;
        int exemptionShiftRegisterReportsCount = statistics.reportsCount.shiftDiscountSheetCount;
        int testMonthRegisterReportsCount = statistics.reportsCount.testMonthSheetCount;
        int monthRegisterReportsCount = statistics.reportsCount.monthSheetCount;
        int exemptionMonthRegisterReportsCount = statistics.reportsCount.monthDiscountSheetCount;
        int controlRegisterReportsCount = statistics.reportsCount.auditTrailCount;
        int totalReportsCount = testShiftRegisterReportsCount + shiftRegisterReportsCount + ettRegisterReportsCount +
                exemptionShiftRegisterReportsCount + monthRegisterReportsCount + exemptionMonthRegisterReportsCount + controlRegisterReportsCount;
        //endregion

        //0 - Установка ленты, 1 - Окончание ленты
        ticketPaperRollEvent.action = isStartEvent ? 0 : 1;

        // Устарело, игнорируем
        // http://agile.srvdev.ru/browse/CPPKPP-29029?focusedCommentId=132808&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#/comment-132808
        ticketPaperRollEvent.deviceLength = 0;

        // Первый документ
        ticketPaperRollEvent.ticketNumber = ticketTapeInfo.firstDocument != null ? ticketTapeInfo.firstDocument.number : ticketTapeInfo.ticketTapeEvent.getExpectedFirstDocNumber();

        // Последний документ
        if (!isStartEvent) {
            if (ticketTapeInfo.lastDocument != null) {
                ticketPaperRollEvent.lastTicketNumber = ticketTapeInfo.lastDocument.number;
            } else {
                ticketPaperRollEvent.lastTicketNumber = ticketTapeInfo.ticketTapeEvent.getExpectedFirstDocNumber();
            }
        } else {
            ticketPaperRollEvent.lastTicketNumber = null;
        }

        //Количество распечатанных чеков
        ticketPaperRollEvent.totalTicketsCount = isStartEvent ? 0 : (totalTicketsCount == 0 ? 1 : totalTicketsCount); //http://agile.srvdev.ru/browse/CPPKPP-26488
        ticketPaperRollEvent.testTicketsCount = isStartEvent ? 0 : testTicketsCount;
        ticketPaperRollEvent.ticketsCount = isStartEvent ? 0 : ticketsCount;
        ticketPaperRollEvent.serviceSaleReceiptsCount = isStartEvent ? 0 : serviceSaleReceiptsCount;
        ticketPaperRollEvent.cancellationReceiptsCount = isStartEvent ? 0 : cancellationReceiptsCount;

        //Количество отчетов
        ticketPaperRollEvent.totalReportsCount = isStartEvent ? 0 : totalReportsCount;
        ticketPaperRollEvent.testShiftRegisterReportsCount = isStartEvent ? 0 : testShiftRegisterReportsCount;
        ticketPaperRollEvent.shiftRegisterReportsCount = isStartEvent ? 0 : shiftRegisterReportsCount;
        ticketPaperRollEvent.ettRegisterReportsCount = isStartEvent ? 0 : ettRegisterReportsCount;
        ticketPaperRollEvent.exemptionShiftRegisterReportsCount = isStartEvent ? 0 : exemptionShiftRegisterReportsCount;
        ticketPaperRollEvent.testMonthRegisterReportsCount = isStartEvent ? 0 : testMonthRegisterReportsCount;
        ticketPaperRollEvent.monthRegisterReportsCount = isStartEvent ? 0 : monthRegisterReportsCount;
        ticketPaperRollEvent.exemptionMonthRegisterReportsCount = isStartEvent ? 0 : exemptionMonthRegisterReportsCount;
        ticketPaperRollEvent.controlRegisterReportsCount = isStartEvent ? 0 : controlRegisterReportsCount;

        return ticketPaperRollEvent;
    }

}
