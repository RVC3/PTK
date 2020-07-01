package ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.PdRepealDocument;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscaldocument.PdRepealDocumentStateSyncronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Single;

/**
 * @author Aleksandr Brazhkin
 */
public class PrintRepealCheckPresenter extends BaseMvpViewStatePresenter<PrintRepealCheckView, PrintRepealCheckViewState> {

    private static final String TAG = Logger.makeLogTag(PrintRepealCheckPresenter.class);

    private InteractionListener interactionListener;

    private boolean initialized = false;
    private final TicketTapeChecker ticketTapeChecker;
    private final UiThread uiThread;
    private final LocalDaoSession localDaoSession;
    private final PdRepealDocumentStateSyncronizer pdRepealDocumentStateSyncronizer;
    /////////////////////////////////
    private final PrintRepealCheckSharedModel printRepealCheckSharedModel;
    private final PdRepealDocument pdRepealDocument;

    @Inject
    PrintRepealCheckPresenter(PrintRepealCheckViewState printFineCheckViewState,
                              PrintRepealCheckSharedModel printRepealCheckSharedModel,
                              TicketTapeChecker ticketTapeChecker,
                              UiThread uiThread,
                              LocalDaoSession localDaoSession,
                              PdRepealDocumentStateSyncronizer pdRepealDocumentStateSyncronizer) {
        super(printFineCheckViewState);
        this.printRepealCheckSharedModel = printRepealCheckSharedModel;
        this.ticketTapeChecker = ticketTapeChecker;
        this.uiThread = uiThread;
        this.localDaoSession = localDaoSession;
        this.pdRepealDocumentStateSyncronizer = pdRepealDocumentStateSyncronizer;
        this.pdRepealDocument = printRepealCheckSharedModel.getPdRepealDocument();
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
        // Сообщаем синхронизатру, возможно ли, что чек аннулирования уже в ФР (с прощлой попытки)
        pdRepealDocumentStateSyncronizer.setPreviousPrintingSyncRequired(printRepealCheckSharedModel.getPdRepealData().isPreviousAttemptCouldBeInFr());
        print();
    }

    /**
     * Возвращает текущий статус связанного с документом события аннулирования ПД. {@code null} если события ещё не создано.
     */
    @Nullable
    private ProgressStatus getActualPdRepealEventStatus() {
        long eventId = pdRepealDocument.getActualPdRepealEventId();
        if (eventId == 0) {
            return null;
        }
        CPPKTicketReturn pdRepealEvent = localDaoSession.getCppkTicketReturnDao().load(eventId);
        if (pdRepealEvent == null) {
            throw new IllegalStateException("PdRepealEvent not found");
        }
        return pdRepealEvent.getProgressStatus();
    }

    /**
     * Выполянет печать нового чека
     */
    private Completable printNewCheck() {
        return Completable
                .fromAction(() -> {
                    // Получаем текущий статус документа
                    ProgressStatus status = getActualPdRepealEventStatus();
                    // Описанные ниже утверждения предполагают нормальный сценарий работы
                    // Не исключено, что события останутся в более низких статусах (при выключении питания, например)
                    // Но, на корректность данных это не влияет
                    if (status == null) {
                        Logger.trace(TAG, "printNewCheck, creating new event");
                        // Сюда попадаем при аннулировании ПД за наличные
                        // или при аннулировании по банку, но только если банковская транзакция была отменена когда-то ранее
                        // Создаем событие в БД со статусом CREATED
                        pdRepealDocument.createPdRepealEvent();
                        // Обновляем событие в БД до статуса PRE_PRINTING
                        pdRepealDocument.updateStatusPrePrinting();
                    } else if (status == ProgressStatus.CREATED) {
                        // Сюда попадаем при аннулировании ПД по банку
                        // Событие создается в статусе CREATED после отмены транзакции
                        Logger.trace(TAG, "printNewCheck, event already exists, status = " + status);
                        // Обновляем событие в БД до статуса PRE_PRINTING
                        pdRepealDocument.updateStatusPrePrinting();
                    } else if (status == ProgressStatus.PrePrinting) {
                        // Событие могло остаться в этом статусе после прошлой попытки аннулирования,
                        // в ходе которой фискальный чек остался в неизвестном состоянии
                        Logger.trace(TAG, "printNewCheck, event already exists, status = " + status);
                    } else if (status == ProgressStatus.Broken) {
                        // Уже была попытка аннулирования, но чек распечатать на удалось
                        Logger.trace(TAG, "printNewCheck, event already exists, status = " + status);
                        // Возвращаем событие в статус PrePrinting перед печатью
                        pdRepealDocument.updateStatusPrePrinting();
                    } else {
                        throw new IllegalStateException("Invalid status for operation = " + status);
                    }
                })// Печатем ПД
                .andThen(pdRepealDocumentStateSyncronizer.printWithSync(pdRepealDocument))
                .toCompletable()
                // Обновляем событие в БД до статуса CHECK_PRINTED
                .doOnCompleted(pdRepealDocument::updateStatusPrinted)
                // Обновляем событие в БД до статуса COMPLETED
                .doOnCompleted(pdRepealDocument::updateStatusCompleted);
    }

    /**
     * Выполянет печать дубликата чека
     */
    private Completable printRepeatCheck() {
        Logger.trace(TAG, "printRepeatCheck");
        // Печатем дубликат ПД
        return pdRepealDocument.printDuplicateReceipt()
                // Обновляем событие в БД до статуса COMPLETED
                // Синхронизатор состояния чека устанавливает статус PRINTED
                .doOnSuccess(PdRepealDocument::updateStatusCompleted)
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
                .andThen(pdRepealDocumentStateSyncronizer.syncBeforePrint())
                // Если синхронизация завершена успешно, считаем, что чека нет на ФР
                .andThen(Single.just(Boolean.FALSE))
                .onErrorResumeNext(throwable -> {
                    if (pdRepealDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
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
                            Logger.warning(TAG, "ошибка печати дубликата чека аннулирования", throwable);
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
                .subscribe(() -> uiThread.post(view::showPrintSuccessState), e -> {
                    Logger.error(TAG, e);
                    if (pdRepealDocumentStateSyncronizer.isInFrButNotPrinted(e)) {
                        // http://agile.srvdev.ru/browse/CPPKPP-38173
                        // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                        // Сообщаем пользователю о необходимости аннулирования ПД.

                        //http://agile.srvdev.ru/browse/CPPKPP-44488
                        //также сюда мы можем попасть при ошибке печати дубликата чека
                        //подобная ситуация также возникаем при продаже штрафа: PrintFineCheckPresenter
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
        printRepealCheckSharedModel.onOperationCanceled();
    }

    void onStartCloseShiftBtnClicked() {
        Logger.trace(TAG, "onStartCloseShiftBtnClicked");
        interactionListener.navigateToCloseShift();
    }

    void onCancelCloseShiftBtnClicked() {
        Logger.trace(TAG, "onCancelCloseShiftBtnClicked");
        printRepealCheckSharedModel.onOperationCanceled();
    }

    void onSuccessOkBtnClicked() {
        printRepealCheckSharedModel.onOperationCompleted();
    }

    void onSetTicketTapeFinished() {
        Logger.trace(TAG, "onSetTicketTapeFinished");
        uiThread.post(view::showPrintFailState);
    }

    void onCancelRePrintBtnClicked() {
        Logger.trace(TAG, "onCancelRePrintBtnClicked");
        printRepealCheckSharedModel.onOperationCanceled();
    }

    void onFailCancelBtnClicked() {
        Logger.trace(TAG, "onFailCancelBtnClicked");
        printRepealCheckSharedModel.onOperationCanceled();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToTicketTapeIsNotSet();

        void navigateToActivateEklz();

        void navigateToCloseShift();
    }

}
