package ru.ppr.cppk.ui.fragment.posSale;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.BankOperationType;
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
public class PosSalePresenter extends BaseMvpViewStatePresenter<PosSaleView, PosSaleViewState> {

    private static final String TAG = Logger.makeLogTag(PosSalePresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private BigDecimal mAmount;
    private UiThread mUiThread;
    private PosManager mPosManager;
    private LocalDaoSession mLocalDaoSession;
    private PrinterManager mPrinterManager;
    private TicketTapeChecker mTicketTapeChecker;
    ///////////////////////////////////////////////////
    private List<String> mReceipt;
    private String mBankResponse;
    private long mBankTransactionEventId = -1;

    public PosSalePresenter() {

    }

    @Override
    protected PosSaleViewState provideViewState() {
        return new PosSaleViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize(BigDecimal amount, UiThread uiThread, PosManager posManager, LocalDaoSession localDaoSession, PrinterManager printerManager, TicketTapeChecker ticketTapeChecker) {
        if (!mInitialized) {
            mInitialized = true;
            mAmount = amount;
            mUiThread = uiThread;
            mPosManager = posManager;
            mLocalDaoSession = localDaoSession;
            mPrinterManager = printerManager;
            mTicketTapeChecker = ticketTapeChecker;
            onInitialize();
        }
    }

    private void onInitialize() {
        mUiThread.post(() -> view.showConnectingState(mPosManager.getConnectionTimeout()));
        mPosManager.sale(mAmount, new PosManager.AbstractFinancialTransactionListener() {
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
                    mBankTransactionEventId = result == null ? -1 : getLastSaleBankTransactionEventId();
                    if (result != null && result.isApproved()) {
                        printSlipSaleFirst(mReceipt, false);
                    } else {
                        onOperationRejected(mReceipt, mBankResponse);
                    }
                });
            }
        });
    }

    void onNotConnectedCancelOperationClicked() {
        interactionListener.onOperationFailed(mBankTransactionEventId);
    }

    void onCancelTransactionClicked() {
        view.showCancelConfirmationDialog();
    }

    void onCancelTransactionConfirmClicked() {
        interactionListener.onCancelTransactionRequired(mBankTransactionEventId);
    }

    void onAfterFirstSlipContinueBtnClicked() {
        printSlipSaleSecond(mReceipt);
    }

    void onRepeatPrintFirstSlipClicked() {
        printSlipSaleFirst(mReceipt, true);
    }

    void onAfterSecondSlipContinueBtnClicked() {
        interactionListener.onOperationCompleted(mBankTransactionEventId);
    }

    void onRepeatPrintSecondSlipClicked() {
        printSlipSaleSecond(mReceipt);
    }

    void onAfterRejectSlipContinueBtnClicked() {
        interactionListener.onOperationFailed(mBankTransactionEventId);
    }

    void onRepeatPrintRejectSlipClicked() {
        printSlipOperationRejected(mBankResponse);
    }

    void onNoRejectSlipCancelOperationClicked() {
        interactionListener.onOperationFailed(mBankTransactionEventId);
    }

    private void onOperationRejected(List<String> receipt, String bankResponse) {
        if (receipt == null || receipt.isEmpty()) {
            //раз нам нечего печатать, то ничего и не печатаем...
            view.showOperationRejectedWithoutSlip(bankResponse);
        } else {
            printSlipOperationRejected(bankResponse);
        }
    }

    private void printSlipSaleFirst(List<String> receipt, boolean repeat) {

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

    private void printSlipSaleSecond(List<String> receipt) {

        view.showPrintingSecondSlip();

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
                .subscribe(o -> view.showPrintSecondSlipSuccessState(), throwable -> {
                    if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        interactionListener.navigateToTicketTapeIsNotSet();
                    }
                    view.showPrintSecondSlipFailState();
                });
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

    private long getLastSaleBankTransactionEventId() {
        return mLocalDaoSession.getBankTransactionDao().getLastEventByType(BankOperationType.SALE).getId();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToTicketTapeIsNotSet();

        void onOperationFailed(long bankTransactionEventId);

        void onOperationCompleted(long bankTransactionEventId);

        void onCancelTransactionRequired(long bankTransactionEventId);
    }

}
