package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.printer.rx.operation.testTicket.PrintTestTicketOperation;
import ru.ppr.cppk.printer.rx.operation.testTicket.TestTicketTpl;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.StationRepository;
import rx.Observable;
import rx.Single;

/**
 * Документ "Пробный ПД"
 *
 * @author Aleksandr Brazhkin
 */
public class DocumentTestPd {

    private static final String TAG = Logger.makeLogTag(DocumentTestPd.class);

    /**
     * Информация о последнем проданном ПД
     */
    private PrintTestTicketOperation.Result printResult;
    /**
     * Рассчитанный порядковый номер документа.
     * Вычисляется перед печатью фискального чека.
     */
    private int pdNumber;
    /**
     * Станция привязки
     */
    private String bindingStationName;
    /**
     * Id связанного события продажи тестового ПД
     */
    private long actualTestSaleEventId;

    private final LocalDaoSession localDaoSession;
    private final ShiftManager shiftManager;
    private final EventBuilder eventBuilder;
    private final DocumentNumberProvider documentNumberProvider;
    private final PrivateSettings privateSettings;
    private final FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder;
    private final UserSessionInfo userSessionInfo;
    private final OperationFactory operationFactory;
    private final PaperUsageCounter paperUsageCounter;
    private final Holder<PrivateSettings> privateSettingsHolder;
    private final NsiVersionManager nsiVersionManager;
    private final StationRepository stationRepository;

    public DocumentTestPd(LocalDaoSession localDaoSession,
                          ShiftManager shiftManager,
                          EventBuilder eventBuilder,
                          DocumentNumberProvider documentNumberProvider,
                          PrivateSettings privateSettings,
                          FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder,
                          UserSessionInfo userSessionInfo,
                          OperationFactory operationFactory,
                          PaperUsageCounter paperUsageCounter,
                          Holder<PrivateSettings> privateSettingsHolder,
                          NsiVersionManager nsiVersionManager,
                          StationRepository stationRepository) {

        this.localDaoSession = localDaoSession;
        this.shiftManager = shiftManager;
        this.eventBuilder = eventBuilder;
        this.documentNumberProvider = documentNumberProvider;
        this.privateSettings = privateSettings;
        this.fiscalHeaderParamsBuilder = fiscalHeaderParamsBuilder;
        this.userSessionInfo = userSessionInfo;
        this.operationFactory = operationFactory;
        this.paperUsageCounter = paperUsageCounter;
        this.privateSettingsHolder = privateSettingsHolder;
        this.nsiVersionManager = nsiVersionManager;
        this.stationRepository = stationRepository;
    }

    private void preparePdNumber() {
        pdNumber = documentNumberProvider.getNextDocumentNumber();
        Logger.trace(TAG, "preparePdNumber, pdNumber = " + pdNumber);
    }

    private void prepareBindingStation() {
        Logger.trace(TAG, "prepareBindingStation START");
        long station_code = (long) privateSettingsHolder.get().getSaleStationCode();
        int nsi_version_id = nsiVersionManager.getCurrentNsiVersionId();
        nsi_version_id = 211;
        Logger.trace(TAG, "station_code = " + station_code + ", nsi_version_id = " + nsi_version_id);
        Station bindingStation = stationRepository.load(station_code, nsi_version_id);
//        Station bindingStation = stationRepository.load((long) privateSettingsHolder.get().getSaleStationCode(), nsiVersionManager.getCurrentNsiVersionId());
        bindingStationName = bindingStation.getShortName();
        Logger.trace(TAG, "prepareBindingStation, bindingStationName = " + bindingStationName);
    }

    /**
     * Собирает параметры для шаблона печати чека тестового ПД
     *
     * @return Параметры
     */
    private PrintTestTicketOperation.Params buildCheckParams(int documentNumber) {

        PrintTestTicketOperation.Params params = new PrintTestTicketOperation.Params();

        params.setPdNumber(documentNumber);
        params.setCashierName(userSessionInfo.getCurrentUser().getName());
        params.setDayCode(privateSettings.getDayCode());
        params.setPtkNumber(privateSettings.getTerminalNumber());
        params.setHeaderParams(fiscalHeaderParamsBuilder.build());

        TestTicketTpl.Params testTicketTplParams = new TestTicketTpl.Params();
        testTicketTplParams.setPdNumber(pdNumber);
        testTicketTplParams.setBindingStationName(bindingStationName);
        params.setTestTicketTplParams(testTicketTplParams);
        return params;
    }

    /**
     * Выполняет печать документа на принтере
     */
    public Single<DocumentTestPd> print() {
        Logger.trace(TAG, "print");
        return checkTicketTapeIsSet()
                .flatMap(isTicketTapeSet -> {
                    if (isTicketTapeSet) {
                        return Observable
                                .fromCallable(() -> {

                                    TestTicketEvent testTicketEvent = getTestTicketEvent();

                                    if (testTicketEvent.getStatus() != TestTicketEvent.Status.PRE_PRINTING) {
                                        throw new IllegalStateException("Invalid status for operation = " + testTicketEvent.getStatus());
                                    }

                                    preparePdNumber();
                                    prepareBindingStation();

                                    return buildCheckParams(pdNumber);
                                })
                                .flatMap(params -> operationFactory.getGetOdometerValue()
                                        .call()
                                        .doOnNext(result -> paperUsageCounter.setCurrentOdometerValueBeforePrinting(result.getOdometerValue()))
                                        .flatMap(result -> Observable.just(params))
                                )
                                .flatMap(params -> operationFactory.getPrintTestTicketOperation(params).call().toObservable())
                                .doOnNext(result -> printResult = result)
                                .flatMap(result -> Observable.just(DocumentTestPd.this));
                    } else {
                        return Observable.error(new PrettyException(Globals.getInstance().getString(R.string.error_msg_ticket_tape_is_not_set)));
                    }
                }).toSingle();
    }

    /**
     * Проверяет, установлена ли билетная лента
     */
    private Observable<Boolean> checkTicketTapeIsSet() {
        return Observable.just(localDaoSession.getTicketTapeEventDao().isTicketTapeSet());
    }

    public int getPdNumber() {
        return pdNumber;
    }

    public int getSpnd() {
        return printResult.getSpnd();
    }

    public Date getSaleDateTime() {
        return printResult.getOperationTime();
    }

    /**
     * Проверят статус текущего события печати данного документа, если это уже не первая попытка.
     */
    private void checkTestTicketEventCouldBeCreated() {
        Logger.trace(TAG, "checkTestTicketEventCouldBeCreated, actualTestSaleEventId = " + actualTestSaleEventId);
        if (actualTestSaleEventId == 0) {
            // Пока ещё не было попыток печати данного документа
            // Можно не переживать, что есть событие печати данного документа в несинхронизированном состоянии
            return;
        }

        TestTicketEvent testTicketEvent = getTestTicketEvent();

        switch (testTicketEvent.getStatus()) {
            case PRE_PRINTING: {
                throw new IllegalStateException("Previous event state for this document is not synchronized");
            }
            case CHECK_PRINTED:
            case COMPLETED: {
                throw new IllegalStateException("Document is already in FR, event could not be created twice");
            }
        }
    }

    @NonNull
    private TestTicketEvent getTestTicketEvent() {
        if (actualTestSaleEventId == 0) {
            throw new IllegalStateException("Method should not be called before creating event");
        }
        TestTicketEvent testTicketEvent = localDaoSession.getTestTicketDao().load(actualTestSaleEventId);
        if (testTicketEvent == null) {
            throw new IllegalStateException("TestTicketEvent not found");
        }
        return testTicketEvent;
    }

    /**
     * Обновляет событие печати тестового ПД в БД до статуса {@link TestTicketEvent.Status#PRE_PRINTING}.
     */
    public void updateStatusPrePrinting() {
        Logger.trace(TAG, "updateStatusPrePrinting");

        TestTicketEvent testTicketEvent = getTestTicketEvent();

        if (testTicketEvent.getStatus() != TestTicketEvent.Status.CREATED) {
            throw new IllegalStateException("Invalid status for operation = " + testTicketEvent.getStatus());
        }

        localDaoSession.beginTransaction();
        try {
            testTicketEvent.setStatus(TestTicketEvent.Status.PRE_PRINTING);

            localDaoSession.getTestTicketDao().update(testTicketEvent);

            localDaoSession.setTransactionSuccessful();
            shiftManager.refreshState();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Обновляет событие печати тестового ПД в БД до статуса {@link TestTicketEvent.Status#CHECK_PRINTED}.
     */
    public void updateStatusPrinted() {
        Logger.trace(TAG, "updateStatusPrinted");

        TestTicketEvent testTicketEvent = getTestTicketEvent();

        if (testTicketEvent.getStatus() != TestTicketEvent.Status.PRE_PRINTING) {
            throw new IllegalStateException("Invalid status for operation = " + testTicketEvent.getStatus());
        }

        localDaoSession.beginTransaction();
        try {
            // Чек
            Check check = new CheckBuilder()
                    .setDocumentNumber(pdNumber)
                    .setSnpdNumber(printResult.getSpnd())
                    .setPrintDateTime(printResult.getOperationTime())
                    .build();
            long checkId = localDaoSession.getCheckDao().insertOrThrow(check);
            testTicketEvent.setCheckId(checkId);
            // Билетная лента
            TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();
            testTicketEvent.setTicketTapeEventId(ticketTapeEvent.getId());
            // Статус
            testTicketEvent.setStatus(TestTicketEvent.Status.CHECK_PRINTED);
            localDaoSession.getTestTicketDao().update(testTicketEvent);

            localDaoSession.setTransactionSuccessful();
            shiftManager.refreshState();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Обновляет событие печати тестового ПД в БД до статуса {@link TestTicketEvent.Status#COMPLETED}.
     */
    public void updateStatusCompleted() {
        Logger.trace(TAG, "updateStatusCompleted");

        TestTicketEvent testTicketEvent = getTestTicketEvent();

        if (testTicketEvent.getStatus() != TestTicketEvent.Status.CHECK_PRINTED) {
            throw new IllegalStateException("Invalid status for operation = " + testTicketEvent.getStatus());
        }

        localDaoSession.beginTransaction();
        try {
            testTicketEvent.setStatus(TestTicketEvent.Status.COMPLETED);
            localDaoSession.getTestTicketDao().update(testTicketEvent);
            localDaoSession.setTransactionSuccessful();
            shiftManager.refreshState();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Создает событие продажи тестового ПД в БД в статусе {@link TestTicketEvent.Status#CREATED}.
     */
    public void createTestSaleEvent() {
        Logger.trace(TAG, "createTestSaleEvent");

        // Проверяем статус последнего события печати данного документа
        checkTestTicketEventCouldBeCreated();

        // Сбрасываем информацию о предыдщем событии печати данного документа, если чек не лег на ФР
        actualTestSaleEventId = 0;

        localDaoSession.beginTransaction();

        try {

            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (shiftEvent == null)
                throw new IllegalArgumentException("CashRegisterWorkingShift is null - incorrect state");

            TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();

            if (ticketTapeEvent.getEndTime() != null) {
                throw new IllegalArgumentException("testTicketEvent.getTicketTapeEvent().getEndTime() != null");
            }

            // добавляем информацию о ПТК
            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
            if (stationDevice != null) {
                localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
            }
            Event event = eventBuilder
                    .setDeviceId(stationDevice.getId())
                    .build();
            long eventId = localDaoSession.getEventDao().insertOrThrow(event);

            // TestTicketEvent
            TestTicketEvent testTicketEvent = new TestTicketEvent();
            testTicketEvent.setEventId(eventId);
            testTicketEvent.setShiftEventId(shiftEvent.getId());
            testTicketEvent.setCheckId(0); //чека на текущий момент еще нет
            testTicketEvent.setTicketTapeEventId(ticketTapeEvent.getId());
            testTicketEvent.setStatus(TestTicketEvent.Status.CREATED);

            long id = localDaoSession.getTestTicketDao().insertOrThrow(testTicketEvent);

            AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                    .setType(AuditTrailEventType.PRINT_TEST_PD)
                    .setExtEventId(id)
                    .setOperationTime(event.getCreationTimestamp())
                    .setShiftEventId(shiftEvent.getId())
                    .setMonthEventId(shiftEvent.getMonthEventId())
                    .build();

            localDaoSession.getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

            localDaoSession.setTransactionSuccessful();

            this.actualTestSaleEventId = id;
            shiftManager.refreshState();

        } finally {
            localDaoSession.endTransaction();
        }
    }

}
