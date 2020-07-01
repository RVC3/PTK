package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.logic.creator.EventCreator;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.printer.rx.operation.fineCheck.FineCheckTpl;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintFineCheckOperation;
import ru.ppr.cppk.printer.rx.operation.fineCheck.PrintRepeatFineCheckOperation;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Single;

/**
 * Документ "Чек взимания штрафа".
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleDocument {

    private static final String TAG = Logger.makeLogTag(FineSaleDocument.class);

    private final FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder;
    private final DocumentNumberProvider documentNumberProvider;
    private final OperationFactory operationFactory;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;
    private final ShiftManager shiftManager;
    private final Provider<EventCreator> eventCreatorProvider;
    /**
     * Данные процесса оформления штрафа
     */
    private FineSaleData fineSaleData;
    /**
     * Id связанного события взимания штрафа
     */
    private long actualFineSaleEventId;
    /**
     * Рассчитанный порядковый номер документа.
     * Вычисляется перед печатью фискального чека.
     */
    private int pdNumber;
    /**
     * Результат печати чека
     */
    private PrintFineCheckOperation.Result printResult;

    @Inject
    FineSaleDocument(FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder,
                     DocumentNumberProvider documentNumberProvider,
                     OperationFactory operationFactory,
                     LocalDaoSession localDaoSession,
                     LocalDbTransaction localDbTransaction,
                     ShiftManager shiftManager,
                     Provider<EventCreator> eventCreatorProvider) {
        this.fiscalHeaderParamsBuilder = fiscalHeaderParamsBuilder;
        this.documentNumberProvider = documentNumberProvider;
        this.operationFactory = operationFactory;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
        this.shiftManager = shiftManager;
        this.eventCreatorProvider = eventCreatorProvider;
    }

    void setFineSaleData(FineSaleData fineSaleData) {
        this.fineSaleData = fineSaleData;
    }

    private void preparePdNumber() {
        pdNumber = documentNumberProvider.getNextDocumentNumber();
        Logger.trace(TAG, "preparePdNumber, pdNumber = " + pdNumber);
    }

    /**
     * Собирает параметры для шаблона печати чека оформления штрафа
     *
     * @return Параметры
     */
    private PrintFineCheckOperation.Params buildCheckParams(int documentNumber) {
        PrintFineCheckOperation.Params params = new PrintFineCheckOperation.Params();
        params.headerParams = fiscalHeaderParamsBuilder.build();
        FineCheckTpl.Params tplParams = new FineCheckTpl.Params();
        tplParams.pdNumber = documentNumber;
        tplParams.fineName = fineSaleData.getFine().getName();
        params.fineCheckTplParams = tplParams;
        params.pdNumber = documentNumber;
        params.amount = fineSaleData.getFine().getValue();
        params.vatRate = fineSaleData.getFine().getNdsPercent();
        params.vatValue = Decimals.getVATValueIncludedFromRate(fineSaleData.getFine().getValue(), BigDecimal.valueOf(fineSaleData.getFine().getNdsPercent()), Decimals.RoundMode.HUNDREDTH);
        params.payment = fineSaleData.getFine().getValue();
        params.paymentType = fineSaleData.getPaymentType();
        params.customerPhoneNumber = fineSaleData.getETicketDataParams() == null ? null : fineSaleData.getETicketDataParams().getPhone();
        return params;
    }

    /**
     * Собирает параметры для шаблона печати дубликата чека оформления штрафа
     *
     * @return Параметры
     */
    private PrintRepeatFineCheckOperation.Params buildRepeatCheckParams(int documentNumber, Date printDateTime) {
        PrintRepeatFineCheckOperation.Params params = new PrintRepeatFineCheckOperation.Params();
        params.headerParams = fiscalHeaderParamsBuilder.build();
        FineCheckTpl.Params tplParams = new FineCheckTpl.Params();
        tplParams.pdNumber = documentNumber;
        tplParams.fineName = fineSaleData.getFine().getName();
        tplParams.printCheckDateTime = printDateTime;
        params.fineCheckTplParams = tplParams;
        return params;
    }

    /**
     * Выполняет печать чека взимания штрафа.
     */
    public Single<FineSaleDocument> print() {
        Logger.trace(TAG, "print");
        return Completable
                .fromAction(() -> {
                    FineSaleEvent fineSaleEvent = getFineSaleEvent();

                    if (fineSaleEvent.getStatus() != FineSaleEvent.Status.PRE_PRINTING) {
                        throw new IllegalStateException("Invalid status for operation = " + fineSaleEvent.getStatus());
                    }

                    preparePdNumber();
                })
                .andThen(Single.defer(() -> operationFactory.getPrintFineCheckOperation(buildCheckParams(pdNumber)).call()))
                .doOnSuccess(result -> printResult = result)
                .map(result -> this)
                .subscribeOn(SchedulersCPPK.printer());
    }

    /**
     * Выполняет печать дубликата чека взимания штрафа.
     */
    public Single<FineSaleDocument> printDuplicateReceipt() {
        Logger.trace(TAG, "printDuplicateReceipt");
        return Single
                .fromCallable(() -> {
                    FineSaleEvent fineSaleEvent = getFineSaleEvent();

                    if (fineSaleEvent.getStatus() != FineSaleEvent.Status.CHECK_PRINTED) {
                        throw new IllegalStateException("Invalid status for operation = " + fineSaleEvent.getStatus());
                    }

                    return localDaoSession.getCheckDao().load(fineSaleEvent.getCheckId());
                })
                .flatMapCompletable(check -> operationFactory.getPrintRepeatFineCheckOperation(buildRepeatCheckParams(check.getOrderNumber(), check.getPrintDatetime())).call())
                .andThen(Single.just(this))
                .subscribeOn(SchedulersCPPK.printer());
    }

    /**
     * Проверят статус текущего события печати данного документа, если это уже не первая попытка.
     */
    private void checkFineSaleEventCouldBeCreated() {
        Logger.trace(TAG, "checkFineSaleEventCouldBeCreated, actualFineSaleEventId = " + actualFineSaleEventId);
        if (actualFineSaleEventId == 0) {
            // Пока ещё не было попыток печати данного документа
            // Можно не переживать, что есть событие печати данного документа в несинхронизированном состоянии
            return;
        }

        FineSaleEvent fineSaleEvent = getFineSaleEvent();

        switch (fineSaleEvent.getStatus()) {
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
    private FineSaleEvent getFineSaleEvent() {
        if (actualFineSaleEventId == 0) {
            throw new IllegalStateException("Method should not be called before creating event");
        }
        FineSaleEvent fineSaleEvent = localDaoSession.getFineSaleEventDao().load(actualFineSaleEventId);
        if (fineSaleEvent == null) {
            throw new IllegalStateException("FineSaleEvent not found");
        }
        return fineSaleEvent;
    }

    /**
     * Создает событие оформления штрафа в БД в статусе {@link FineSaleEvent.Status#CREATED}.
     */
    public void createFineSaleEvent() {
        Logger.trace(TAG, "createFineSaleEvent");

        // Проверяем статус последнего события печати данного документа
        checkFineSaleEventCouldBeCreated();

        // Сбрасываем информацию о предыдщем событии печати данного документа, если чек не лег на ФР
        actualFineSaleEventId = 0;

        localDbTransaction.runInTx(() -> {
            ShiftEvent shiftEvent = shiftManager.getCurrentShiftEvent();

            // Пишем в БД Event
            Event event = eventCreatorProvider.get().create();
            // FineSaleEvent
            FineSaleEvent fineSaleEvent = new FineSaleEvent();
            fineSaleEvent.setAmount(fineSaleData.getFine().getValue());
            fineSaleEvent.setOperationDateTime(new Date());
            fineSaleEvent.setPaymentMethod(fineSaleData.getPaymentType());
            fineSaleEvent.setStatus(fineSaleData.getStatus());
            fineSaleEvent.setShiftEventId(shiftEvent.getId());
            fineSaleEvent.setFineCode(fineSaleData.getFine().getCode());
            fineSaleEvent.setEventId(event.getId());
            fineSaleEvent.setBankTransactionEventId(fineSaleData.getBankTransactionEventId());

            localDaoSession.getFineSaleEventDao().insertOrThrow(fineSaleEvent);

            // AuditTrailEvent
            AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                    .setType(AuditTrailEventType.FINE_SALE)
                    .setExtEventId(fineSaleEvent.getId())
                    .setOperationTime(event.getCreationTimestamp())
                    .setShiftEventId(shiftEvent.getId())
                    .setMonthEventId(shiftEvent.getMonthEventId())
                    .build();

            localDaoSession.getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

            this.actualFineSaleEventId = fineSaleEvent.getId();
        });
    }

    /**
     * Обновляет событие оформления штрафа в БД до статуса {@link FineSaleEvent.Status#PRE_PRINTING}.
     */
    public void updateStatusPrePrinting() {
        Logger.trace(TAG, "updateStatusPrePrinting");

        FineSaleEvent fineSaleEvent = getFineSaleEvent();

        if (fineSaleEvent.getStatus() != FineSaleEvent.Status.PAYED) {
            throw new IllegalStateException("Invalid status for operation = " + fineSaleEvent.getStatus());
        }

        localDbTransaction.runInTx(() -> {
            fineSaleEvent.setStatus(FineSaleEvent.Status.PRE_PRINTING);
            localDaoSession.getFineSaleEventDao().update(fineSaleEvent);
        });
    }

    /**
     * Обновляет событие оформления штрафа в БД до статуса {@link FineSaleEvent.Status#CHECK_PRINTED}.
     */
    public void updateStatusPrinted() {
        Logger.trace(TAG, "updateStatusPrinted");

        FineSaleEvent fineSaleEvent = getFineSaleEvent();

        if (fineSaleEvent.getStatus() != FineSaleEvent.Status.PRE_PRINTING) {
            throw new IllegalStateException("Invalid status for operation = " + fineSaleEvent.getStatus());
        }

        localDbTransaction.runInTx(() -> {
            // Чек
            Check check = new CheckBuilder()
                    .setDocumentNumber(pdNumber)
                    .setSnpdNumber(printResult.getSpnd())
                    .setPrintDateTime(printResult.getOperationTime())
                    .build();
            localDaoSession.getCheckDao().insertOrThrow(check);
            fineSaleEvent.setCheckId(check.getId());
            // Билетная лента
            TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();
            fineSaleEvent.setTicketTapeEventId(ticketTapeEvent.getId());
            // Статус
            fineSaleEvent.setStatus(FineSaleEvent.Status.CHECK_PRINTED);
            localDaoSession.getFineSaleEventDao().update(fineSaleEvent);
            // Банковская транзакция
            if (fineSaleEvent.getPaymentMethod() == PaymentType.INDIVIDUAL_BANK_CARD) {
                BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(fineSaleEvent.getBankTransactionEventId());
                bankTransactionEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);
                localDaoSession.getBankTransactionDao().update(bankTransactionEvent);
            }
        });
    }

    /**
     * Обновляет событие оформления штрафа в БД до статуса {@link FineSaleEvent.Status#COMPLETED}.
     */
    public void updateStatusCompleted() {
        Logger.trace(TAG, "updateStatusCompleted");

        FineSaleEvent fineSaleEvent = getFineSaleEvent();

        if (fineSaleEvent.getStatus() != FineSaleEvent.Status.CHECK_PRINTED) {
            throw new IllegalStateException("Invalid status for operation = " + fineSaleEvent.getStatus());
        }

        localDbTransaction.runInTx(() -> {
            fineSaleEvent.setStatus(FineSaleEvent.Status.COMPLETED);
            localDaoSession.getFineSaleEventDao().update(fineSaleEvent);
        });
    }

}
