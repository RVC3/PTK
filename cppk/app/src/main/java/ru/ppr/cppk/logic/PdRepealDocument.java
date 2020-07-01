package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.ReturnOperationType;
import ru.ppr.cppk.entity.utils.builders.events.PriceBuilder;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.logic.pdRepeal.PdRepealData;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.PrintRepeatRepealCheckOperation;
import ru.ppr.cppk.printer.rx.operation.repealCheck.RepealCheckTpl;
import ru.ppr.cppk.ui.helper.TicketTypeStringifier;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.cppk.utils.SlipConverter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.repository.TariffRepository;
import rx.Completable;
import rx.Single;

/**
 * Документ "Чек аннулирования"
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealDocument {

    private static final String TAG = Logger.makeLogTag(PdRepealDocument.class);

    private final PdRepealData pdRepealData;
    private final FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder;
    private final DocumentNumberProvider documentNumberProvider;
    private final OperationFactory operationFactory;
    private final LocalDaoSession localDaoSession;
    private final NsiDaoSession nsiDaoSession;
    private final EventBuilder eventBuilder;
    private final PaperUsageCounter paperUsageCounter;
    private final TariffRepository tariffRepository;
    private final TicketTypeStringifier ticketTypeStringifier;
    /**
     * Id связанного события аннулирования ПД
     */
    private long actualPdRepealEventId;
    /**
     * Рассчитанный порядковый номер документа.
     * Вычисляется перед печатью фискального чека.
     */
    private int pdNumber;
    /**
     * Результат печати чека
     */
    private PrintRepealCheckOperation.Result printResult;

    PdRepealDocument(PdRepealData pdRepealData,
                     FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder,
                     DocumentNumberProvider documentNumberProvider,
                     OperationFactory operationFactory,
                     LocalDaoSession localDaoSession,
                     NsiDaoSession nsiDaoSession,
                     EventBuilder eventBuilder,
                     PaperUsageCounter paperUsageCounter,
                     TariffRepository tariffRepository) {
        this.pdRepealData = pdRepealData;
        this.fiscalHeaderParamsBuilder = fiscalHeaderParamsBuilder;
        this.documentNumberProvider = documentNumberProvider;
        this.operationFactory = operationFactory;
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
        this.eventBuilder = eventBuilder;
        this.paperUsageCounter = paperUsageCounter;
        this.tariffRepository = tariffRepository;
        this.ticketTypeStringifier = Dagger.appComponent().ticketTypeStringifier();

    }

    public long getActualPdRepealEventId() {
        return actualPdRepealEventId;
    }

    public void setActualPdRepealEventId(long actualPdRepealEventId) {
        this.actualPdRepealEventId = actualPdRepealEventId;
    }

    private void preparePdNumber() {
        pdNumber = documentNumberProvider.getNextDocumentNumber();
        Logger.trace(TAG, "preparePdNumber, pdNumber = " + pdNumber);
    }

    /**
     * Собирает параметры для шаблона печати чека аннулирования
     *
     * @return Параметры
     */
    private PrintRepealCheckOperation.Params buildCheckParams() {
        PrintRepealCheckOperation.Params params = new PrintRepealCheckOperation.Params();

        CPPKTicketSales pdSaleEvent = pdRepealData.getPdSaleEvent();

        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());

        BigDecimal lossSum = BigDecimal.ZERO;
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        BigDecimal full = fullPrice.getFull();
        BigDecimal nds = fullPrice.getNds();
        BigDecimal payed = fullPrice.getPayed();
        BigDecimal feeTotal = fee == null ? BigDecimal.ZERO : fee.getTotal();
        BigDecimal feeNds = fee == null ? BigDecimal.ZERO : fee.getNds();
        BigDecimal fullTicketPrice = pdSaleEvent.getFullTicketPrice();

        if (exemptionForEvent != null) {
            lossSum = exemptionForEvent.getLossSumm();
        }

        params.feeValue = feeTotal;
        params.feeVatValue = feeNds;
        params.feeVatRate = Decimals.getVATRateIncludedFromValue(feeTotal, feeNds, Decimals.RoundMode.WITHOUT);
        params.ticketCostValueWithoutDiscount = fullTicketPrice;
        params.ticketCostValueWithDiscount = fullTicketPrice.subtract(lossSum);
        params.ticketCostVatValue = nds.subtract(feeNds);
        Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        params.ticketCostVatRate = tariff.getTicketType(nsiDaoSession).getTax();
        params.pdNumber = pdNumber;
        params.payment = payed;
        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        params.paymentType = bankTransactionEvent == null ? PaymentType.INDIVIDUAL_CASH : PaymentType.INDIVIDUAL_BANK_CARD;

        params.headerParams = fiscalHeaderParamsBuilder.build();

        RepealCheckTpl.Params repealCheckTplParams = new RepealCheckTpl.Params();
        params.repealCheckTplParams = repealCheckTplParams;
        repealCheckTplParams.pdNumber = pdNumber;
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        repealCheckTplParams.repealPdNumber = check.getOrderNumber();
        repealCheckTplParams.repealReason = pdRepealData.getRepealReason();
        repealCheckTplParams.repealPdSaleTime = ticketEventBase.getSaledateTime();
        repealCheckTplParams.smartCard = smartCard;
        repealCheckTplParams.repealPdTicketTypeName = ticketTypeStringifier.stringify(
                exemptionForEvent != null,
                fullPrice,
                fee,
                ticketEventBase.getTicketTypeShortName()
        );


        return params;
    }

    /**
     * Собирает параметры для шаблона печати дубликата чека оформления штрафа
     *
     * @return Параметры
     */
    private PrintRepeatRepealCheckOperation.Params buildRepeatCheckParams(Date printDateTime) {

        CPPKTicketSales pdSaleEvent = pdRepealData.getPdSaleEvent();

        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());

        PrintRepeatRepealCheckOperation.Params params = new PrintRepeatRepealCheckOperation.Params();
        params.headerParams = fiscalHeaderParamsBuilder.build();
        RepealCheckTpl.Params repealCheckTplParams = new RepealCheckTpl.Params();
        params.repealCheckTplParams = repealCheckTplParams;
        repealCheckTplParams.pdNumber = pdNumber;
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        repealCheckTplParams.repealPdNumber = check.getOrderNumber();
        repealCheckTplParams.repealReason = pdRepealData.getRepealReason();
        repealCheckTplParams.repealPdSaleTime = ticketEventBase.getSaledateTime();
        repealCheckTplParams.smartCard = smartCard;
        repealCheckTplParams.printCheckDateTime = printDateTime;
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        repealCheckTplParams.repealPdTicketTypeName = ticketTypeStringifier.stringify(
                exemptionForEvent != null,
                fullPrice,
                fee,
                ticketEventBase.getTicketTypeShortName()
        );
        return params;
    }

    /**
     * Проверят статус текущего события печати данного документа, если это уже не первая попытка.
     */
    private void checkRepealEventCouldBeCreated() {
        Logger.trace(TAG, "checkRepealEventCouldBeCreated, actualPdRepealEventId = " + actualPdRepealEventId);
        if (actualPdRepealEventId == 0) {
            // Пока ещё не было попыток печати данного документа
            // Можно не переживать, что есть событие печати данного документа в несинхронизированном состоянии
            return;
        }

        CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

        switch (pdRepealEvent.getProgressStatus()) {
            case PrePrinting: {
                throw new IllegalStateException("Previous event state for this document is not synchronized");
            }
            case CheckPrinted:
            case Completed: {
                throw new IllegalStateException("Document is already in FR, event could not be created twice");
            }
        }
    }

    @NonNull
    private CPPKTicketReturn getPdRepealEvent() {
        if (actualPdRepealEventId == 0) {
            throw new IllegalStateException("Method should not be called before creating event");
        }
        CPPKTicketReturn pdRepealEvent = localDaoSession.getCppkTicketReturnDao().load(actualPdRepealEventId);
        if (pdRepealEvent == null) {
            throw new IllegalStateException("PdRepealEvent not found");
        }
        return pdRepealEvent;
    }

    /**
     * Создает событие аннулирования ПД в БД в статусе {@link ProgressStatus#CREATED}.
     */
    public void createPdRepealEvent() {
        Logger.trace(TAG, "createPdRepealEvent");

        // Проверяем статус последнего события печати данного документа
        checkRepealEventCouldBeCreated();

        // Сбрасываем информацию о предыдущем событии печати данного документа, если чек не лег на ФР
        actualPdRepealEventId = 0;

        CPPKTicketSales saleEvent = pdRepealData.getPdSaleEvent();
        Preconditions.checkNotNull(saleEvent, "CPPKTicketSales is null");

        TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();
        Preconditions.checkNotNull(ticketTapeEvent, "TicketTapeEvent is null");

        //Т.к. TicketSaleReturnEventBase берется из уже проданного билета,
        // то заново сохранять его не надо, просто берем его ИД
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(saleEvent.getTicketSaleReturnEventBaseId());

        Price sellPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        Preconditions.checkNotNull(sellPrice, "Price is null");

        ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        //Т.к. CashRegisterWorkingShift берется текущее
        // то заново сохранять его не надо, просто берем его ИД
        Preconditions.checkNotNull(shiftEvent, "CashRegisterWorkingShift is null");

        localDaoSession.beginTransaction();
        try {
            Preconditions.checkNotNull(ticketTapeEvent, "TicketTapeEvent is null");
            if (ticketTapeEvent.getEndTime() != null) {
                throw new IllegalStateException("cppkTicketReturn.getTicketTapeEvent().getEndTime() != null");
            }

            // добавляем информацию о ПТК
            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
            if (stationDevice != null) {
                localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
            }
            Event event = eventBuilder
                    .setDeviceId(stationDevice.getId())
                    .build();
            Preconditions.checkNotNull(event, "Event is null");
            localDaoSession.getEventDao().insertOrThrow(event);

            Price price = new PriceBuilder()
                    .setNds(sellPrice.getNds())
                    .setFull(sellPrice.getFull())
                    .setPayed(sellPrice.getPayed())
                    .setSumForReturn(sellPrice.getSumForReturn())
                    .build();
            Preconditions.checkNotNull(price, "Price is null");
            localDaoSession.getPriceDao().insertOrThrow(price);

            CPPKTicketReturn pdRepealEvent = new CPPKTicketReturn();
            pdRepealEvent.setPdSaleEventId(saleEvent.getId());
            pdRepealEvent.setBankTransactionCashRegisterEventId(-1);
            pdRepealEvent.setEventId(event.getId());
            pdRepealEvent.setCashRegisterWorkingShiftId(shiftEvent.getId());
            pdRepealEvent.setPriceId(price.getId());
            pdRepealEvent.setRecallReason(pdRepealData.getRepealReason());
            pdRepealEvent.setOperation(ReturnOperationType.Annulment); // на ПТК только аннулирование
            pdRepealEvent.setTicketTapeEventId(ticketTapeEvent.getId());
            pdRepealEvent.setSumToReturn(sellPrice.getSumForReturn());

            BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
            boolean pdSaleEventHasBankTransaction = bankTransactionEvent != null;
            if (pdSaleEventHasBankTransaction) {
                pdRepealEvent.setReturnPaymentMethod(PaymentType.INDIVIDUAL_BANK_CARD);
            } else {
                pdRepealEvent.setReturnPaymentMethod(PaymentType.INDIVIDUAL_CASH);
            }
            pdRepealEvent.setProgressStatus(ProgressStatus.CREATED);
            long id = localDaoSession.getCppkTicketReturnDao().insertOrThrow(pdRepealEvent);

            AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                    .setType(AuditTrailEventType.RETURN)
                    .setExtEventId(id)
                    .setOperationTime(event.getCreationTimestamp())
                    .setShiftEventId(shiftEvent.getId())
                    .setMonthEventId(shiftEvent.getMonthEventId())
                    .build();
            localDaoSession.getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

            localDaoSession.setTransactionSuccessful();

            this.actualPdRepealEventId = pdRepealEvent.getId();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Обновляет событие аннулирования ПД в БД до статуса {@link ProgressStatus#PrePrinting}.
     */
    public void updateStatusPrePrinting() {
        Logger.trace(TAG, "updateStatusPrePrinting");

        CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

        if (pdRepealEvent.getProgressStatus() != ProgressStatus.CREATED && pdRepealEvent.getProgressStatus() != ProgressStatus.Broken) {
            throw new IllegalStateException("Invalid status for operation = " + pdRepealEvent.getProgressStatus());
        }

        localDaoSession.beginTransaction();
        try {
            long bankTransactionEventId = pdRepealData.getBankTransactionEventId();

            CPPKTicketSales pdSaleEvent = pdRepealData.getPdSaleEvent();

            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
            BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
            boolean pdSaleEventHasBankTransaction = bankTransactionEvent != null;

            if (pdSaleEventHasBankTransaction) {
                if (bankTransactionEventId == -1) {
                    throw new IllegalStateException("Sale event with bank transaction event can't be repealed without bank transaction event");
                }
                if (pdRepealData.getSlipReceipt() == null) {
                    throw new IllegalStateException("Slip should not be null");
                }
                pdRepealEvent.setBankTransactionCashRegisterEventId(bankTransactionEventId);
                pdRepealEvent.setReturnBankTerminalSlip(SlipConverter.toImage(pdRepealData.getSlipReceipt()));
            }

            pdRepealEvent.setProgressStatus(ProgressStatus.PrePrinting);
            localDaoSession.getCppkTicketReturnDao().update(pdRepealEvent);
            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Обновляет событие аннулирования ПД в БД до статуса {@link ProgressStatus#CheckPrinted}.
     */
    public void updateStatusPrinted() {
        Logger.trace(TAG, "updateStatusPrinted");

        CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

        if (pdRepealEvent.getProgressStatus() != ProgressStatus.PrePrinting) {
            throw new IllegalStateException("Invalid status for operation = " + pdRepealEvent.getProgressStatus());
        }

        localDaoSession.beginTransaction();
        try {
            Check check = new CheckBuilder()
                    .setDocumentNumber(pdNumber)
                    .setSnpdNumber(printResult.getSpnd())
                    .setPrintDateTime(printResult.getOperationTime())
                    .build();

            localDaoSession.getCheckDao().insertOrThrow(check);

            pdRepealEvent.setCheckId(check.getId());
            pdRepealEvent.setRecallDateTime(printResult.getOperationTime());
            pdRepealEvent.setProgressStatus(ProgressStatus.CheckPrinted);

            localDaoSession.getCppkTicketReturnDao().update(pdRepealEvent);

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Обновляет событие аннулирования ПД в БД до статуса {@link ProgressStatus#Completed}.
     */
    public void updateStatusCompleted() {
        Logger.trace(TAG, "updateStatusCompleted");

        CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

        if (pdRepealEvent.getProgressStatus() != ProgressStatus.CheckPrinted) {
            throw new IllegalStateException("Invalid status for operation = " + pdRepealEvent.getProgressStatus());
        }

        localDaoSession.beginTransaction();
        try {
            pdRepealEvent.setProgressStatus(ProgressStatus.Completed);
            localDaoSession.getCppkTicketReturnDao().update(pdRepealEvent);
            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    /**
     * Выполняет печать чека аннулирования ПД.
     */
    public Single<PdRepealDocument> print() {
        Logger.trace(TAG, "print");
        return Completable
                .fromAction(() -> {
                    CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

                    if (pdRepealEvent.getProgressStatus() != ProgressStatus.PrePrinting) {
                        throw new IllegalStateException("Invalid status for operation = " + pdRepealEvent.getProgressStatus());
                    }

                    preparePdNumber();
                })
                .andThen(updateOdometerValue())
                .andThen(Single.defer(() -> operationFactory.getPrintRepealCheckOperation(buildCheckParams()).call()))
                .doOnSuccess(result -> printResult = result)
                .map(result -> this)
                .subscribeOn(SchedulersCPPK.printer());
    }

    /**
     * Выполняет печать дубликата чека аннулирования ПД.
     */
    public Single<PdRepealDocument> printDuplicateReceipt() {
        Logger.trace(TAG, "printDuplicateReceipt");
        return Single
                .fromCallable(() -> {
                    CPPKTicketReturn pdRepealEvent = getPdRepealEvent();

                    if (pdRepealEvent.getProgressStatus() != ProgressStatus.CheckPrinted) {
                        throw new IllegalStateException("Invalid status for operation = " + pdRepealEvent.getProgressStatus());
                    }
                    return localDaoSession.getCheckDao().load(pdRepealEvent.getCheckId());
                })
                .flatMapCompletable(check -> operationFactory.getPrintRepeatRepealCheckOperation(buildRepeatCheckParams(check.getPrintDatetime())).call())
                .andThen(Single.just(this))
                .subscribeOn(SchedulersCPPK.printer());
    }

    private Completable updateOdometerValue() {
        Logger.trace(TAG, "updateOdometerValue");
        return operationFactory.getGetOdometerValue().call()
                .doOnNext(result -> paperUsageCounter.setCurrentOdometerValueBeforePrinting(result.getOdometerValue()))
                .toCompletable();
    }
}
