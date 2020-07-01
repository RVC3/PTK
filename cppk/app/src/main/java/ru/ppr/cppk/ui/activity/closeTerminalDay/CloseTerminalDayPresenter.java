package ru.ppr.cppk.ui.activity.closeTerminalDay;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TerminalDayDao;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Observable;
import rx.Subscriber;

/**
 * @author Aleksandr Brazhkin
 */
public class CloseTerminalDayPresenter extends BaseMvpViewStatePresenter<CloseTerminalDayView, CloseTerminalDayViewState> {

    private static final String TAG = Logger.makeLogTag(CloseTerminalDayPresenter.class);

    private static final int STEP_ID_ASK_INIT_CLOSE_DAY = 10001;
    private static final int STEP_ID_PREPARE_DATA = 10002;
    private static final int STEP_ID_CLOSE_DAY = 10003;
    private static final int STEP_ID_PRINT_SLIP = 10004;
    private static final int STEP_ID_SUCCESS_DIALOG = 10005;

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private final LinkedList<Integer> stepsStack = new LinkedList<>();
    private final TempData tempData = new TempData();
    //////////////
    private LocalDaoSession mLocalDaoSession;
    /**
     * UI-поток
     */
    private UiThread mUiThread;
    private PosManager mPosManager;
    private PrinterManager mPrinterManager;
    private TicketTapeChecker mTicketTapeChecker;

    public CloseTerminalDayPresenter() {

    }

    @Override
    protected CloseTerminalDayViewState provideViewState() {
        return new CloseTerminalDayViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize(LocalDaoSession localDaoSession, UiThread uiThread, PosManager posManager, PrinterManager printerManager, TicketTapeChecker ticketTapeChecker) {
        if (!mInitialized) {
            mInitialized = true;
            mUiThread = uiThread;
            mLocalDaoSession = localDaoSession;
            mPosManager = posManager;
            mPrinterManager = printerManager;
            mTicketTapeChecker = ticketTapeChecker;

            stepsStack.add(STEP_ID_ASK_INIT_CLOSE_DAY);
            stepsStack.add(STEP_ID_PREPARE_DATA);
            stepsStack.add(STEP_ID_CLOSE_DAY);
            stepsStack.add(STEP_ID_PRINT_SLIP);
            stepsStack.add(STEP_ID_SUCCESS_DIALOG);

            runOperation();
        }
    }

    private void runOperation() {

        view.hideAnyError();

        int currentStep = stepsStack.peek();

        switch (currentStep) {
            case STEP_ID_ASK_INIT_CLOSE_DAY: {
                view.showInitDayEndAskDialog();
                break;
            }
            case STEP_ID_PREPARE_DATA: {
                prepareData();
                break;
            }
            case STEP_ID_CLOSE_DAY: {
                endDay();
                break;
            }
            case STEP_ID_PRINT_SLIP: {
                printSlip();
                break;
            }
            case STEP_ID_SUCCESS_DIALOG: {
                view.showCloseDaySuccessDialog(tempData.dayNumber);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown step");
            }
        }
    }

    private void completeCurrentStepAndContinue() {
        stepsStack.pop();
        runOperation();
    }

    private void completeOperation() {
        interactionListener.navigateToPreviousScreen();
    }

    private void cancelOperation() {
        interactionListener.navigateToPreviousScreen();
    }

    void onInitDayEndApproved() {
        view.hideInitDayEndAskDialog();
        completeCurrentStepAndContinue();
    }

    void onInitDayEndCancelled() {
        cancelOperation();
    }

    void onOperationSuccessApproved() {
        completeOperation();
    }

    void onPreparingDataRepeatBtnClick() {
        runOperation();
    }

    void onPreparingDataCancelBtnClick() {
        cancelOperation();
    }

    void onClosingDayRepeatBtnClick() {
        runOperation();
    }

    void onClosingDayCancelBtnClick() {
        cancelOperation();
    }

    void onPrintingSlipRepeatBtnClick() {
        runOperation();
    }

    void onPrintingSlipCancelBtnClick() {
        completeCurrentStepAndContinue();
    }

    private void prepareData() {
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showPreparingDataProgress());
                    TerminalDayDao terminalDayDao = mLocalDaoSession.getTerminalDayDao();
                    TerminalDay lastTerminalDay = terminalDayDao.getLastTerminalDay();
                    if (lastTerminalDay == null || lastTerminalDay.getEndDateTime() != null) {
                        mUiThread.post(() -> {
                            view.hideAnyProgress();
                            view.showPreparingDataError();
                        });
                    } else {
                        tempData.dayNumber = lastTerminalDay.getTerminalDayId();
                        completeCurrentStepAndContinue();
                    }
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void endDay() {
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showClosingDayProgress());
                    mPosManager.dayEnd(new PosManager.AbstractTransactionListener() {
                        @Override
                        public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                            TransactionResult result = operationResult.getTransactionResult();
                            if (result != null && result.isApproved() &&
                                    result.getReceipt() != null && !result.getReceipt().isEmpty()) {
                                tempData.dayEndReceipt = result.getReceipt();
                                completeCurrentStepAndContinue();
                                return;
                            }
                            mUiThread.post(() -> {
                                view.hideAnyProgress();
                                view.showClosingDayError(result == null ? null : result.getBankResponse());
                            });
                        }
                    });
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void printSlip() {
        mTicketTapeChecker.checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = tempData.dayEndReceipt;

                    return params;
                }))
                .flatMap(params -> mPrinterManager.getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call()
                )
                .subscribeOn(SchedulersCPPK.printer())
                .doOnSubscribe(() -> mUiThread.post(() -> view.showPrintingSlipProgress()))
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   Logger.trace(TAG, "printSlip onCompleted");
                                   completeCurrentStepAndContinue();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Logger.trace(TAG, "printSlip onError", e);
                                   mUiThread.post(() -> {
                                       view.hideAnyProgress();
                                       view.showPrintingSlipError();
                                   });
                               }

                               @Override
                               public void onNext(Object o) {

                               }
                           }
                );
    }

    private class TempData {
        List<String> dayEndReceipt;
        long dayNumber;
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void navigateToPreviousScreen();
    }
}
