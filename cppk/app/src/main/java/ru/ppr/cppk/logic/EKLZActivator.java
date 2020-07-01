package ru.ppr.cppk.logic;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.HolderDefault;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.utils.builders.events.CPPKServiceSaleBuilder;
import ru.ppr.cppk.entity.utils.builders.events.PriceBuilder;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.printer.rx.operation.PrinterGetShiftsInfo;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ServiceFee;
import rx.Observable;
import rx.Single;

/**
 * Активатор ЭКЛЗ.
 *
 * @author Aleksandr Brazhkin
 */
public class EKLZActivator {

    private static final String TAG = Logger.makeLogTag(EKLZActivator.class);

    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final ShiftManager shiftManager;
    private final PaperUsageCounter paperUsageCounter;

    public EKLZActivator(LocalDaoSession localDaoSession, NsiVersionManager nsiVersionManager, ShiftManager shiftManager, PaperUsageCounter paperUsageCounter) {
        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.shiftManager = shiftManager;
        this.paperUsageCounter = paperUsageCounter;
    }

    private LocalDaoSession getLocalDaoSession() {
        return localDaoSession;
    }

    /**
     * Функция активизации ЭКЛЗ
     *
     * @param printer Принтер, с которым выполняется работа
     * @return
     */
    public Single<Void> activateEKLZ(IPrinter printer) {

        Holder<CashRegister> cashRegisterHolder = new HolderDefault<>();
        return Single
                .fromCallable(() -> {
                    closeShiftIfNeed();
                    return (Void) null;
                })
                .flatMap(aVoid -> Di.INSTANCE.printerManager().getCashRegisterFromPrinter(printer))
                .doOnSuccess(cashRegister -> cashRegisterHolder.set(cashRegister))
                .flatMapObservable((CashRegister cashRegister) -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                .flatMap(printerState -> {
                    PrinterGetShiftsInfo.Params params = new PrinterGetShiftsInfo.Params();
                    params.startShiftNum = 1;
                    params.endShiftNum = printerState.getShiftNum();
                    return Di.INSTANCE.printerManager().getOperationFactory().getGetShiftsInfo().setParams(params).call();
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(closedShiftInfoList -> Observable.fromCallable(() -> {
                            addShiftEventsToDb(cashRegisterHolder.get(), closedShiftInfoList);
                            return (Void) null;
                        })
                )
                .toSingle();
    }

    /**
     * Принудительно закрывает смену на ПТК по данным из БД.
     *
     * @throws Exception
     */
    private void closeShiftIfNeed() throws Exception {

        if (shiftManager.isShiftClosed()) {
            return;
        }

        Date currentDate = new Date();

        if (currentDate.before(shiftManager.getStartShiftEvent().getStartTime())) {
            throw new IllegalStateException("Shift couldn't be finished before opening");
        }

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(nsiVersionManager.getCurrentNsiVersionId())
                .setShiftId(shiftManager.getCurrentShiftId())
                .build();
        BigDecimal cashInFR = pdStatistics.countAndProfit.profit.total.subtract(pdStatistics.countAndProfit.profit.totalRepeal);
        PaperUsage paperUsage = paperUsageCounter.getPaperUsage(PaperUsage.ID_SHIFT);
        int spndNumber = localDaoSession.getCheckDao().getLastCheck().getSnpdNumber() + 1;

        getLocalDaoSession().beginTransaction();
        try {
            ShiftEvent closeShiftEvent = shiftManager.createCloseShiftEvent(paperUsage.getPaperLength(), paperUsage.isRestarted(), cashInFR);
            shiftManager.closeShiftEventUpdateToComplete(new Date(), closeShiftEvent.getId(), spndNumber);
            getLocalDaoSession().setTransactionSuccessful();
        } finally {
            getLocalDaoSession().endTransaction();
        }

    }

    /**
     * Добавляет в БД информацию по сменам, полученную с принетра
     *
     * @param cashRegister        Информация об ФР
     * @param closedShiftInfoList Информация по сменам
     * @throws Exception
     */
    private void addShiftEventsToDb(CashRegister cashRegister, List<IPrinter.ClosedShiftInfo> closedShiftInfoList) throws Exception {

        getLocalDaoSession().beginTransaction();

        Logger.trace(TAG, "addShiftEventsToDb started");

        try {
            long lastEventTimeStamp = getLocalDaoSession().getEventDao().getLastEventTimeStamp();
            Calendar previousEventCalendar = Calendar.getInstance();
            previousEventCalendar.setTimeInMillis(lastEventTimeStamp);
            Calendar currentCalendar = Calendar.getInstance();

            Dagger.appComponent().cashRegisterEventCreator()
                    .setCashRegister(cashRegister)
                    .create();

            MonthEvent monthEvent = getLocalDaoSession().getMonthEventDao().getLastMonthEvent();

            if (monthEvent == null) {
                throw new IllegalStateException("Month is not opened");
            }

            if (currentCalendar.before(previousEventCalendar)) {
                throw new IllegalStateException("We are in the past");
            }

            for (IPrinter.ClosedShiftInfo closedShiftInfo : closedShiftInfoList) {

                int shiftNum = closedShiftInfo.getNumber();
                BigDecimal cashInFR = closedShiftInfo.getTotalSaleSum();
                String shiftId = UUID.randomUUID().toString();

                currentCalendar.setTimeInMillis(System.currentTimeMillis());
                Logger.trace(TAG, "previousEventCalendar " + previousEventCalendar.getTime().toString());
                Logger.trace(TAG, "currentCalendar " + currentCalendar.getTime().toString());
                if (previousEventCalendar.get(Calendar.MINUTE) == currentCalendar.get(Calendar.MINUTE)) {
                    int secondToNextMinute = 60 - previousEventCalendar.get(Calendar.SECOND);
                    Logger.trace(TAG, "The same minute, start sleep for " + secondToNextMinute + " seconds");
                    Thread.sleep(secondToNextMinute * 1000);
                }

                Date openTime = new Date();

                ShiftEvent shiftEventStarted = Dagger.appComponent().shiftEventCreator()
                        .setShiftId(shiftId)
                        .setStatus(ShiftEvent.Status.STARTED)
                        .setShiftNumber(shiftNum)
                        .setStartTime(openTime)
                        .setOperationTime(openTime)
                        .setPaperConsumption(0)
                        .setPaperCounterRestarted(false)
                        .setCashInFR(BigDecimal.ZERO)
                        .setCheck(null)
                        .setProgressStatus(ShiftEvent.ShiftProgressStatus.COMPLETED)
                        .create();

                Event eventOpenShift = localDaoSession.getEventDao().load(shiftEventStarted.getEventId());
                previousEventCalendar.setTime(eventOpenShift.getCreationTimestamp());
                Logger.trace(TAG, "OpenShift added, id = " + shiftEventStarted.getId() + " , timestamp = " + eventOpenShift.getCreationTimestamp());

                TicketTapeEvent ticketTapeEvent = getLocalDaoSession().getTicketTapeEventDao().getInstalledTicketTape();

                Price price = new PriceBuilder()
                        .setNds(BigDecimal.ZERO)
                        .setFull(cashInFR)
                        .setPayed(cashInFR)
                        .setSumForReturn(BigDecimal.ZERO)
                        .build();

                // добавляем информацию о ПТК
                StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                if (stationDevice != null) {
                    localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
                }
                Event eventServiceSale = Di.INSTANCE.eventBuilder()
                        .setDeviceId(stationDevice.getId())
                        .build();

                Preconditions.checkNotNull(eventServiceSale, "Event is null");
                getLocalDaoSession().getEventDao().insertOrThrow(eventServiceSale);

                ServiceFee serviceFee = Dagger.appComponent().serviceFeeRepository().load(
                        (long) ServiceFee.SERVICE_FEE_CODE_EKLZ_CHANGE,
                        nsiVersionManager.getNsiVersionIdForDate(eventServiceSale.getCreationTimestamp()));

                Check check = new CheckBuilder()
                        .setDocumentNumber(Di.INSTANCE.documentNumberProvider().getNextDocumentNumber())
                        .setSnpdNumber(0)
                        .setPrintDateTime(eventServiceSale.getCreationTimestamp())
                        .build();

                CPPKServiceSale cppkServiceSale = new CPPKServiceSaleBuilder()
                        .setEvent(eventServiceSale)
                        .setShiftEvent(shiftEventStarted)
                        .setTicketTapeEvent(ticketTapeEvent)
                        .setPrice(price)
                        .setCheck(check)
                        .setSaleDateTime(eventServiceSale.getCreationTimestamp())
                        .setServiceFee(serviceFee)
                        .build();

                Preconditions.checkNotNull(shiftEventStarted, "CashRegisterWorkingShift is null");
                shiftEventStarted.setId(shiftEventStarted.getId());

                if (check != null) {
                    long checkId = getLocalDaoSession().getCheckDao().insertOrThrow(check);
                    cppkServiceSale.setCheckId(checkId);
                }

                Preconditions.checkNotNull(price, "Price is null");
                long priceId = getLocalDaoSession().getPriceDao().insertOrThrow(price);
                cppkServiceSale.setPriceId(priceId);

                long id = getLocalDaoSession().getCppkServiceSaleDao().insertOrThrow(cppkServiceSale);

                AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                        .setType(AuditTrailEventType.SERVICE_SALE)
                        .setExtEventId(id)
                        .setOperationTime(eventServiceSale.getCreationTimestamp())
                        .setShiftEventId(shiftEventStarted.getId())
                        .setMonthEventId(shiftEventStarted.getMonthEventId())
                        .build();

                getLocalDaoSession().getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

                Logger.trace(TAG, "CPPKServiceSale added, id = " + cppkServiceSale.getId() + " , timestamp = " + eventServiceSale.getCreationTimestamp());

                currentCalendar.setTimeInMillis(System.currentTimeMillis());
                Logger.trace(TAG, "previousEventCalendar " + previousEventCalendar.getTime().toString());
                Logger.trace(TAG, "currentCalendar " + currentCalendar.getTime().toString());
                if (previousEventCalendar.get(Calendar.MINUTE) == currentCalendar.get(Calendar.MINUTE)) {
                    int secondToNextMinute = 60 - previousEventCalendar.get(Calendar.SECOND);
                    Logger.trace(TAG, "The same minute, start sleep for " + secondToNextMinute + " seconds");
                    Thread.sleep(secondToNextMinute * 1000);
                }

                Date closeTime = new Date();

                ShiftEvent shiftEventEnded = Dagger.appComponent().shiftEventCreator()
                        .setShiftId(shiftId)
                        .setStatus(ShiftEvent.Status.ENDED)
                        .setShiftNumber(shiftNum)
                        .setStartTime(openTime)
                        .setOperationTime(closeTime)
                        .setCloseTime(closeTime)
                        .setPaperConsumption(0)
                        .setPaperCounterRestarted(true)
                        .setCashInFR(cashInFR)
                        .setCheck(null)
                        .setProgressStatus(ShiftEvent.ShiftProgressStatus.COMPLETED)
                        .create();

                Event eventCloseShift = localDaoSession.getEventDao().load(shiftEventEnded.getEventId());

                previousEventCalendar.setTime(eventCloseShift.getCreationTimestamp());
                Logger.trace(TAG, "CloseShift added, id = " + shiftEventEnded.getId() + " , timestamp = " + eventCloseShift.getCreationTimestamp());
            }

            Logger.trace(TAG, "addShiftEventsToDb successful");

            getLocalDaoSession().setTransactionSuccessful();

            Di.INSTANCE.printerManager().setCashRegister(cashRegister);
        } catch (Exception e) {
            Logger.trace(TAG, "addShiftEventsToDb error", e);
            throw e;
        } finally {
            Logger.trace(TAG, "addShiftEventsToDb ended");
            getLocalDaoSession().endTransaction();
        }
    }
}
