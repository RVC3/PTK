package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;

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
 * Экран оплаты банковской картой.
 *
 * @author Aleksandr Brazhkin
 */
public class PosCancelPrintSlipFragment extends MvpFragment implements PosCancelPrintSlipView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PosCancelPrintSlipFragment.class);

    // RC
    private static final int RC_SET_TICKET_TAPE = 102;

    public static PosCancelPrintSlipFragment newInstance() {
        return new PosCancelPrintSlipFragment();
    }

    // region Di
    private PosCancelPrintSlipComponent component;
    // endregion
    //region Views
    private SimpleLseView simpleLseView;
    //endregion
    //region Other
    private PosCancelPrintSlipPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = ((PdRepealActivity) getActivity()).getComponent().posCancelPrintSlipComponent();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::posCancelPrintSlipPresenter, PosCancelPrintSlipPresenter.class);
        presenter.bindNavigator(posCancelPrintSlipNavigator);
        presenter.initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_repeal_pos_cancel_print_slip, container, false);
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
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_print_slip_printing);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showFailState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_print_slip_fail_msg);
        stateBuilder.setButton1(R.string.pd_repeal_pos_cancel_print_slip_fail_repeat_btn, v -> presenter.onFailRepeatBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_pos_cancel_print_slip_fail_abort_btn, v -> presenter.onFailAbortBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showSuccessState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_QUESTION);
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_print_slip_success_msg);
        stateBuilder.setButton1(R.string.pd_repeal_pos_cancel_print_slip_success_repeat_btn, v -> presenter.onSuccessRepeatBtnClicked());
        stateBuilder.setButton2(R.string.pd_repeal_pos_cancel_print_slip_success_continue_btn, v -> presenter.onSuccessContinueBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();

    }

    private PosCancelPrintSlipPresenter.Navigator posCancelPrintSlipNavigator = new PosCancelPrintSlipPresenter.Navigator() {

        private final LifecycleActionHandler lifecycleActionHandler = LifecycleActionHandler.newStartStopInstance();

        {
            addLifecycleListener(lifecycleActionHandler, EnumSet.of(LifecycleEvent.ON_START, LifecycleEvent.ON_STOP));
        }

        @Override
        public void navigateToTicketTapeIsNotSet() {
            lifecycleActionHandler.post(() -> Navigator.navigateToTicketTapeIsNotSetActivity(getActivity(), PosCancelPrintSlipFragment.this, RC_SET_TICKET_TAPE));
        }

    };
}
