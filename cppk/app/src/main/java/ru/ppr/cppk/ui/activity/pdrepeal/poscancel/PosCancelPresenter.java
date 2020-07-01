package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class PosCancelPresenter extends BaseMvpViewStatePresenter<PosCancelView, PosCancelViewState> {

    private static final String TAG = Logger.makeLogTag(PosCancelPresenter.class);

    private boolean initialized = false;
    private final long saleTransactionEventId;
    private final UiThread uiThread;
    private final PosManager posManager;
    private final LocalDaoSession localDaoSession;
    private final PosCancelSharedModel posCancelSharedModel;

    @Inject
    PosCancelPresenter(PosCancelViewState posCancelViewState,
                       @Named("saleTransactionEventId") long saleTransactionEventId,
                       UiThread uiThread,
                       PosManager posManager,
                       LocalDaoSession localDaoSession,
                       PosCancelSharedModel posCancelSharedModel) {
        super(posCancelViewState);
        this.saleTransactionEventId = saleTransactionEventId;
        this.uiThread = uiThread;
        this.posManager = posManager;
        this.localDaoSession = localDaoSession;
        this.posCancelSharedModel = posCancelSharedModel;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        uiThread.post(() -> view.showConnectingState(posManager.getConnectionTimeout()));
        posManager.cancelTransaction(saleTransactionEventId, new PosManager.AbstractFinancialTransactionListener() {

            @Override
            public void onTickBeforeTimeout(long value) {
                Logger.trace(TAG, "onTickBeforeTimeout, value = " + value);
                uiThread.post(() -> view.showConnectingState(value));
            }

            @Override
            public void onConnectionTimeout() {
                Logger.trace(TAG, "onConnectionTimeout");
                uiThread.post(() -> uiThread.post(view::showConnectionTimeoutState));
            }

            @Override
            public void onConnected() {
                Logger.trace(TAG, "onConnected");
                uiThread.post(() -> uiThread.post(() -> uiThread.post(view::showConnectedState)));
            }

            @Override
            public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                FinancialTransactionResult result = operationResult.getTransactionResult();
                Logger.trace(TAG, "onResult, result  = " + result);
                uiThread.post(() -> {
                    List<String> receipt = result == null ? null : result.getReceipt();
                    String bankResponse = result == null ? null : result.getBankResponse();
                    long returnTransactionEventId = result == null ? -1 : getLastReturnBankTransactionEventId();
                    if (result != null && result.isApproved()) {
                        Logger.trace(TAG, "onOperationCompleted");
                        Logger.trace(TAG, "returnTransactionEventId = " + returnTransactionEventId);
                        Logger.trace(TAG, "receipt = " + receipt);
                        Logger.trace(TAG, "bankResponse = " + bankResponse);
                        posCancelSharedModel.onOperationCompleted(returnTransactionEventId, receipt, bankResponse);
                    } else {
                        Logger.trace(TAG, "onOperationFailed");
                        Logger.trace(TAG, "returnTransactionEventId = " + returnTransactionEventId);
                        Logger.trace(TAG, "receipt = " + receipt);
                        Logger.trace(TAG, "bankResponse = " + bankResponse);
                        String message;
                        if (bankResponse == null) {
                            //http://agile.srvdev.ru/browse/CPPKPP-40277
                            message = operationResult.getErrorMessage();
                        } else {
                            message = bankResponse;
                        }
                        Logger.trace(TAG, "message = " + message);
                        posCancelSharedModel.onOperationFailed(returnTransactionEventId, receipt, message);
                    }
                });
            }
        });
    }

    /**
     * Возвращает Id последнего события совершения отмены банковской транзакции.
     * Предполагается, что так мы точно получим именно ту транзакцию, которая нам нужна.
     */
    private long getLastReturnBankTransactionEventId() {
        long id = localDaoSession.getBankTransactionDao().getLastEventByType(BankOperationType.CANCELLATION).getId();
        Logger.trace(TAG, "getLastReturnBankTransactionEventId, res = " + id);
        return id;
    }

    void onNotConnectedCancelBtnClicked() {
        Logger.trace(TAG, "onNotConnectedCancelBtnClicked");
        posCancelSharedModel.onOperationCanceled();
    }

}
