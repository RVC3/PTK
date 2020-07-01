package ru.ppr.cppk.ui.activity.pdSale;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.math.BigDecimal;

import ru.ppr.cppk.R;
import ru.ppr.cppk.model.PdSaleData;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.cashPaymentCancel.CashPaymentCancelFragment;
import ru.ppr.cppk.ui.fragment.pdSalePreparation.PdSalePreparationFragment;
import ru.ppr.cppk.ui.fragment.pdSalePrint.PdSalePrintFragment;
import ru.ppr.cppk.ui.fragment.pdSalePrintSuccess.PdSalePrintSuccessFragment;
import ru.ppr.cppk.ui.fragment.pdSaleWrite.PdSaleWriteFragment;
import ru.ppr.cppk.ui.fragment.pdSaleWriteWithExemption.PdSaleWriteWithExemptionFragment;
import ru.ppr.cppk.ui.fragment.posCancel.PosCancelFragment;
import ru.ppr.cppk.ui.fragment.posSale.PosSaleFragment;
import ru.ppr.cppk.ui.fragment.removeExemption.RemoveExemptionFragment;
import ru.ppr.logger.Logger;

/**
 * Экран оформления ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleActivity extends SimpleMvpActivity implements PdSaleView {

    private static final String TAG = Logger.makeLogTag(PdSaleActivity.class);

    // EXTRAS
    private static final String EXTRA_PD_SALE_PARAMS = "EXTRA_PD_SALE_PARAMS";
    // MVP CHILDREN
    private static final String MVP_PD_SALE_PREPARATION = "MVP_PD_SALE_PREPARATION";
    private static final String MVP_PRINT_FINE_CHECK = "MVP_PRINT_FINE_CHECK";
    private static final String MVP_POS_SALE = "MVP_POS_SALE";
    private static final String MVP_POS_CANCEL = "MVP_POS_CANCEL";
    private static final String MVP_CASH_PAYMENT_CANCEL = "MVP_CASH_PAYMENT_CANCEL";
    private static final String MVP_REMOVE_EXEMPTION = "MVP_REMOVE_EXEMPTION";
    private static final String MVP_PRINT_SUCCESS = "MVP_PRINT_SUCCESS";

    public static Intent getCallingIntent(Context context, PdSaleParams pdSaleParams) {
        Intent intent = new Intent(context, PdSaleActivity.class);
        intent.putExtra(EXTRA_PD_SALE_PARAMS, pdSaleParams);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Di
     */
    private PdSaleDi di;
    /**
     * Экран оформления ПД
     */
    private PdSalePreparationFragment pdSalePreparationFragment;
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
    /**
     * Экран печати ПД
     */
    private PdSalePrintFragment pdSalePrintFragment;
    /**
     * Экран записи ПД на карту
     */
    private PdSaleWriteFragment pdSaleWriteFragment;
    /**
     * Экран записи ПД на карту с использованием льготы
     */
    private PdSaleWriteWithExemptionFragment pdSaleWriteWithExemptionFragment;
    /**
     * Экран успешной печати одного из серии ПД
     */
    private PdSalePrintSuccessFragment pdSalePrintSuccessFragment;
    //region Other
    private PdSalePresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fine_sale);
        di = new PdSaleDi(di());

        PdSalePreparationFragment fragment = (PdSalePreparationFragment) getFragmentManager().findFragmentByTag(PdSalePreparationFragment.FRAGMENT_TAG);
        if (fragment != null) {
            pdSalePreparationFragment = fragment;
            pdSalePreparationFragment.setInteractionListener(pdSalePreparationInteractionListener);
            pdSalePreparationFragment.init(getMvpDelegate(), MVP_PD_SALE_PREPARATION);
        }

        presenter = getMvpDelegate().getPresenter(PdSalePresenter::new, PdSalePresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(fineSaleInteractionListener);
        presenter.initialize(
                getIntent().getParcelableExtra(EXTRA_PD_SALE_PARAMS),
                di.localDaoSession(),
                di.nsiDaoSession(),
                di.ticketStorageTypeToTicketTypeChecker()
        );
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

    @Override
    public void onClickSettings() {
        // http://agile.srvdev.ru/browse/CPPKPP-34368
        // отключаем кнопку на экране продажи
    }

    void removeExistingFragment(FragmentTransaction fragmentTransaction) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == pdSalePreparationFragment) {
            // Оставляем экран оформления
            fragmentTransaction.detach(currentFragment);
        } else if (currentFragment == posSaleFragment) {
            // Удаляем экран оплаты банковской картой
            fragmentTransaction.remove(currentFragment);
            posSaleFragment = null;
        } else if (currentFragment == pdSalePrintSuccessFragment) {
            // Удаляем экран успешной печати
            fragmentTransaction.remove(currentFragment);
            pdSalePrintSuccessFragment = null;
        } else if (currentFragment == pdSaleWriteFragment) {
            // Удаляем экран записи на карту
            fragmentTransaction.remove(currentFragment);
            pdSaleWriteFragment = null;
        } else {
            throw new IllegalStateException("Not expected state");
        }
    }

    private PdSalePresenter.InteractionListener fineSaleInteractionListener = new PdSalePresenter.InteractionListener() {
        @Override
        public void transferDataToChild(PdSaleParams pdSaleParams, PdSaleData pdSaleData) {
            PdSalePreparationFragment fragment = PdSalePreparationFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, PdSalePreparationFragment.FRAGMENT_TAG)
                    .commit();
            pdSalePreparationFragment = fragment;
            pdSalePreparationFragment.setInteractionListener(pdSalePreparationInteractionListener);
            pdSalePreparationFragment.init(getMvpDelegate(), MVP_PD_SALE_PREPARATION);
            pdSalePreparationFragment.initialize(pdSaleParams, pdSaleData);
        }

        @Override
        public void navigateToCardPayment(BigDecimal amount) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            // Удаляем любой экран за исключением экрана оформления
            removeExistingFragment(fragmentTransaction);
            PosSaleFragment fragment = PosSaleFragment.newInstance();
            fragmentTransaction
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
            pdSalePrintFragment = null;
            // Удаляем экран записи на карту
            pdSaleWriteFragment = null;
            // Удаляем экран записи на карту с использованием льготы
            pdSaleWriteWithExemptionFragment = null;
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
        public void navigateToPdSalePreparation() {
            // Удаляем экран отмены оплаты картой
            posCancelFragment = null;
            // Удаляем экран отмены оплаты наличными
            cashPaymentCancelFragment = null;
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .attach(pdSalePreparationFragment)
                    .commit();
        }

        @Override
        public void navigateToCancelCashPayment(BigDecimal amount) {
            // Удаляем экран печати
            pdSalePrintFragment = null;
            // Удаляем экран записи на карту
            pdSaleWriteFragment = null;
            // Удаляем экран записи на карту с использованием льготы
            pdSaleWriteWithExemptionFragment = null;
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

        @Override
        public void navigateToPrintPdCheck(DataSalePD dataSalePD) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            // Удаляем любой экран за исключением экрана оформления
            removeExistingFragment(fragmentTransaction);
            PdSalePrintFragment fragment = PdSalePrintFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragmentContainer, fragment, PdSalePrintFragment.FRAGMENT_TAG)
                    .commit();
            pdSalePrintFragment = fragment;
            pdSalePrintFragment.setInteractionListener(pdSalePrintInteractionListener);
            pdSalePrintFragment.initialize(dataSalePD);
        }

        @Override
        public void navigateToWriteToBSC(DataSalePD dataSalePD) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            // Удаляем любой экран за исключением экрана оформления
            removeExistingFragment(fragmentTransaction);
            PdSaleWriteFragment fragment = PdSaleWriteFragment.newInstance(false);
            fragmentTransaction
                    .add(R.id.fragmentContainer, fragment, PdSaleWriteFragment.FRAGMENT_TAG)
                    .commit();
            pdSaleWriteFragment = fragment;
            pdSaleWriteFragment.setInteractionListener(pdSaleWriteInteractionListener);
            pdSaleWriteFragment.initialize(dataSalePD);
        }

        @Override
        public void navigateToSellWithExemption(DataSalePD dataSalePD) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            // Удаляем любой экран за исключением экрана оформления
            removeExistingFragment(fragmentTransaction);
            PdSaleWriteWithExemptionFragment fragment = PdSaleWriteWithExemptionFragment.newInstance();
            fragmentTransaction
                    .add(R.id.fragmentContainer, fragment, PdSaleWriteWithExemptionFragment.FRAGMENT_TAG)
                    .commit();
            pdSaleWriteWithExemptionFragment = fragment;
            pdSaleWriteWithExemptionFragment.setInteractionListener(pdSaleWriteWithExemptionInteractionListener);
            pdSaleWriteWithExemptionFragment.initialize(dataSalePD);
        }

        @Override
        public void navigateToSaleSuccess(PdSaleSuccessParams pdSaleSuccessParams) {
            Navigator.navigateToSellPdSuccessActivity(PdSaleActivity.this, pdSaleSuccessParams);
            finish();
        }

        @Override
        public void navigateToPrintSuccess() {
            // Удаляем экран печати
            pdSalePrintFragment = null;
            PdSalePrintSuccessFragment fragment = PdSalePrintSuccessFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .add(R.id.fragmentContainer, fragment, PdSalePrintSuccessFragment.FRAGMENT_TAG)
                    .commit();
            pdSalePrintSuccessFragment = fragment;
            pdSalePrintSuccessFragment.setInteractionListener(pdSalePrintSuccessInteractionListener);
            pdSalePrintSuccessFragment.init(getMvpDelegate(), MVP_PRINT_SUCCESS);
            pdSalePrintSuccessFragment.initialize();
        }
    };

    private PdSalePreparationFragment.InteractionListener pdSalePreparationInteractionListener = new PdSalePreparationFragment.InteractionListener() {

        @Override
        public void navigateBack() {
            finish();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            Navigator.navigateToCloseShiftActivity(PdSaleActivity.this, true, true);
        }

        @Override
        public void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams) {
            RemoveExemptionFragment fragment = RemoveExemptionFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, RemoveExemptionFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            fragment.setInteractionListener(removeExemptionInteractionListener);
            fragment.init(getMvpDelegate(), MVP_REMOVE_EXEMPTION);
            fragment.initialize(removeExemptionParams);
        }

        @Override
        public void writePd() {
            presenter.writePd();
        }

        @Override
        public void printPd() {
            presenter.printPd();
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

    private RemoveExemptionFragment.InteractionListener removeExemptionInteractionListener = new RemoveExemptionFragment.InteractionListener() {
        @Override
        public void navigateToPreviousScreen(boolean exemptionRemoved) {
            getFragmentManager().popBackStack();
            if (exemptionRemoved) {
                pdSalePreparationFragment.onExemptionRemoved();
            }
        }
    };

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

    private PdSaleWriteFragment.InteractionListener pdSaleWriteInteractionListener = new PdSaleWriteFragment.InteractionListener() {
        @Override
        public void onPrintPdOnWriteDenied() {
            presenter.onPrintPdOnWriteDenied();
        }

        @Override
        public void onWriteCompleted(long newPdId, boolean isPrinted) {
            presenter.onWriteCompleted(newPdId, isPrinted);
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

    private PdSaleWriteWithExemptionFragment.InteractionListener pdSaleWriteWithExemptionInteractionListener = new PdSaleWriteWithExemptionFragment.InteractionListener() {

        @Override
        public void onWriteCompleted(long newPdId, boolean isPrinted) {
            presenter.onWriteCompleted(newPdId, isPrinted);
        }

        @Override
        public void onCancelSaleProcess() {
            presenter.onCancelSaleProcess();
        }

        @Override
        public void onReturnMoneyRequired() {
            presenter.onReturnMoneyRequired();
        }
    };

    private PdSalePrintSuccessFragment.InteractionListener pdSalePrintSuccessInteractionListener = new PdSalePrintSuccessFragment.InteractionListener() {

        @Override
        public void onPrintNextPdBtnClicked() {
            presenter.onPrintNextPdBtnClicked();
        }
    };
}
