package ru.ppr.cppk.ui.fragment.cashPaymentCancel;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class CashPaymentCancelViewState extends BaseMvpViewState<CashPaymentCancelView> implements CashPaymentCancelView {

    private BigDecimal mAmount;

    @Override
    protected void onViewAttached(CashPaymentCancelView view) {
        view.setAmount(mAmount);
    }

    @Override
    protected void onViewDetached(CashPaymentCancelView view) {

    }

    @Override
    public void setAmount(BigDecimal amount) {
        mAmount = amount;
        forEachView(view -> view.setAmount(mAmount));
    }
}
