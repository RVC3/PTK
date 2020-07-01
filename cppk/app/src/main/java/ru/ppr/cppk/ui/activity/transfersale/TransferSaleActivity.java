package ru.ppr.cppk.ui.activity.transfersale;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.activity.transfersale.preparation.PreparationFragment;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.cashPaymentCancel.CashPaymentCancelFragment;
import ru.ppr.cppk.ui.fragment.pdSalePrint.PdSalePrintFragment;
import ru.ppr.cppk.ui.fragment.pdSaleWrite.PdSaleWriteFragment;
import ru.ppr.cppk.ui.fragment.posCancel.PosCancelFragment;
import ru.ppr.cppk.ui.fragment.posSale.PosSaleFragment;

/**
 * Экран оформления трансфера.
 *
 * @author Dmitry Nevolin
 */
public class TransferSaleActivity extends MvpActivity implements TransferSaleView {


    //region Extras
    private static final String EXTRA_TRANSFER_SALE_PARAMS = "EXTRA_TRANSFER_SALE_PARAMS";
    //endregion
    //region Fragment tags
    private static final String F_TAG_PREPARATION = "F_TAG_PREPARATION";
    private static final String F_TAG_CASH_PAYMENT_CANCEL = "F_TAG_CASH_PAYMENT_CANCEL";
    private static final String F_TAG_POS_SALE = "F_TAG_POS_SALE";
    private static final String F_TAG_POS_CANCEL = "F_TAG_POS_CANCEL";
    private static final String F_TAG_PD_PRINT = "F_TAG_PD_PRINT";
    private static final String F_TAG_PD_WRITE = "F_TAG_PD_WRITE";
    //endregion

    /**
     * Создаёт интент для запуска данной активити
     *
     * @param context контекст
     * @return интент для запуска данной активити
     */
    public static Intent getCallingIntent(@NonNull Context context, @NonNull TransferSaleParams transferSaleParams) {
        return new Intent(context, TransferSaleActivity.class)
                .putExtra(EXTRA_TRANSFER_SALE_PARAMS, transferSaleParams);
    }

    // MVP CHILDREN
    private static final String MVP_POS_SALE = "MVP_POS_SALE";
    private static final String MVP_POS_CANCEL = "MVP_POS_CANCEL";
    private static final String MVP_CASH_PAYMENT_CANCEL = "MVP_CASH_PAYMENT_CANCEL";
    // Dependencies
    private TransferSaleComponent component;
    private TransferSalePresenter presenter;
    // Fragments
    /**
     * Фрагмент подготовки билета
     */
    private PreparationFragment preparationFragment;
    /**
     * Экран записи на карту
     */
    private PdSaleWriteFragment pdSaleWriteFragment;
    /**
     * Экран печати ПД
     */
    private PdSalePrintFragment pdSalePrintFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerTransferSaleComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .transferSaleParams(getIntent().getParcelableExtra(EXTRA_TRANSFER_SALE_PARAMS))
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_sale);

        presenter = getMvpDelegate().getPresenter(component::transferSalePresenter, TransferSalePresenter.class);
        presenter.setNavigator(navigator);
        presenter.initialize2();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;
            if (fragmentOnBackPressed.onBackPress()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onClickSettings() {
        // http://agile.srvdev.ru/browse/CPPKPP-34368
        // отключаем кнопку на экране продажи
    }

    public TransferSaleComponent getTransferSaleComponent() {
        return component;
    }

    private void removeExistingFragment(FragmentTransaction fragmentTransaction) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            return;
        }
        if (currentFragment == preparationFragment) {
            // Оставляем экран оформления
            fragmentTransaction.detach(currentFragment);
        } else if (currentFragment == posSaleFragment) {
            // Удаляем экран оплаты банковской картой
            fragmentTransaction.remove(currentFragment);
            posSaleFragment = null;
        } else if (currentFragment == posCancelFragment) {
            // Удаляем экран отмены оплаты банковской картой
            fragmentTransaction.remove(currentFragment);
            posCancelFragment = null;
        } else if (currentFragment == cashPaymentCancelFragment) {
            // Удаляем экран отмены оплаты наличными
            fragmentTransaction.remove(currentFragment);
            cashPaymentCancelFragment = null;
        } else if (currentFragment == pdSaleWriteFragment) {
            // Удаляем экран записи на карту
            fragmentTransaction.remove(currentFragment);
            pdSaleWriteFragment = null;
        } else if (currentFragment == pdSalePrintFragment) {
            // Удаляем экран печати ПД
            fragmentTransaction.remove(currentFragment);
            pdSalePrintFragment = null;
        }
    }

    private TransferSalePresenter.Navigator navigator = new TransferSalePresenter.Navigator() {

        @Override
        public void navigateToPreparation() {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            if (preparationFragment == null) {
                preparationFragment = PreparationFragment.newInstance();
                preparationFragment.setInteractionListener(preparationFragmentInteractionListener);
                fragmentTransaction.add(R.id.fragment_container, preparationFragment, F_TAG_PREPARATION);
            } else {
                fragmentTransaction.attach(preparationFragment);
            }
            fragmentTransaction.commit();
        }

        @Override
        public void navigateToCancelCashPayment(BigDecimal amount) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            CashPaymentCancelFragment fragment = CashPaymentCancelFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragment_container, fragment, F_TAG_CASH_PAYMENT_CANCEL)
                    .commit();
            cashPaymentCancelFragment = fragment;
            cashPaymentCancelFragment.setInteractionListener(cashPaymentCancelFragmentInteractionListener);
            cashPaymentCancelFragment.init(getMvpDelegate(), MVP_CASH_PAYMENT_CANCEL);
            cashPaymentCancelFragment.initialize(amount);
        }

        @Override
        public void navigateToCardPayment(BigDecimal amount) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            PosSaleFragment fragment = PosSaleFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragment_container, fragment, F_TAG_POS_SALE)
                    .commit();
            posSaleFragment = fragment;
            posSaleFragment.setInteractionListener(posSaleInteractionListener);
            posSaleFragment.init(getMvpDelegate(), MVP_POS_SALE);
            posSaleFragment.initialize(amount);
        }

        @Override
        public void navigateToCancelCardPayment(long bankTransactionEventId) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            PosCancelFragment fragment = PosCancelFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragment_container, fragment, F_TAG_POS_CANCEL)
                    .commit();
            posCancelFragment = fragment;
            posCancelFragment.setInteractionListener(posCancelInteractionListener);
            posCancelFragment.init(getMvpDelegate(), MVP_POS_CANCEL);
            posCancelFragment.initialize(bankTransactionEventId);
        }

        @Override
        public void navigateToPrintPdCheck(@NonNull DataSalePD dataSalePD) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            PdSalePrintFragment fragment = PdSalePrintFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragment_container, fragment, F_TAG_PD_PRINT)
                    .commit();
            pdSalePrintFragment = fragment;
            pdSalePrintFragment.setInteractionListener(pdSalePrintInteractionListener);
            pdSalePrintFragment.initialize(dataSalePD);
        }

        @Override
        public void navigateToWriteToBSC(@NonNull DataSalePD dataSalePD) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            removeExistingFragment(fragmentTransaction);
            PdSaleWriteFragment fragment = PdSaleWriteFragment.newInstance(true);
            fragmentTransaction
                    .add(R.id.fragment_container, fragment, F_TAG_PD_WRITE)
                    .commit();
            pdSaleWriteFragment = fragment;
            pdSaleWriteFragment.setInteractionListener(pdSaleWriteFragmentInteractionListener);
            pdSaleWriteFragment.initialize(dataSalePD);
        }

        @Override
        public void navigateToSaleSuccess(PdSaleSuccessParams pdSaleSuccessParams) {
            Navigator.navigateToSellPdSuccessActivity(TransferSaleActivity.this, pdSaleSuccessParams);
            finish();
        }

        @Override
        public void navigateBack() {
            finish();
        }

    };

    private PreparationFragment.InteractionListener preparationFragmentInteractionListener = new PreparationFragment.InteractionListener() {

        @Override
        public void navigateBack() {
            presenter.onPreparationCanceled();
        }

        @Override
        public void writePd() {
            presenter.writePd();
        }

        @Override
        public void printPd() {
            presenter.printPd();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            Navigator.navigateToCloseShiftActivity(TransferSaleActivity.this, true, true);
        }

    };

    private CashPaymentCancelFragment.InteractionListener cashPaymentCancelFragmentInteractionListener = () -> presenter.onCancelCashPaymentFinished();

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

    private PosCancelFragment.InteractionListener posCancelInteractionListener = () -> presenter.onCancelCardPaymentFinished();

    private PdSalePrintFragment.InteractionListener pdSalePrintInteractionListener = new PdSalePrintFragment.InteractionListener() {

        @Override
        public void onReturnMoneyRequired() {
            presenter.onReturnMoneyRequired();
        }

        @Override
        public void onPrintCompleted(long saleEventId) {
            presenter.onPrintCompleted(saleEventId);
        }

        @Override
        public void onCancelSaleProcess() {
            presenter.onCancelSaleProcess();
        }
    };

    private PdSaleWriteFragment.InteractionListener pdSaleWriteFragmentInteractionListener = new PdSaleWriteFragment.InteractionListener() {

        @Override
        public void onPrintPdOnWriteDenied() {
            presenter.onPrintPdOnWriteDenied();
        }

        @Override
        public void onWriteCompleted(long newPdId, boolean isPrinted) {
            presenter.onWriteCompleted(newPdId);
        }

        @Override
        public void onReturnMoneyRequired() {
            presenter.onReturnMoneyRequired();
        }

        @Override
        public void onCancelSaleProcess() {
            presenter.onCancelSaleProcess();
        }

    };

}
