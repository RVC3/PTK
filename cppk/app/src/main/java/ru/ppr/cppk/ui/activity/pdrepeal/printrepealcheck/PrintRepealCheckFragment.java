package ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.EnumSet;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.helpers.lifecycle.LifecycleActionHandler;
import ru.ppr.cppk.helpers.lifecycle.LifecycleEvent;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.pdrepeal.PdRepealActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.logger.Logger;

/**
 * Экран печати штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class PrintRepealCheckFragment extends MvpFragment implements PrintRepealCheckView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PrintRepealCheckFragment.class);
    public static final String FRAGMENT_TAG = PrintRepealCheckFragment.class.getSimpleName();
    // RC
    private static final int RC_ACTIVATE_EKLZ = 101;
    private static final int RC_SET_TICKET_TAPE = 102;

    public static PrintRepealCheckFragment newInstance() {
        return new PrintRepealCheckFragment();
    }

    // region Di
    private PrintRepealCheckComponent component;
    // endregion

    //region Views
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private PrintRepealCheckPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = ((PdRepealActivity) getActivity()).getComponent().printRepealCheckComponent();
        super.onCreate(savedInstanceState);

        presenter = getMvpDelegate().getPresenter(component::printRepealCheckPresenter, PrintRepealCheckPresenter.class);
        presenter.bindInteractionListener(printFineCheckInteractionListener);
        presenter.initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_print_repeal_check, container, false);
        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        return view;
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
        return true;
    }

    @Override
    public void showPrintingState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_printing_msg);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintSuccessState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_SUCCESS);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_success_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_success_btn_ok, v -> presenter.onSuccessOkBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintFailState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_fail_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_btn_repeat, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_print_repeal_check_btn_cancel, v -> presenter.onFailCancelBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showNeedActivateEklzState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_eklz_activation_required_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_eklz_activation_start, v -> presenter.onStartEklzActivationBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_print_repeal_check_eklz_activation_cancel, v -> presenter.onCancelEklzActivationBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showPrintingFailedAndCheckInFrState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_repeal_created_print_fail_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_repeal_created_print_fail_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_print_repeal_check_repeal_created_print_fail_cancel_btn, v -> presenter.onCancelRePrintBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showShiftTimeOutError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_shift_timeout_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_shift_timeout_close_btn, v -> presenter.onStartCloseShiftBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_print_repeal_check_shift_timeout_cancel_btn, v -> presenter.onCancelCloseShiftBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showIncorrectFrStateError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_print_repeal_check_incorrect_fr_state_msg);
        stateBuilder.setButton1(R.string.pd_repeal_print_repeal_check_incorrect_fr_state_close_btn, v -> presenter.onStartCloseShiftBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_print_repeal_check_incorrect_fr_state_cancel_btn, v -> presenter.onCancelCloseShiftBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }


    private PrintRepealCheckPresenter.InteractionListener printFineCheckInteractionListener = new PrintRepealCheckPresenter.InteractionListener() {

        private final LifecycleActionHandler lifecycleActionHandler = LifecycleActionHandler.newStartStopInstance();

        {
            addLifecycleListener(lifecycleActionHandler, EnumSet.of(LifecycleEvent.ON_START, LifecycleEvent.ON_STOP));
        }

        @Override
        public void navigateToTicketTapeIsNotSet() {
            lifecycleActionHandler.post(() -> Navigator.navigateToTicketTapeIsNotSetActivity(getActivity(), PrintRepealCheckFragment.this, RC_SET_TICKET_TAPE));
        }

        @Override
        public void navigateToActivateEklz() {
            lifecycleActionHandler.post(() -> Navigator.navigateToSettingsPrinterActivity(getActivity(), PrintRepealCheckFragment.this, RC_ACTIVATE_EKLZ));
        }

        @Override
        public void navigateToCloseShift() {
            lifecycleActionHandler.post(() -> Navigator.navigateToCloseShiftActivity(getActivity(), true, false));
        }

    };
}
