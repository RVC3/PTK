package ru.ppr.cppk.ui.fragment.printFineCheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.math.BigDecimal;
import java.util.EnumSet;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.helpers.lifecycle.LifecycleActionHandler;
import ru.ppr.cppk.helpers.lifecycle.LifecycleEvent;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.logger.Logger;

/**
 * Экран печати штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class PrintFineCheckFragment extends MvpFragment implements PrintFineCheckView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PrintFineCheckFragment.class);
    public static final String FRAGMENT_TAG = PrintFineCheckFragment.class.getSimpleName();
    // RC
    private static final int RC_ACTIVATE_EKLZ = 101;
    private static final int RC_SET_TICKET_TAPE = 102;

    public static PrintFineCheckFragment newInstance() {
        return new PrintFineCheckFragment();
    }

    // region Di
    private PrintFineCheckComponent component;
    // endregion
    private InteractionListener mInteractionListener;

    //region Views
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private PrintFineCheckPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        PrintFineCheckSharedComponent sharedComponent = ((HasPrintFineCheckSharedComponent) getActivity()).getPrintFineCheckSharedComponent();
        component = sharedComponent.printFineCheckComponent();
        super.onCreate(savedInstanceState);

        presenter = getMvpDelegate().getPresenter(component::printFineCheckPresenter, PrintFineCheckPresenter.class);
        presenter.bindInteractionListener(printFineCheckInteractionListener);
        presenter.initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_print_fine_check, container, false);
        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SET_TICKET_TAPE:
                presenter.onSetTicketTapeFinished();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public boolean onBackPress() {
        presenter.onBackPressed();
        return true;
    }

    @Override
    public void showPrintingState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.print_fine_check_printing_msg);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintSuccessState(boolean withCalculateDeliveryBtn) {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_SUCCESS);
        stateBuilder.setTextMessage(R.string.print_fine_check_success_msg);
        if (withCalculateDeliveryBtn) {
            stateBuilder.setButton1(R.string.print_fine_check_calculate_delivery, v -> presenter.onCalculateDeliveryBtnClicked());
        }
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintFailState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.print_fine_check_fail_msg);
        stateBuilder.setButton1(R.string.print_fine_check_fail_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.print_fine_check_fail_cancel_btn, v -> presenter.onReturnMoneyBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showNeedActivateEklzState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.print_fine_check_eklz_activation_required_msg);
        stateBuilder.setButton1(R.string.print_fine_check_eklz_activation_start, v -> presenter.onStartEklzActivationBtnClicked());
        stateBuilder.setButton2(R.string.print_fine_check_eklz_activation_cancel, v -> presenter.onCancelEklzActivationBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintingFailedAndCheckInFrState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.print_fine_check_fine_created_print_fail_msg);
        stateBuilder.setButton1(R.string.print_fine_check_fine_created_print_fail_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.print_fine_check_fine_created_print_fail_cancel_btn, v -> presenter.onCancelRePrintBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showShiftTimeOutError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.print_fine_check_shift_timeout_msg);
        stateBuilder.setButton1(R.string.print_fine_check_shift_timeout_close_btn, v -> presenter.onStartCloseShiftBtnClicked());
        stateBuilder.setButton2(R.string.print_fine_check_shift_timeout_cancel_btn, v -> presenter.onCancelCloseShiftBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showIncorrectFrStateError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.print_fine_check_incorrect_fr_state_error_msg);
        stateBuilder.setButton1(R.string.print_fine_check_incorrect_fr_state_error_close_btn, v -> presenter.onStartCloseShiftBtnClicked());
        stateBuilder.setButton2(R.string.print_fine_check_incorrect_fr_state_error_cancel_btn, v -> presenter.onCancelCloseShiftBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showReturnMoneyConfirmationDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.print_fine_check_return_money_confirm_msg),
                getString(R.string.print_fine_check_return_money_yes),
                getString(R.string.print_fine_check_return_money_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            presenter.onReturnMoneyConfirmClicked();
        });
    }

    private PrintFineCheckPresenter.InteractionListener printFineCheckInteractionListener = new PrintFineCheckPresenter.InteractionListener() {

        private final LifecycleActionHandler lifecycleActionHandler = LifecycleActionHandler.newStartStopInstance();

        {
            addLifecycleListener(lifecycleActionHandler, EnumSet.of(LifecycleEvent.ON_START, LifecycleEvent.ON_STOP));
        }

        @Override
        public void navigateToTicketTapeIsNotSet() {
            lifecycleActionHandler.post(() -> Navigator.navigateToTicketTapeIsNotSetActivity(getActivity(), PrintFineCheckFragment.this, RC_SET_TICKET_TAPE));
        }

        @Override
        public void navigateToActivateEklz() {
            lifecycleActionHandler.post(() -> Navigator.navigateToSettingsPrinterActivity(getActivity(), PrintFineCheckFragment.this, RC_ACTIVATE_EKLZ));
        }

        @Override
        public void navigateToCloseShift() {
            lifecycleActionHandler.post(() -> Navigator.navigateToCloseShiftActivity(getActivity(), true, false));
        }

        @Override
        public void onReturnMoneyRequired() {
            mInteractionListener.onReturnMoneyRequired();
        }

        @Override
        public void navigateToCalculateDelivery(BigDecimal amount) {
            mInteractionListener.navigateToCalculateDelivery(amount);
        }

        @Override
        public void closeScreen() {
            mInteractionListener.closeScreen();
        }

        @Override
        public void onOperationCanceled() {
            mInteractionListener.onOperationCanceled();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onReturnMoneyRequired();

        void navigateToCalculateDelivery(BigDecimal amount);

        void onOperationCanceled();

        void closeScreen();
    }
}
