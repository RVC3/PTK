package ru.ppr.cppk.ui.fragment.cashPaymentCancel;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface CashPaymentCancelView extends MvpView {

    void setAmount(BigDecimal amount);
}
