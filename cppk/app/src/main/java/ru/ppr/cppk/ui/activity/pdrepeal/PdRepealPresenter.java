package ru.ppr.cppk.ui.activity.pdrepeal;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.BankOperationResult;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.logic.PdRepealDocument;
import ru.ppr.cppk.logic.PdRepealDocumentFactory;
import ru.ppr.cppk.logic.pdRepeal.PdRepealData;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancel.PosCancelSharedModel;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip.PosCancelPrintSlipSharedModel;
import ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck.PrintRepealCheckSharedModel;
import ru.ppr.cppk.utils.SlipConverter;
import ru.ppr.logger.Logger;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Aleksandr Brazhkin
 */
public class PdRepealPresenter extends BaseMvpViewStatePresenter<PdRepealView, PdRepealViewState> {

    private static final String TAG = Logger.makeLogTag(PdRepealPresenter.class);

    private Navigator navigator;

    private boolean initialized = false;
    private final PdRepealParams pdRepealParams;
    private final PrintRepealCheckSharedModel printRepealCheckSharedModel;
    private final PdRepealData pdRepealData = new PdRepealData();
    private final LocalDaoSession localDaoSession;
    private final PosCancelSharedModel posCancelSharedModel;
    private final PdRepealDocumentFactory pdRepealDocumentFactory;
    private final PosCancelPrintSlipSharedModel posCancelPrintSlipSharedModel;
    ///////
    private Subscription initSubscription = Subscriptions.unsubscribed();

    @Inject
    PdRepealPresenter(PdRepealViewState pdRepealViewState,
                      @Named("pdRepealParams") PdRepealParams pdRepealParams,
                      LocalDaoSession localDaoSession,
                      PdRepealDocumentFactory pdRepealDocumentFactory,
                      PosCancelSharedModel posCancelSharedModel,
                      PrintRepealCheckSharedModel printRepealCheckSharedModel,
                      PosCancelPrintSlipSharedModel posCancelPrintSlipSharedModel) {
        super(pdRepealViewState);
        this.pdRepealParams = pdRepealParams;
        this.printRepealCheckSharedModel = printRepealCheckSharedModel;
        this.localDaoSession = localDaoSession;
        this.posCancelSharedModel = posCancelSharedModel;
        this.pdRepealDocumentFactory = pdRepealDocumentFactory;
        this.posCancelPrintSlipSharedModel = posCancelPrintSlipSharedModel;
    }

    void bindNavigator(@NonNull final Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");

//        initSubscription = Completable.fromAction(() -> {
//
//        })
//                .subscribeOn(SchedulersCPPK.background())
//                .doOnSubscribe(subscription -> view.setInitializingStateVisible(true))
//                .subscribe();


        // Ищем событие продажи ПД
        CPPKTicketSales pdSaleEvent = localDaoSession.getCppkTicketSaleDao().load(pdRepealParams.getPdSaleEventId());
        if (pdSaleEvent == null) {
            throw new IllegalArgumentException("PdSaleEvent not found, id = " + pdRepealParams.getPdSaleEventId());
        }

        // Формируем общие данные
        PdRepealDocument pdRepealDocument = pdRepealDocumentFactory.create(pdRepealData);
        printRepealCheckSharedModel.setPdRepealData(pdRepealData);
        printRepealCheckSharedModel.setPdRepealDocument(pdRepealDocument);
        printRepealCheckSharedModel.setCallback(printRepealCheckCallback);
        posCancelPrintSlipSharedModel.setCallback(printSlipCallback);

        posCancelSharedModel.setCallback(posCancelCallback);

        pdRepealData.setPdSaleEvent(pdSaleEvent);
        pdRepealData.setRepealReason(pdRepealParams.getRepealReason());

        // Ищем существующее событие аннулирования ПД
        CPPKTicketReturn pdRepealEvent = localDaoSession.getCppkTicketReturnDao().findLastPdRepealEventForPdSaleEvent(pdSaleEvent.getId(), EnumSet.allOf(ProgressStatus.class));
        if (pdRepealEvent != null) {
            ProgressStatus status = pdRepealEvent.getProgressStatus();
            Logger.trace(TAG, "Event already exists with status = " + status);
            if (status == ProgressStatus.CheckPrinted || status == ProgressStatus.Completed) {
                // Если ПД уже аннулирован, и был напечатан чек, мы не должны оказаться на данном экране
                throw new IllegalArgumentException("Incorrect status = " + status);
            } else {
                // Уже была попытка аннулирования
                // Связываем документ с уже существующим событием аннулирования ПД
                Logger.trace(TAG, "Binding to existing event, id = " + pdRepealEvent.getId());
                pdRepealDocument.setActualPdRepealEventId(pdRepealEvent.getId());
                if (status == ProgressStatus.PrePrinting) {
                    // Запоминаем тот факт, что чек аннулирования данного ПД уже может быть в ФР
                    // Флаг нужен синхронизатору состояния чека на экране печати чека
                    pdRepealData.setPreviousAttemptCouldBeInFr(true);
                }
            }
        }

        // Получаем информацию о банковской транзакции для события продажи ПД
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
        BankTransactionEvent saleBankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        if (saleBankTransactionEvent != null) {
            // ПД был продан по банку
            BankTransactionEvent returnBankTransactionEvent = localDaoSession.getBankTransactionDao()
                    .getEventByParams(saleBankTransactionEvent.getTransactionId(), BankOperationType.CANCELLATION, saleBankTransactionEvent.getTerminalDayId());
            if (returnBankTransactionEvent != null
                    && returnBankTransactionEvent.getStatus() != BankTransactionEvent.Status.STARTED
                    && returnBankTransactionEvent.getOperationResult() == BankOperationResult.Approved) {
                Logger.trace(TAG, "Bank transaction is already cancelled, id = " + returnBankTransactionEvent.getId() + ". Skipping pos operations");
                // Банковская транзакция уже отменена
                // Считаем, что возврат денег выполнен
                pdRepealData.setBankTransactionEventId(returnBankTransactionEvent.getId());
                if (pdRepealEvent == null || pdRepealEvent.getProgressStatus() == ProgressStatus.CREATED) {
                    // Нам в токой ситуации негде взять слип, считаем, что он пустой
                    pdRepealData.setSlipReceipt(Collections.emptyList());
                } else {
                    pdRepealData.setSlipReceipt(SlipConverter.fromImage(pdRepealEvent.getReturnBankTerminalSlip()));
                }
                // Печатаем чек аннулирования
                navigator.navigateToPrintRepealCheck();
            } else {
                Logger.trace(TAG, "Starting with cancelling bank transaction, id = " + saleBankTransactionEvent.getId());
                // Создаем событие в БД
                printRepealCheckSharedModel.getPdRepealDocument().createPdRepealEvent();
                // Аннулируем транзакцию
                navigator.navigateToCancelCardPayment(saleBankTransactionEvent.getId());
            }
        } else {
            Logger.trace(TAG, "Starting for cash payment");
            // ПД был продан за наличные, считаем, что возврат денег выполнен
            onCashPaymentCanceled();
        }

    }

    private void onCashPaymentCanceled() {
        Logger.trace(TAG, "onCashPaymentCanceled");
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdRepealData.getPdSaleEvent().getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        if (smartCard == null) {
            // ПД был распечатан на бумаге, переходим к печати чека аннулирования
            navigator.navigateToPrintRepealCheck();
        } else {
            // ПД был записан на карту, запускаем экран удаления ПД с карты
            navigator.navigateToDeletePdFromCard(smartCard.getCrystalSerialNumber(), smartCard.getTrack());
        }
    }

    private void onBankTransactionCanceled() {
        Logger.trace(TAG, "onBankTransactionCanceled");
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdRepealData.getPdSaleEvent().getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        if (smartCard == null) {
            // ПД был распечатан на бумаге, переходим к печати банковского слипа
            navigateToPrintSlip();
        } else {
            // ПД был записан на карту, запускаем экран удаления ПД с карты
            navigator.navigateToDeletePdFromCard(smartCard.getCrystalSerialNumber(), smartCard.getTrack());
        }
    }

    void onDeletingFromCardFinished(boolean deleted) {
        Logger.trace(TAG, "onDeletingFromCardFinished, deleted = " + deleted);
        if (pdRepealData.getBankTransactionEventId() == -1) {
            // ПД был продан за наличные
            if (deleted) {
                // Если удаление ПД с карты завершилось успешно
                // Печатаем чек аннулирования
                navigator.navigateToPrintRepealCheck();
            } else {
                // Ошибка при удалении ПД с карты, закрываем экран
                Logger.trace(TAG, "Pd was not deleted from card. Cancelling");
                navigator.closeScreen();
            }
        } else {
            // Как я понял, нам не особо важно, удалось стереть с карты или нет, идём дальше в любом случае
            // Печатаем банковский слип
            navigateToPrintSlip();
        }
    }

    private void navigateToPrintSlip() {
        if (pdRepealData.getSlipReceipt() == null || pdRepealData.getSlipReceipt().isEmpty()) {
            // Если печатать нечего - пропускаем шаг
            navigator.navigateToPrintRepealCheck();
        } else {
            posCancelPrintSlipSharedModel.setSlipReceipt(pdRepealData.getSlipReceipt());
            navigator.navigateToPrintSlip();
        }
    }

    void onAbortedOkBtnClicked() {
        Logger.trace(TAG, "onAbortedOkBtnClicked");
        navigator.closeScreen();
    }

    @Override
    public void destroy() {
        super.destroy();
        initSubscription.unsubscribe();
    }

    private final PosCancelSharedModel.Callback posCancelCallback = new PosCancelSharedModel.Callback() {

        @Override
        public void onOperationCompleted(long bankTransactionEventId, List<String> receipt, String message) {
            Logger.trace(TAG, "posCancelCallback.onOperationCompleted");
            // Отмена банковской транзакции прошла успешно
            pdRepealData.setBankTransactionEventId(bankTransactionEventId);
            pdRepealData.setSlipReceipt(receipt);
            // Обновляем статус, чтобы сохранить информацию о транзакции
            printRepealCheckSharedModel.getPdRepealDocument().updateStatusPrePrinting();
            // Переходим к следующему шагу после возврата денег
            onBankTransactionCanceled();
        }

        @Override
        public void onOperationFailed(long bankTransactionEventId, List<String> receipt, String message) {
            Logger.trace(TAG, "posCancelCallback.onOperationFailed");
            // Не удалось отменить транзакцию по банку, отображаем экран с сообщением о прерванности операции
            view.showAbortedState(message);
        }

        @Override
        public void onOperationCanceled() {
            Logger.trace(TAG, "posCancelCallback.onOperationCanceled");
            // Не удалось отменить транзакцию по банку, отображаем экран с сообщением о прерванности операции
            view.showAbortedState(null);
        }
    };

    private final PosCancelPrintSlipSharedModel.Callback printSlipCallback = new PosCancelPrintSlipSharedModel.Callback() {

        @Override
        public void onOperationCompleted() {
            Logger.trace(TAG, "printSlipCallback.onOperationCompleted");
            navigator.navigateToPrintRepealCheck();
        }

        @Override
        public void onOperationFailed() {
            Logger.trace(TAG, "printSlipCallback.onOperationFailed");
            view.showAbortedState(null);
        }

    };

    private final PrintRepealCheckSharedModel.Callback printRepealCheckCallback = new PrintRepealCheckSharedModel.Callback() {
        @Override
        public void onOperationCanceled() {
            Logger.trace(TAG, "printRepealCheckCallback.onOperationCanceled");
            navigator.closeScreen();
        }

        @Override
        public void onOperationCompleted() {
            Logger.trace(TAG, "printRepealCheckCallback.onOperationCompleted");
            navigator.closeScreen();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface Navigator {

        void navigateToPrintRepealCheck();

        void navigateToPrintSlip();

        void navigateToCancelCardPayment(long bankTransactionEventId);

        void navigateToDeletePdFromCard(String cardUid, int pdPosition);

        void closeScreen();
    }
}
