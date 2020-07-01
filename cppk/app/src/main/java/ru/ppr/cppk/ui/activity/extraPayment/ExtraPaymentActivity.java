package ru.ppr.cppk.ui.activity.extraPayment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.FragmentWorkWithPosTerminal;
import ru.ppr.cppk.ui.fragment.OnCancelBTOperationDialogClickListener;
import ru.ppr.cppk.ui.fragment.extraPaymentExecution.ExtraPaymentExecutionFragment;
import ru.ppr.cppk.ui.fragment.extraPaymentPrint.ExtraPaymentPrintFragment;
import ru.ppr.cppk.ui.fragment.removeExemption.RemoveExemptionFragment;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketCategory;

/**
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentActivity extends SimpleMvpActivity implements
        ExtraPaymentView,
        FragmentWorkWithPosTerminal.OnInteractionListener,
        ExtraPaymentPrintFragment.OnInteractionListener,
        CppkDialogFragment.CppkDialogClickListener {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentActivity.class);

    // EXTRAS
    private static final String EXTRA_EXTRA_PAYMENT_PARAMS = "EXTRA_EXTRA_PAYMENT_PARAMS";
    // MVP CHILDREN
    private static final String MVP_PAYMENT_EXECUTION_FRAGMENT = "MVP_PAYMENT_EXECUTION_FRAGMENT";
    private static final String MVP_REMOVE_EXEMPTION = "MVP_REMOVE_EXEMPTION";

    /**
     * @param context            Контекст
     * @param extraPaymentParams Информация о для оформления доплаты
     * @return Интент
     */
    public static Intent getCallingIntent(Context context, ExtraPaymentParams extraPaymentParams) {
        Intent intent = new Intent(context, ExtraPaymentActivity.class);
        intent.putExtra(EXTRA_EXTRA_PAYMENT_PARAMS, extraPaymentParams);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Di
     */
    private ExtraPaymentDi di;
    /**
     * Экран оформления ПД
     */
    private ExtraPaymentExecutionFragment extraPaymentExecutionFragment;
    /**
     * Флаг, что экран работы с POS-терминалом был запущен для продажи.
     */
    private boolean workWithPosWasStartedForSale = false;
    /**
     * Обработочик нажатий на кнопки в диалоговых окнах
     */
    private OnCancelBTOperationDialogClickListener onCancelBTOperationDialogClickListener;
    //region Other
    private ExtraPaymentPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_payment);
        di = new ExtraPaymentDi(di());

        ExtraPaymentExecutionFragment fragment = (ExtraPaymentExecutionFragment) getFragmentManager().findFragmentByTag(ExtraPaymentExecutionFragment.FRAGMENT_TAG);
        if (fragment != null) {
            extraPaymentExecutionFragment = fragment;
            extraPaymentExecutionFragment.setInteractionListener(extraPaymentExecutionInteractionListener);
            extraPaymentExecutionFragment.init(getMvpDelegate(), MVP_PAYMENT_EXECUTION_FRAGMENT);
        }

        presenter = getMvpDelegate().getPresenter(ExtraPaymentPresenter::new, ExtraPaymentPresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(extraPaymentInteractionListener);
        presenter.initialize(
                getIntent().getParcelableExtra(EXTRA_EXTRA_PAYMENT_PARAMS),
                di.localDaoSession(),
                di.documentNumberProvider()
        );
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof OnCancelBTOperationDialogClickListener)
            onCancelBTOperationDialogClickListener = (OnCancelBTOperationDialogClickListener) fragment;
    }

    @Override
    public DataSalePD getDataSalePD() {
        return presenter.getDataSalePd();
    }

    @Override
    public void startWorkWithPosTerminal(@Nullable SaleType saleType, String param) {
        presenter.onPaymentWithPosShouldBeCanceled(param);
    }

    @Override
    public void printPdSuccess(long newPdId, BigDecimal pdCost) {
        presenter.onPdSuccessfullyPrinted(newPdId, pdCost);
    }

    @Override
    public void onCancelSellProcess() {
        presenter.onCancelSellProcess();
    }

    @Override
    public void onActionSuccess(@Nullable SaleType saleType) {
        if (workWithPosWasStartedForSale) {
            presenter.onCardPaymentSuccess();
        } else {
            throw new IllegalStateException("Method should not be called");
        }
    }

    @Override
    public void onActionFail(@Nullable SaleType saleType) {
        if (workWithPosWasStartedForSale) {
            presenter.onCardPaymentFailed();
        } else {
            presenter.onCancelCardPaymentFailed();
        }
    }

    @Override
    public void onStartNewSellProcess() {
        presenter.onSellAnotherPdClick();
    }

    @Override
    public void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        FragmentManager manager = getFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.fragmentContainer);

        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;
            if (fragmentOnBackPressed.onBackPress()) {
                Logger.info(TAG, "onBackPressed, currentFragment return true");
                return;
            }
        }

        if (manager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            manager.popBackStack();
        }
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int idDialog) {
        if (onCancelBTOperationDialogClickListener != null)
            onCancelBTOperationDialogClickListener.onOk(dialog);
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int idDialog) {
        if (onCancelBTOperationDialogClickListener != null)
            onCancelBTOperationDialogClickListener.onNope(dialog);
    }

    private ExtraPaymentPresenter.InteractionListener extraPaymentInteractionListener = new ExtraPaymentPresenter.InteractionListener() {
        @Override
        public void transferDataToChild(ExtraPaymentParams extraPaymentParams, DataSalePD dataSalePd) {
            ExtraPaymentExecutionFragment fragment = ExtraPaymentExecutionFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, ExtraPaymentExecutionFragment.FRAGMENT_TAG)
                    .commit();
            extraPaymentExecutionFragment = fragment;
            extraPaymentExecutionFragment.setInteractionListener(extraPaymentExecutionInteractionListener);
            extraPaymentExecutionFragment.init(getMvpDelegate(), MVP_PAYMENT_EXECUTION_FRAGMENT);
            extraPaymentExecutionFragment.initialize(Dagger.appComponent().resources(), extraPaymentParams, dataSalePd);
        }

        @Override
        public void navigateToPrintPd() {
            if (getFragmentManager().findFragmentByTag(ExtraPaymentPrintFragment.FRAGMENT_TAG) != null) {
                Logger.trace(TAG, "navigateToPrintPd, pop fragment from back stack");
                getFragmentManager().popBackStack();
            } else {
                Logger.trace(TAG, "navigateToPrintPd, create new fragment");
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, ExtraPaymentPrintFragment.newInstance(), ExtraPaymentPrintFragment.FRAGMENT_TAG)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @Override
        public void navigateToPayWithPos(SaleType saleType, String price) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, FragmentWorkWithPosTerminal.newInstance(saleType, price), FragmentWorkWithPosTerminal.TAG)
                    .addToBackStack(null)
                    .commit();
            workWithPosWasStartedForSale = true;
        }

        @Override
        public void navigateToCancelCardPayment(int transactionId) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, FragmentWorkWithPosTerminal.newInstance(transactionId), FragmentWorkWithPosTerminal.TAG)
                    .addToBackStack(null)
                    .commit();
            workWithPosWasStartedForSale = false;
        }

        @Override
        public void navigateToSellPdSuccess(PdSaleSuccessParams pdSaleSuccessParams) {
            Navigator.navigateToSellPdSuccessActivity(ExtraPaymentActivity.this, pdSaleSuccessParams);
            finish();
        }

        @Override
        public void navigateToNewSale() {
            PdSaleParams pdSaleParams = new PdSaleParams();
            pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
            pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
            Navigator.navigateToPdSaleActivity(ExtraPaymentActivity.this, pdSaleParams);
            finish();
        }

        @Override
        public void navigateToExecution() {
            FragmentManager.BackStackEntry first = getFragmentManager().getBackStackEntryAt(0);
            getFragmentManager().popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    };

    private ExtraPaymentExecutionFragment.InteractionListener extraPaymentExecutionInteractionListener = new ExtraPaymentExecutionFragment.InteractionListener() {
        @Override
        public void onSellBtnClick() {
            presenter.onSellBtnClick();
        }

        @Override
        public void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams) {
            RemoveExemptionFragment fragment = RemoveExemptionFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, ExtraPaymentExecutionFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            fragment.setInteractionListener(removeExemptionInteractionListener);
            fragment.init(getMvpDelegate(), MVP_REMOVE_EXEMPTION);
            fragment.initialize(removeExemptionParams);
        }

        @Override
        public void navigateToCloseShiftActivity() {
            Navigator.navigateToCloseShiftActivity(ExtraPaymentActivity.this, true, true);
        }

        @Override
        public void navigateBack() {
            finish();
        }
    };

    private RemoveExemptionFragment.InteractionListener removeExemptionInteractionListener = new RemoveExemptionFragment.InteractionListener() {
        @Override
        public void navigateToPreviousScreen(boolean exemptionRemoved) {
            getFragmentManager().popBackStack();
            if (exemptionRemoved) {
                extraPaymentExecutionFragment.onExemptionRemoved();
            }
        }
    };
}
