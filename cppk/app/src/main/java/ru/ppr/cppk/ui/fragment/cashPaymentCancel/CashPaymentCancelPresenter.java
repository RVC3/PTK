package ru.ppr.cppk.ui.fragment.cashPaymentCancel;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class CashPaymentCancelPresenter extends BaseMvpViewStatePresenter<CashPaymentCancelView, CashPaymentCancelViewState> {

    private static final String TAG = Logger.makeLogTag(CashPaymentCancelPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private BigDecimal mAmount;

    public CashPaymentCancelPresenter() {

    }

    @Override
    protected CashPaymentCancelViewState provideViewState() {
        return new CashPaymentCancelViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize(BigDecimal amount) {
        if (!mInitialized) {
            mInitialized = true;
            mAmount = amount;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        view.setAmount(mAmount);
    }

    void onContinueBtnClicked() {
        interactionListener.onOperationFinished();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onOperationFinished();
    }

}
