package ru.ppr.cppk.ui.activity.fineSale;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.math.BigDecimal;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.cashPaymentCancel.CashPaymentCancelFragment;
import ru.ppr.cppk.ui.fragment.fineSalePreparation.FineSalePreparationFragment;
import ru.ppr.cppk.ui.fragment.posCancel.PosCancelFragment;
import ru.ppr.cppk.ui.fragment.posSale.PosSaleFragment;
import ru.ppr.cppk.ui.fragment.printFineCheck.HasPrintFineCheckSharedComponent;
import ru.ppr.cppk.ui.fragment.printFineCheck.PrintFineCheckFragment;
import ru.ppr.cppk.ui.fragment.printFineCheck.PrintFineCheckSharedComponent;
import ru.ppr.logger.Logger;

/**
 * Экран выбора льготы
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleActivity extends SimpleMvpActivity implements FineSaleView, HasPrintFineCheckSharedComponent {

    private static final String TAG = Logger.makeLogTag(FineSaleActivity.class);
    // MVP CHILDREN
    private static final String MVP_FINE_SALE_PREPARATION = "MVP_FINE_SALE_PREPARATION";
    private static final String MVP_POS_SALE = "MVP_POS_SALE";
    private static final String MVP_POS_CANCEL = "MVP_POS_CANCEL";
    private static final String MVP_CASH_PAYMENT_CANCEL = "MVP_CASH_PAYMENT_CANCEL";

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, FineSaleActivity.class);
    }

    // region Di
    private FineSaleComponent component;
    // endregion
    /**
     * Экран оформления штрафа
     */
    private FineSalePreparationFragment fineSalePreparationFragment;
    /**
     * Экран печати чека
     */
    private PrintFineCheckFragment printFineCheckFragment;
    /**
     * Экран оплаты банковской картой
     */
    private PosSaleFragment posSaleFragment;
    /**
     * Экран отмены банковской транзакции
     */
    private PosCancelFragment posCancelFragment;
    /**
     * Экран возврата наличных
     */
    private CashPaymentCancelFragment cashPaymentCancelFragment;
    //region Other
    private FineSalePresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerFineSaleComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fine_sale);

        FineSalePreparationFragment fragment = (FineSalePreparationFragment) getFragmentManager().findFragmentByTag(FineSalePreparationFragment.FRAGMENT_TAG);
        if (fragment != null) {
            fineSalePreparationFragment = fragment;
            fineSalePreparationFragment.setInteractionListener(fineSalePreparationInteractionListener);
            fineSalePreparationFragment.init(getMvpDelegate(), MVP_FINE_SALE_PREPARATION);
        }

        presenter = getMvpDelegate().getPresenter(component::fineSalePresenter, FineSalePresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(fineSaleInteractionListener);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;
            if (fragmentOnBackPressed.onBackPress()) {
                return;
            }
        }
        super.onBackPressed();
    }

    private FineSalePresenter.InteractionListener fineSaleInteractionListener = new FineSalePresenter.InteractionListener() {
        @Override
        public void transferDataToChild(FineSaleData fineSaleData) {
            FineSalePreparationFragment fragment = FineSalePreparationFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, FineSalePreparationFragment.FRAGMENT_TAG)
                    .commit();
            fineSalePreparationFragment = fragment;
            fineSalePreparationFragment.setInteractionListener(fineSalePreparationInteractionListener);
            fineSalePreparationFragment.init(getMvpDelegate(), MVP_FINE_SALE_PREPARATION);
            fineSalePreparationFragment.initialize(fineSaleData);
        }

        @Override
        public void navigateToPrintFineCheck() {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if (currentFragment == fineSalePreparationFragment) {
                // Оставляем экран экран оформления
                fragmentTransaction.detach(currentFragment);
            } else if (currentFragment == posSaleFragment) {
                // Удаляем экран оплаты банковской картой
                fragmentTransaction.remove(currentFragment);
                posSaleFragment = null;
            } else {
                throw new IllegalStateException("Not expected state");
            }

            PrintFineCheckFragment fragment = PrintFineCheckFragment.newInstance();
            printFineCheckFragment = fragment;
            printFineCheckFragment.setInteractionListener(printFineCheckInteractionListener);
            fragmentTransaction
                    .add(R.id.fragmentContainer, fragment, PrintFineCheckFragment.FRAGMENT_TAG)
                    .commit();
        }

        @Override
        public void navigateToCardPayment(BigDecimal amount) {
            PosSaleFragment fragment = PosSaleFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    // Оставляем экран экран оформления
                    .detach(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .add(R.id.fragmentContainer, fragment, PosSaleFragment.FRAGMENT_TAG)
                    .commit();
            posSaleFragment = fragment;
            posSaleFragment.setInteractionListener(posSaleInteractionListener);
            posSaleFragment.init(getMvpDelegate(), MVP_POS_SALE);
            posSaleFragment.initialize(amount);
        }

        @Override
        public void navigateToCancelCardPayment(long bankTransactionEventId) {
            // Удаляем экран оплаты
            posSaleFragment = null;
            // Удаляем экран печати
            printFineCheckFragment = null;
            PosCancelFragment fragment = PosCancelFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .add(R.id.fragmentContainer, fragment, PosCancelFragment.FRAGMENT_TAG)
                    .commit();
            posCancelFragment = fragment;
            posCancelFragment.setInteractionListener(posCancelInteractionListener);
            posCancelFragment.init(getMvpDelegate(), MVP_POS_CANCEL);
            posCancelFragment.initialize(bankTransactionEventId);
        }

        @Override
        public void navigateToFineSalePreparation() {
            // Удаляем отмены оплаты картой
            posCancelFragment = null;
            // Удаляем отмены оплаты наличными
            cashPaymentCancelFragment = null;
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .attach(fineSalePreparationFragment)
                    .commit();
        }

        @Override
        public void navigateToCancelCashPayment(BigDecimal amount) {
            // Удаляем экран печати
            printFineCheckFragment = null;
            CashPaymentCancelFragment fragment = CashPaymentCancelFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .add(R.id.fragmentContainer, fragment, CashPaymentCancelFragment.FRAGMENT_TAG)
                    .commit();
            cashPaymentCancelFragment = fragment;
            cashPaymentCancelFragment.setInteractionListener(cashPaymentCancelInteractionListener);
            cashPaymentCancelFragment.init(getMvpDelegate(), MVP_CASH_PAYMENT_CANCEL);
            cashPaymentCancelFragment.initialize(amount);
        }
    };

    @Override
    public PrintFineCheckSharedComponent getPrintFineCheckSharedComponent() {
        return component;
    }

    private FineSalePreparationFragment.InteractionListener fineSalePreparationInteractionListener = new FineSalePreparationFragment.InteractionListener() {

        @Override
        public void onSellBtnClick() {
            presenter.onSellBtnClick();
        }

        @Override
        public void closeScreen() {
            finish();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            Navigator.navigateToCloseShiftActivity(FineSaleActivity.this, true, true);
        }
    };

    private PrintFineCheckFragment.InteractionListener printFineCheckInteractionListener = new PrintFineCheckFragment.InteractionListener() {

        @Override
        public void onReturnMoneyRequired() {
            presenter.onReturnMoneyRequired();
        }

        @Override
        public void navigateToCalculateDelivery(BigDecimal amount) {
            Navigator.navigateToCalculateDeliveryFixedCostActivity(FineSaleActivity.this, amount);
            finish();
        }

        @Override
        public void onOperationCanceled() {
            presenter.onPrintOperationCanceled();
        }

        @Override
        public void closeScreen() {
            finish();
        }
    };

    private PosSaleFragment.InteractionListener posSaleInteractionListener = new PosSaleFragment.InteractionListener() {

        @Override
        public void onOperationFailed(long bankTransactionEventId) {
            presenter.onCardPaymentFailed(bankTransactionEventId);
        }

        @Override
        public void onOperationCompleted(long bankTransactionEventId) {
            presenter.onCardPaymentCompleted(bankTransactionEventId);
        }

        @Override
        public void onCancelTransactionRequired(long bankTransactionEventId) {
            presenter.onCancelCardPaymentRequired(bankTransactionEventId);
        }
    };

    private PosCancelFragment.InteractionListener posCancelInteractionListener = new PosCancelFragment.InteractionListener() {

        @Override
        public void onOperationFinished() {
            presenter.onCancelCardPaymentFinished();
        }
    };

    private CashPaymentCancelFragment.InteractionListener cashPaymentCancelInteractionListener = new CashPaymentCancelFragment.InteractionListener() {

        @Override
        public void onOperationFinished() {
            presenter.onCancelCashPaymentFinished();
        }
    };
}
