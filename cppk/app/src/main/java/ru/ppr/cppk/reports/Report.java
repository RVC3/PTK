package ru.ppr.cppk.reports;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.CppkTicketReturnDao;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.db.local.MonthEventDao;
import ru.ppr.cppk.db.local.PrintReportEventDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.TestTicketDao;
import ru.ppr.cppk.db.local.TicketTapeEventDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.PrintReportEvent;
import ru.ppr.cppk.printer.paramBuilders.ReportClicheParamsBuilder;
import ru.ppr.cppk.printer.rx.operation.PrinterGetCashInFR;
import ru.ppr.cppk.printer.rx.operation.PrinterGetOdometerValue;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import rx.Completable;
import rx.Observable;
import rx.Subscriber;

public abstract class Report<REPORT extends Report, T> {

    private static final String TAG = Logger.makeLogTag(Report.class);

    protected Globals g;


    protected final LocalDaoSession localDaoSession;
    protected final NsiDaoSession nsiDaoSession;

    private final ShiftEventDao shiftDao;
    private final CppkTicketControlsDao controlsDao;
    private final CppkTicketSaleDao saleDao;
    private final MonthEventDao monthEventDao;
    private final CheckDao checkDao;
    private final TestTicketDao testTicketDao;
    private final CppkTicketReturnDao cppkTicketReturnDao;
    private final PrintReportEventDao printReportEventDao;
    private final TicketTapeEventDao ticketTapeEventDao;
    private final NsiVersionManager nsiVersionManager;


    /**
     * Сумма в ФР, запрашивается каждый раз перед печатью отчета
     */
    private BigDecimal cashInFR = BigDecimal.ZERO;
    /**
     * Показания одометра, запрашиваются каждый раз перед печатью отчета
     */
    private long printerOdometerValue = 0;

    public Report() {
        this(Globals.getInstance(), Globals.getInstance().getLocalDaoSession(), Globals.getInstance().getNsiDaoSession(), Di.INSTANCE.nsiVersionManager());
    }

    /**
     * Конструктор для создания объекта Report.
     * Данный конструктор будет полезен в случае написания тестов.
     */
    public Report(Globals globals,
                  LocalDaoSession localDaoSession,
                  NsiDaoSession nsiDaoSession,
                  NsiVersionManager nsiVersionManager) {
        g = globals;
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
        shiftDao = localDaoSession.getShiftEventDao();
        controlsDao = localDaoSession.getCppkTicketControlsDao();
        saleDao = localDaoSession.getCppkTicketSaleDao();
        monthEventDao = localDaoSession.getMonthEventDao();
        checkDao = localDaoSession.getCheckDao();
        testTicketDao = localDaoSession.getTestTicketDao();
        cppkTicketReturnDao = localDaoSession.getCppkTicketReturnDao();
        printReportEventDao = localDaoSession.getPrintReportEventDao();
        ticketTapeEventDao = localDaoSession.getTicketTapeEventDao();
        this.nsiVersionManager = nsiVersionManager;
    }

    @NonNull
    protected TestTicketDao getTestTicketDao() {
        return testTicketDao;
    }

    public TicketTapeEventDao getTicketTapeEventDao() {
        return ticketTapeEventDao;
    }

    @NonNull
    public CheckDao getCheckDao() {
        return checkDao;
    }

    @NonNull
    protected ShiftEventDao getShiftDao() {
        return shiftDao;
    }

    @NonNull
    protected CppkTicketControlsDao getControlsDao() {
        return controlsDao;
    }

    @NonNull
    protected CppkTicketSaleDao getSaleDao() {
        return saleDao;
    }

    @NonNull
    protected MonthEventDao getMonthEventDao() {
        return monthEventDao;
    }

    @NonNull
    protected CppkTicketReturnDao getCPPKTicketReturn() {
        return cppkTicketReturnDao;
    }

    @NonNull
    protected NsiVersionManager getNsiVersionManager() {
        return nsiVersionManager;
    }

    public Observable<REPORT> buildObservable(IPrinter printer) {
        return getCashInFr(printer)
                .flatMap(result -> getPrinterOdometerValue(printer)
                        .doOnNext(result1 -> Globals.getInstance().getPaperUsageCounter().setCurrentOdometerValueBeforePrinting(result1.getOdometerValue())))
                .flatMap(result -> Observable
                        .create((Subscriber<? super REPORT> subscriber) -> {
                            try {
                                REPORT that = build();
                                subscriber.onNext(that);
                                subscriber.onCompleted();
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        }));

    }

    protected abstract REPORT build() throws Exception;

    public Observable<T> printObservable(IPrinter printer) {
        return printImpl(printer)
                .flatMap(printResult ->
                        addPrintReportEventObservable(printResult)
                                .flatMap(aVoid -> Observable.just(printResult)))
                .subscribeOn(SchedulersCPPK.printer());
    }

    protected abstract Observable<T> printImpl(IPrinter printer);

    protected abstract Observable<Void> addPrintReportEventObservable(T printResult);

    public Observable<T> buildAndPrintObservable(IPrinter printer) {
        return checkTicketTapeIsSet()
                .andThen(Dagger.appComponent().fiscalDocStateSynchronizer().rxSyncCheckState())
                .flatMapObservable((isTicketTapeSet -> buildObservable(printer).flatMap(report -> report.printObservable(printer))));
    }

    /**
     * Получение суммы в ФР
     *
     * @param printer Принтер
     * @return Observable с результатом выполнения операции
     */
    private Observable<PrinterGetCashInFR.Result> getCashInFr(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory().getGetCashInFrOperation()
                .call()
                .doOnNext(result -> cashInFR = result.getCashInFR());
    }

    /**
     * Получение показаний одометра
     *
     * @param printer Принтер
     * @return Observable с результатом выполнения операции
     */
    private Observable<PrinterGetOdometerValue.Result> getPrinterOdometerValue(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue()
                .call()
                .doOnNext(result -> printerOdometerValue = result.getOdometerValue());
    }

    protected Observable<Void> addPrintReportEventObservable(ReportType reportType, Date date) {
        return Observable.create((Subscriber<? super Void> subscriber) -> {

            LocalDaoSession localDaoSession = Dagger.appComponent().localDaoSession();

            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            //если смена уже закрыта значит событие вне смены
            if (shiftEvent != null && shiftEvent.getStatus() == ShiftEvent.Status.ENDED) {
                shiftEvent = null;
            }
            MonthEvent monthEvent = localDaoSession.getMonthEventDao().getLastMonthEvent();
            TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();

            boolean saved = false;

            localDaoSession.beginTransaction();
            try {
                // добавляем информацию о ПТК
                StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                if (stationDevice != null) {
                    localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
                }
                Event event = Di.INSTANCE.eventBuilder()
                        .setDeviceId(stationDevice.getId())
                        .build();

                Preconditions.checkNotNull(event, "Event is null");
                localDaoSession.getEventDao().insertOrThrow(event);

                Preconditions.checkNotNull(cashRegisterEvent, "CashRegisterEvent is null");

                Preconditions.checkNotNull(ticketTapeEvent, "TicketTapeEvent is null");
                if (ticketTapeEvent.getEndTime() != null) {
                    throw new IllegalArgumentException("ticketTapeEvent.getEndTime()!=null");
                }

                PrintReportEvent printReportEvent = new PrintReportEvent();
                printReportEvent.setEventId(event.getId());
                printReportEvent.setOperationTime(date);
                printReportEvent.setCashRegisterEventId(cashRegisterEvent.getId());
                printReportEvent.setReportType(reportType);
                printReportEvent.setTicketTapeEventId(ticketTapeEvent.getId());
                printReportEvent.setMonthEventId(monthEvent == null ? -1 : monthEvent.getId());
                printReportEvent.setShiftEventId(shiftEvent == null ? -1 : shiftEvent.getId());
                printReportEvent.setCashInFR(cashInFR);

                localDaoSession.getPrintReportEventDao().insertOrThrow(printReportEvent);

                AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                        .setType(AuditTrailEventType.PRINT_REPORT)
                        .setExtEventId(printReportEvent.getId())
                        .setOperationTime(event.getCreationTimestamp())
                        .setShiftEventId(printReportEvent.getShiftEventId())
                        .setMonthEventId(printReportEvent.getMonthEventId())
                        .build();

                localDaoSession.getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

                localDaoSession.setTransactionSuccessful();
                saved = true;
            } catch (Exception e) {
                Logger.error(TAG, e);
                throw e;
            } finally {
                localDaoSession.endTransaction();
                if (!saved) {
                    subscriber.onError(new Exception("daoSession.getPrintReportEventDao().insertOrThrow(printReportEvent) = FALSE"));
                } else {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            }
        });
    }

    protected Completable checkTicketTapeIsSet() {
        return Dagger.appComponent().ticketTapeChecker().checkOrThrow();
    }

    public ReportClicheTpl.Params buildClicheParams() throws Exception {
        return ReportClicheParamsBuilder.build();

    }

    /**
     * Возвращает сумму в ФР перед печатью отчета
     *
     * @return сумма в ФР
     */
    protected BigDecimal getCashInFR() {
        return cashInFR;
    }

    /**
     * Возвращает показания одометра перед печатью отчета
     *
     * @return показания одометра
     */
    protected long getPrinterOdometerValue() {
        return printerOdometerValue;
    }
}