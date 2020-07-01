package ru.ppr.cppk.ui.activity.closeshift.interactor;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.logic.PdRepealDocument;
import ru.ppr.cppk.logic.PdRepealDocumentFactory;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.PdRepealDocumentStateSyncronizer;
import ru.ppr.cppk.logic.pdRepeal.PdRepealData;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.cppk.utils.SlipConverter;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Observable;

/**
 * Класс, осуществляющий заверешение процесса аннулирования для недоаннулированных ПД.
 * Т.е. событий, для которых была отменена банковская транзакция, но по фискальнику аннулирование не было выполнено.
 *
 * @author Aleksandr Brazhkin
 */
public class CompletePdRepealEventInteractor {

    private static final String TAG = Logger.makeLogTag(CompletePdRepealEventInteractor.class);

    private final LocalDaoSession localDaoSession;
    private final FiscalDocStateSynchronizer fiscalDocStateSynchronizer;
    private final PdRepealDocumentFactory pdRepealDocumentFactory;
    private final Provider<PdRepealDocumentStateSyncronizer> pdRepealDocumentStateSyncronizerProvider;
    private final ShiftManager shiftManager;

    @Inject
    public CompletePdRepealEventInteractor(LocalDaoSession localDaoSession,
                                           FiscalDocStateSynchronizer fiscalDocStateSynchronizer,
                                           PdRepealDocumentFactory pdRepealDocumentFactory,
                                           Provider<PdRepealDocumentStateSyncronizer> pdRepealDocumentStateSyncronizerProvider,
                                           ShiftManager shiftManager) {
        this.localDaoSession = localDaoSession;
        this.fiscalDocStateSynchronizer = fiscalDocStateSynchronizer;
        this.pdRepealDocumentFactory = pdRepealDocumentFactory;
        this.pdRepealDocumentStateSyncronizerProvider = pdRepealDocumentStateSyncronizerProvider;
        this.shiftManager = shiftManager;
    }

    /**
     * Заверешает процесс аннулирования для недоаннулированных ПД.
     */
    public Completable completeEvents() {
        // Синхронизируем состояния чеков
        // Не надеемся, что кто-то обеспечит синхронизированное состояние до вызова данного метода. Так проще и безопаснее.
        // Сейчас при закрытии смены до вызова данного метода тоже осуществляется синхронизация состояния
        // Получается, что происходит 2 вызова. Пока оставляем так, вряд ли это создаст проблемы.
        return fiscalDocStateSynchronizer.rxSyncCheckState()
                .flatMapObservable(syncStateResult -> {
                    if (shiftManager.isShiftClosed()) {
                        throw new IllegalStateException("Operation could not be executed out of shift");
                    }
                    return Observable.from(localDaoSession.getCppkTicketReturnDao().getUncompletedEventsForShift(shiftManager.getCurrentShiftId()));
                })
                .flatMap(pdRepealEvent -> Observable.fromCallable(() -> {
                    ProgressStatus status = pdRepealEvent.getProgressStatus();
                    Logger.trace(TAG, "ReturnEventId = " + pdRepealEvent.getId() + ", status = " + status);
                    if (status == ProgressStatus.PrePrinting || status == ProgressStatus.CheckPrinted || status == ProgressStatus.Completed) {
                        // Событие не может быть в статусе PrePrinting. Нужно выполнить предварительную синхронизацию, иначе метод отработает некорректно
                        // Событие не может быть в статусе CheckPrinted и Completed, потому что для них уже распечатаны фискальные чеки
                        throw new IllegalStateException("Invalid event status = " + status);
                    }
                    PdRepealData pdRepealData = new PdRepealData();
                    // Получаем информацию о событии продажи ПД
                    Logger.trace(TAG, "SaleEventId = " + pdRepealEvent.getPdSaleEventId());
                    CPPKTicketSales pdSaleEvent = localDaoSession.getCppkTicketSaleDao().load(pdRepealEvent.getPdSaleEventId());
                    if (pdSaleEvent == null) {
                        throw new IllegalStateException("PdSaleEvent should not be null");
                    }
                    pdRepealData.setPdSaleEvent(pdSaleEvent);
                    pdRepealData.setRepealReason(pdRepealEvent.getRecallReason());
                    // Получаем информацию о банковской транзакции для события продажи ПД
                    TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
                    BankTransactionEvent saleBankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
                    if (saleBankTransactionEvent == null) {
                        throw new IllegalStateException("SaleBankTransactionEvent should not be null");
                    }
                    BankTransactionEvent returnBankTransactionEvent = localDaoSession.getBankTransactionDao()
                            .getEventByParams(saleBankTransactionEvent.getTransactionId(), BankOperationType.CANCELLATION, saleBankTransactionEvent.getTerminalDayId());
                    if (returnBankTransactionEvent == null) {
                        throw new IllegalStateException("ReturnBankTransactionEvent should not be null");
                    }
                    Logger.trace(TAG, "ReturnBankTransactionEventId = " + returnBankTransactionEvent.getId());
                    pdRepealData.setBankTransactionEventId(returnBankTransactionEvent.getId());
                    if (pdRepealEvent.getProgressStatus() == ProgressStatus.CREATED) {
                        // Нам в токой ситуации негде взять слип, считаем, что он пустой
                        pdRepealData.setSlipReceipt(Collections.emptyList());
                    } else {
                        pdRepealData.setSlipReceipt(SlipConverter.fromImage(pdRepealEvent.getReturnBankTerminalSlip()));
                    }

                    PdRepealDocument pdRepealDocument = pdRepealDocumentFactory.create(pdRepealData);
                    pdRepealDocument.setActualPdRepealEventId(pdRepealEvent.getId());

                    if (status == ProgressStatus.CREATED || status == ProgressStatus.Broken) {
                        // Возвращаем событие в статус PrePrinting перед печатью
                        pdRepealDocument.updateStatusPrePrinting();
                    }

                    return pdRepealDocument;
                }))
                .flatMap(pdRepealDocument -> {
                    PdRepealDocumentStateSyncronizer pdRepealDocumentStateSyncronizer = pdRepealDocumentStateSyncronizerProvider.get();
                    return pdRepealDocumentStateSyncronizer
                            .printWithSync(pdRepealDocument)
                            .toObservable()
                            .onErrorResumeNext(throwable -> {
                                if (pdRepealDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
                                    Logger.trace(TAG, "Check is in fr, but not printed");
                                    // Если при печати произошла ошибка, но чек всё-таки лег на ФР, поглощаем ошибку
                                    return Observable.just(pdRepealDocument);
                                } else {
                                    return Observable.error(throwable);
                                }
                            })
                            // Обновляем событие в БД до статуса CHECK_PRINTED
                            .doOnNext(PdRepealDocument::updateStatusPrinted)
                            // Обновляем событие в БД до статуса COMPLETED
                            .doOnNext(PdRepealDocument::updateStatusCompleted);
                })
                .onErrorResumeNext(throwable -> {
                    //если пришла такая ошибка, то прошло более 24 часов на принтере
                    //с момента открытия смены и он ничего больше не даст сделать, кроме как напечатать
                    //z-report, в соответствии с https://aj.srvdev.ru/browse/CPPKPP-20961
                    //ошибкой не считается и должно продолжить нормально работать
                    //в этом моменте цикл отмен прервётся, но не методом onError, а onComplete
                    if (throwable instanceof ShiftTimeOutException) {
                        Logger.trace(TAG, "Consuming ShiftTimeOutException");
                        return Observable.empty();
                    }
                    //смена не открыта, значит мы ничего не можем сделать, кроме как закрыть смену, поэтому вываливаемся из цикла методом onComplete
                    else if (throwable instanceof ShiftNotOpenedException) {
                        Logger.trace(TAG, "Consuming ShiftNotOpenedException");
                        return Observable.empty();
                    } else {
                        //всё остальное = ошибка пусть туда и валится
                        return Observable.error(throwable);
                    }
                })
                .doOnSubscribe(() -> Logger.trace(TAG, "Operation started"))
                .doOnCompleted(() -> Logger.trace(TAG, "Operation completed"))
                .doOnError(throwable -> Logger.error(TAG, "Operation failed", throwable))
                .toCompletable();
    }
}
