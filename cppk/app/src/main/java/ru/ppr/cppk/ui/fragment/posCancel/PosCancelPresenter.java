package ru.ppr.cppk.ui.fragment.posCancel;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Aleksandr Brazhkin
 */
public class PosCancelPresenter extends BaseMvpViewStatePresenter<PosCancelView, PosCancelViewState> {

    private static final String TAG = Logger.makeLogTag(PosCancelPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private long mBankTransactionEventId;
    private UiThread mUiThread;
    private PosManager mPosManager;
    private LocalDaoSession mLocalDaoSession;
    private PrinterManager mPrinterManager;
    private TicketTapeChecker mTicketTapeChecker;
    ///////////////////////////////////////////////////
    private List<String> mReceipt;
    private String mBankResponse;

    public PosCancelPresenter() {

    }

    @Override
    protected PosCancelViewState provideViewState() {
        return new PosCancelViewState();
    }


    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize(long bankTransactionEventId, UiThread uiThread, PosManager posManager, LocalDaoSession localDaoSession, PrinterManager printerManager, TicketTapeChecker ticketTapeChecker) {
        if (!mInitialized) {
            mInitialized = true;
            mBankTransactionEventId = bankTransactionEventId;
            mUiThread = uiThread;
            mPosManager = posManager;
            mLocalDaoSession = localDaoSession;
            mPrinterManager = printerManager;
            mTicketTapeChecker = ticketTapeChecker;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        mUiThread.post(() -> view.showConnectingState(mPosManager.getConnectionTimeout()));
        mPosManager.cancelTransaction(mBankTransactionEventId, new PosManager.AbstractFinancialTransactionListener() {

            @Override
            public void onTickBeforeTimeout(long value) {
                mUiThread.post(() -> view.showConnectingState(value));
            }

            @Override
            public void onConnectionTimeout() {
                Logger.info(TAG, "onConnectionTimeout");
                mUiThread.post(() -> mUiThread.post(() -> view.showConnectionTimeoutState()));
            }

            @Override
            public void onConnected() {
                Logger.info(TAG, "onConnected");
                mUiThread.post(() -> mUiThread.post(() -> mUiThread.post(() -> view.showConnectedState())));
            }

            @Override
            public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                FinancialTransactionResult result = operationResult.getTransactionResult();
                Logger.info(TAG, "onResult, result  = " + result);
                mUiThread.post(() -> {
                    mReceipt = result == null ? null : result.getReceipt();
                    mBankResponse = result == null ? null : result.getBankResponse();
                    if (result != null && result.isApproved()) {
                        printSlipCancelFirst(mReceipt, false);
                    } else {
                        onOperationRejected(mReceipt, mBankResponse);
                    }
                });
            }
        });
    }

    void onNotConnectedCancelOperationClicked() {
        interactionListener.onOperationFinished();
    }

    void onRepeatPrintFirstSlipClicked() {
        printSlipCancelFirst(mReceipt, true);
    }

    void onAfterRejectSlipContinueBtnClicked() {
        interactionListener.onOperationFinished();
    }

    void onRepeatPrintRejectSlipClicked() {
        printSlipOperationRejected(mBankResponse);
    }

    void onNoRejectSlipCancelOperationClicked() {
        interactionListener.onOperationFinished();
    }

    void onAfterSlipContinueBtnClicked() {
        interactionListener.onOperationFinished();
    }

    private void printSlipCancelFirst(List<String> receipt, boolean repeat) {

        view.showPrintingFirstSlip(repeat);

        mTicketTapeChecker.checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();
                    params.tplParams.slipLines = receipt;
                    return params;
                }))
                .flatMap(params -> mPrinterManager.getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showPrintFirstSlipSuccessState(), throwable -> {
                    if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        interactionListener.navigateToTicketTapeIsNotSet();
                    }
                    view.showPrintFirstSlipFailState();
                });
    }

    private void onOperationRejected(List<String> receipt, String bankResponse) {
        if (receipt == null || receipt.isEmpty()) {
            //раз нам нечего печатать, то ничего и не печатаем...
            view.showOperationRejectedWithoutSlip(bankResponse);
        } else {
            printSlipOperationRejected(bankResponse);
        }
    }

    private void printSlipOperationRejected(String bankResponse) {

        view.showPrintingRejectSlipState(bankResponse);

        mTicketTapeChecker.checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();
                    params.tplParams.slipLines = mReceipt;
                    return params;
                }))
                .flatMap(params -> mPrinterManager.getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showPrintRejectSlipSuccessState(), throwable -> {
                    if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        interactionListener.navigateToTicketTapeIsNotSet();
                    }
                    view.showPrintRejectSlipFailState();
                });
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void navigateToTicketTapeIsNotSet();

        void onOperationFinished();
    }

}
