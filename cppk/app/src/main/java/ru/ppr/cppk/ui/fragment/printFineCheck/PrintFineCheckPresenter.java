package ru.ppr.cppk.ui.fragment.printFineCheck;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.FineSaleDocument;
import ru.ppr.cppk.logic.FineSaleDocumentFactory;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscaldocument.FineSaleDocumentStateSyncronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.cppk.ui.activity.fineSale.FineSaleDataStorage;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Single;

/**
 * @author Aleksandr Brazhkin
 */
public class PrintFineCheckPresenter extends BaseMvpViewStatePresenter<PrintFineCheckView, PrintFineCheckViewState> {

    private static final String TAG = Logger.makeLogTag(PrintFineCheckPresenter.class);

    private InteractionListener interactionListener;

    private boolean initialized = false;
    private final FineSaleData fineSaleData;
    private final FineSaleDocumentFactory fineSaleDocumentFactory;
    private final TicketTapeChecker ticketTapeChecker;
    private final UiThread uiThread;
    private final CommonSettings commonSettings;
    private final FineSaleDocumentStateSyncronizer fineSaleDocumentStateSyncronizer;
    /////////////////////////////////
    private Timer autoCloseTimer;
    private final Object lock = new Object();
    private final FineSaleDocument fineSaleDocument;

    @Inject
    PrintFineCheckPresenter(PrintFineCheckViewState printFineCheckViewState,
                            FineSaleDataStorage fineSaleDataStorage,
                            FineSaleDocumentFactory fineSaleDocumentFactory,
                            TicketTapeChecker ticketTapeChecker,
                            UiThread uiThread,
                            CommonSettings commonSettings,
                            FineSaleDocumentStateSyncronizer fineSaleDocumentStateSyncronizer) {
        super(printFineCheckViewState);
        this.fineSaleData = fineSaleDataStorage.getFineSaleData();
        this.fineSaleDocumentFactory = fineSaleDocumentFactory;
        this.ticketTapeChecker = ticketTapeChecker;
        this.uiThread = uiThread;
        this.commonSettings = commonSettings;
        this.fineSaleDocumentStateSyncronizer = fineSaleDocumentStateSyncronizer;
        this.fineSaleDocument = fineSaleDocumentFactory.create(fineSaleData);
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        print();
    }

    /**
     * Выполянет печать нового чека
     */
    private Completable printNewCheck() {
        return Completable
                // Создаем событие в БД со статусом CREATED
                .fromAction(fineSaleDocument::createFineSaleEvent)
                // Создаем событие в БД до статуса PRE_PRINTING
                .doOnCompleted(fineSaleDocument::updateStatusPrePrinting)
                // Печатем ПД
                .andThen(fineSaleDocumentStateSyncronizer.printWithSync(fineSaleDocument))
                .toCompletable()
                // Обновляем событие в БД до статуса CHECK_PRINTED
                .doOnCompleted(fineSaleDocument::updateStatusPrinted)
                // Обновляем событие в БД до статуса COMPLETED
                .doOnCompleted(fineSaleDocument::updateStatusCompleted);
    }

    /**
     * Выполянет печать дубликата чека
     */
    private Completable printRepeatCheck() {
        Logger.trace(TAG, "printRepeatCheck");
        // Печатем дубликат ПД
        return fineSaleDocument.printDuplicateReceipt()
                // Обновляем событие в БД до статуса COMPLETED
                // Синхронизатор состояния чека устанавливает статус PRINTED
                .doOnSuccess(FineSaleDocument::updateStatusCompleted)
                .toCompletable();
    }

    /**
     * Выполняет печать чека
     */
    private void print() {
        Logger.trace(TAG, "print");
        // Проверяем наличие билетной ленты
        ticketTapeChecker.checkOrThrow()
                // Синхронизируем состояние последнего чека
                .andThen(fineSaleDocumentStateSyncronizer.syncBeforePrint())
                // Если синхронизация завершена успешно, считаем, что чека нет на ФР
                .andThen(Single.just(Boolean.FALSE))
                .onErrorResumeNext(throwable -> {
                    if (fineSaleDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
                        // Попадаем сюда, если это уже не первая попытка печати данного документа
                        // И на какой-то из предыдущих попыток он всё-таки уже попал в ФР
                        // Сообщаем далее, что чек лег на ФР
                        return Single.just(Boolean.TRUE);
                    } else {
                        // Необрабатываемая на данном шаге ошибка
                        return Single.error(throwable);
                    }
                })
                .flatMapCompletable(checkInFr -> {
                    if (checkInFr) {
                        // Если чек уже в ФР, печатаем дубликат
                        return printRepeatCheck().onErrorResumeNext(throwable -> {
                            Logger.warning(TAG, "ошибка печати дубликата чека штрафа", throwable);
                            //http://agile.srvdev.ru/browse/CPPKPP-44488
                            //сознательно идем на то что все ошибки печати дубликата чека модифицируем в PdInFrNotPrintedException,
                            //чтобы не отменять банковскую транзакцию при ошибких печати чека дубликата
                            return Completable.error(new DocumentStateSyncronizer.PdInFrNotPrintedException(throwable));
                        });
                    } else {
                        // Если чека в ФР ещё нет, печатаем новый
                        return printNewCheck();
                    }
                })
                .subscribeOn(SchedulersCPPK.printer())
                // Отображаем процесс печати чека
                .doOnSubscribe(subscription -> uiThread.post(view::showPrintingState))
                .subscribe(() -> {
                    uiThread.post(() -> view.showPrintSuccessState(fineSaleData.getPaymentType() == PaymentType.INDIVIDUAL_CASH));
                    startAutoCloseTimer();
                }, e -> {
                    Logger.error(TAG, e);
                    if (fineSaleDocumentStateSyncronizer.isInFrButNotPrinted(e)) {
                        // http://agile.srvdev.ru/browse/CPPKPP-38173
                        // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                        // Сообщаем пользователю о необходимости аннулирования ПД.

                        //http://agile.srvdev.ru/browse/CPPKPP-44488
                        //также сюда мы можем попасть при ошибке печати дубликата чека
                        //подобная ситуация также возникаем при аннулировании: PrintRepealCheckPresenter
                        Logger.info(TAG, "Команда печати завершилась с ошибкой, однако синхронизатор установил что чек лег на ФР и обновил статус для билета");
                        uiThread.post(view::showPrintingFailedAndCheckInFrState);
                    } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        interactionListener.navigateToTicketTapeIsNotSet();
                    } else if (e instanceof ShiftTimeOutException) {
                        uiThread.post(view::showShiftTimeOutError);
                    } else if (e instanceof ShiftNotOpenedException) {
                        uiThread.post(view::showIncorrectFrStateError);
                    } else if (e instanceof PrinterException) {
                        uiThread.post(view::showPrintFailState);
                    } else if (e instanceof IncorrectEKLZNumberException) {
                        uiThread.post(view::showNeedActivateEklzState);
                    } else {
                        EmergencyModeHelper.startEmergencyMode(e);
                    }
                });
    }

    void onReturnMoneyBtnClicked() {
        Logger.trace(TAG, "onReturnMoneyBtnClicked");
        view.showReturnMoneyConfirmationDialog();
    }

    void onRepeatBtnClicked() {
        Logger.trace(TAG, "onRepeatBtnClicked");
        print();
    }

    void onStartEklzActivationBtnClicked() {
        Logger.trace(TAG, "onStartEklzActivationBtnClicked");
        interactionListener.navigateToActivateEklz();
    }

    void onCancelEklzActivationBtnClicked() {
        Logger.trace(TAG, "onCancelEklzActivationBtnClicked");
        view.showReturnMoneyConfirmationDialog();
    }

    void onStartCloseShiftBtnClicked() {
        Logger.trace(TAG, "onStartCloseShiftBtnClicked");
        interactionListener.navigateToCloseShift();
    }

    void onCancelCloseShiftBtnClicked() {
        Logger.trace(TAG, "onCancelCloseShiftBtnClicked");
        view.showReturnMoneyConfirmationDialog();
    }

    void onCalculateDeliveryBtnClicked() {
        Logger.trace(TAG, "onCalculateDeliveryBtnClicked");
        interactionListener.navigateToCalculateDelivery(fineSaleData.getFine().getValue());
    }

    void onReturnMoneyConfirmClicked() {
        Logger.trace(TAG, "onReturnMoneyConfirmClicked");
        interactionListener.onReturnMoneyRequired();
    }

    void onSetTicketTapeFinished() {
        Logger.trace(TAG, "onSetTicketTapeFinished");
        uiThread.post(view::showPrintFailState);
    }

    void onCancelRePrintBtnClicked() {
        Logger.trace(TAG, "onCancelRePrintBtnClicked");
        interactionListener.onOperationCanceled();
    }

    void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        closeScreenIfAllowed();
    }

    /**
     * Стартует таймер автозакрытия экрана
     */
    private void startAutoCloseTimer() {
        Logger.trace(TAG, "startAutoCloseTimer");
        // http://agile.srvdev.ru/browse/CPPKPP-27923
        // http://agile.srvdev.ru/browse/CPPKPP-28025
        autoCloseTimer = new Timer();
        autoCloseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                closeScreenIfAllowed();
            }
        }, 1000 * commonSettings.getAutoCloseTime());
    }

    /**
     * Останавливает таймер автозакрытия экрана
     */
    private void closeScreenIfAllowed() {
        Logger.trace(TAG, "closeScreenIfAllowed");
        synchronized (lock) {
            if (autoCloseTimer != null) {
                autoCloseTimer.cancel();
                autoCloseTimer = null;
                uiThread.post(() -> interactionListener.closeScreen());
            }
        }
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToTicketTapeIsNotSet();

        void navigateToActivateEklz();

        void navigateToCloseShift();

        void onReturnMoneyRequired();

        void navigateToCalculateDelivery(BigDecimal amount);

        void closeScreen();

        void onOperationCanceled();
    }

}
