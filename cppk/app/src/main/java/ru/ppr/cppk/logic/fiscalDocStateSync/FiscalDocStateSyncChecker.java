package ru.ppr.cppk.logic.fiscalDocStateSync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;

/**
 * Класс для проверки наличия фискальных документов в несинхронизированном состоянии.
 *
 * @author Dmitry Nevolin
 */
public class FiscalDocStateSyncChecker {

    private final LocalDaoSession localDaoSession;
    private final ShiftManager shiftManager;

    @Inject
    public FiscalDocStateSyncChecker(LocalDaoSession localDaoSession, ShiftManager shiftManager) {
        this.localDaoSession = localDaoSession;
        this.shiftManager = shiftManager;
    }

    /**
     * Выполняет проверку наличия фискальных документов в несинхронизированном состоянии (PRE_PRINTING).
     * Если смена закрыта, считается, что таких документов нет.
     *
     * @return Результат, содержащий несинхронизированные документы, если таковые имеются.
     */
    @NonNull
    public Result check() {

        String shiftId = null;
        if (shiftManager.isShiftOpened()) {
            shiftId = shiftManager.getCurrentShiftId();
        }
        if (shiftId == null) {
            // Если смена закрыта, считаем, несинхронизированных документов нет.
            return new Result(null, null, null, null, null);
        }

        // Ищем несинхронизированное событие закрытия смены
        ShiftEvent lastEndShiftEventForCheck =
                localDaoSession.getShiftEventDao().getLastShiftEvent(EnumSet.of(ShiftEvent.Status.ENDED), EnumSet.of(ShiftEvent.ShiftProgressStatus.PRE_PRINTING));
        // Ищем несинхронизированное событие продажи ПД
        CPPKTicketSales lastTicketSaleEventForCheck =
                localDaoSession.getCppkTicketSaleDao().getLastSaleForShift(shiftId, EnumSet.of(ProgressStatus.PrePrinting));
        // Ищем несинхронизированное событие аннулирования ПД
        CPPKTicketReturn lastTicketReturnEventForCheck =
                localDaoSession.getCppkTicketReturnDao().getLastReturnForShift(shiftId, EnumSet.of(ProgressStatus.PrePrinting));
        // Ищем несинхронизированное событие оформления штрафа
        FineSaleEvent lastFineSaleEventForCheck =
                localDaoSession.getFineSaleEventDao().getLastFineSaleEventForShift(shiftId, EnumSet.of(FineSaleEvent.Status.PRE_PRINTING));
        // Ищем несинхронизированное событие продажи тестового ПД
        TestTicketEvent lastTestTicketEventForCheck =
                localDaoSession.getTestTicketDao().getLastTestTicketForShift(shiftId, EnumSet.of(TestTicketEvent.Status.PRE_PRINTING));

        return new Result(lastTicketSaleEventForCheck, lastTicketReturnEventForCheck, lastFineSaleEventForCheck, lastTestTicketEventForCheck, lastEndShiftEventForCheck);
    }

    /**
     * Результат выполнения проверки наличия несинхронизированных документов.
     */
    public class Result {

        private final boolean empty;
        private final CPPKTicketSales ticketSaleEvent;
        private final CPPKTicketReturn ticketReturnEvent;
        private final FineSaleEvent fineSaleEvent;
        private final ShiftEvent closeShiftEvent;
        private final TestTicketEvent testTicketEvent;

        private Result(CPPKTicketSales ticketSaleEvent, CPPKTicketReturn ticketReturnEvent, FineSaleEvent fineSaleEvent, TestTicketEvent testTicketEvent, ShiftEvent closeShiftEvent) {
            empty = ticketSaleEvent == null && ticketReturnEvent == null && fineSaleEvent == null && testTicketEvent == null && closeShiftEvent == null;
            this.ticketSaleEvent = ticketSaleEvent;
            this.ticketReturnEvent = ticketReturnEvent;
            this.fineSaleEvent = fineSaleEvent;
            this.testTicketEvent = testTicketEvent;
            this.closeShiftEvent = closeShiftEvent;
        }

        /**
         * Возвращает факт наличия несинхронизированных событий.
         *
         * @return {@code true} если таквые имеются, {@code false} - иначе
         */
        public boolean isEmpty() {
            return empty;
        }

        /**
         * Возвращает несинхронизированное событие продажи ПД, {@code null} - если такого события нет.
         */
        @Nullable
        public CPPKTicketSales getTicketSaleEvent() {
            return ticketSaleEvent;
        }

        /**
         * Возвращает несинхронизированное событие аннулирования ПД, {@code null} - если такого события нет.
         */
        @Nullable
        public CPPKTicketReturn getTicketReturnEvent() {
            return ticketReturnEvent;
        }

        /**
         * Возвращает несинхронизированное событие оформления штрафа, {@code null} - если такого события нет.
         */
        @Nullable
        public FineSaleEvent getFineSaleEvent() {
            return fineSaleEvent;
        }

        /**
         * Возвращает несинхронизированное событие продажи тестового ПД, {@code null} - если такого события нет.
         */
        @Nullable
        public TestTicketEvent getTestTicketEvent() {
            return testTicketEvent;
        }

        /**
         * Возвращает несинхронизированное событие закрытия смены, {@code null} - если такого события нет.
         */
        @Nullable
        public ShiftEvent getCloseShiftEvent() {
            return closeShiftEvent;
        }

    }
}
