package ru.ppr.cppk.ui.activity.fineSale;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class FineSalePresenter extends BaseMvpPresenter<FineSaleView> {

    private static final String TAG = Logger.makeLogTag(FineSalePresenter.class);

    private InteractionListener mInteractionListener;

    private boolean mInitialized = false;
    private final FineSaleData fineSaleData;

    @Inject
    FineSalePresenter(FineSaleDataStorage fineSaleDataStorage) {
        this.fineSaleData = fineSaleDataStorage.getFineSaleData();
    }

    void initialize() {
        if (!mInitialized) {
            mInitialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        mInteractionListener.transferDataToChild(fineSaleData);
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.mInteractionListener = interactionListener;
    }

    void onSellBtnClick() {
        if (fineSaleData.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
            fineSaleData.setStatus(FineSaleEvent.Status.PAYED);
            mInteractionListener.navigateToPrintFineCheck();
        } else {
            mInteractionListener.navigateToCardPayment(fineSaleData.getFine().getValue());
        }
    }

    void onCardPaymentFailed(long bankTransactionEventId) {
        fineSaleData.setBankTransactionEventId(bankTransactionEventId == -1 ? null : bankTransactionEventId);
        fineSaleData.setStatus(FineSaleEvent.Status.CREATED);
        mInteractionListener.navigateToFineSalePreparation();
    }

    void onCardPaymentCompleted(long bankTransactionEventId) {
        fineSaleData.setBankTransactionEventId(bankTransactionEventId == -1 ? null : bankTransactionEventId);
        fineSaleData.setStatus(FineSaleEvent.Status.PAYED);
        mInteractionListener.navigateToPrintFineCheck();
    }

    void onCancelCardPaymentRequired(long bankTransactionEventId) {
        fineSaleData.setBankTransactionEventId(bankTransactionEventId == -1 ? null : bankTransactionEventId);
        fineSaleData.setStatus(FineSaleEvent.Status.PAYED);
        mInteractionListener.navigateToCancelCardPayment(bankTransactionEventId);
    }

    void onReturnMoneyRequired() {
        if (fineSaleData.getBankTransactionEventId() != null) {
            mInteractionListener.navigateToCancelCardPayment(fineSaleData.getBankTransactionEventId());
        } else {
            mInteractionListener.navigateToCancelCashPayment(fineSaleData.getFine().getValue());
        }
    }

    void onCancelCardPaymentFinished() {
        mInteractionListener.navigateToFineSalePreparation();
    }

    void onCancelCashPaymentFinished() {
        mInteractionListener.navigateToFineSalePreparation();
    }

    void onPrintOperationCanceled() {
        mInteractionListener.navigateToFineSalePreparation();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void transferDataToChild(FineSaleData fineSaleData);

        void navigateToPrintFineCheck();

        void navigateToCardPayment(BigDecimal amount);

        void navigateToCancelCardPayment(long bankTransactionEventId);

        void navigateToFineSalePreparation();

        void navigateToCancelCashPayment(BigDecimal amount);
    }
}
