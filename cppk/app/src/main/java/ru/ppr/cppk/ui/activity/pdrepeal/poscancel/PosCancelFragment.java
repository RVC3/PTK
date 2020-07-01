package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.activity.pdrepeal.PdRepealActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.logger.Logger;

/**
 * Экран отмены банковской транзакции ьез печати слипов.
 *
 * @author Aleksandr Brazhkin
 */
public class PosCancelFragment extends MvpFragment implements PosCancelView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PosCancelFragment.class);

    // ARGS
    private static final String BANK_TRANSACTION_EVENT_ID = "BANK_TRANSACTION_EVENT_ID";

    public static PosCancelFragment newInstance(long bankTransactionEventId) {
        PosCancelFragment fragment = new PosCancelFragment();
        Bundle args = new Bundle();
        args.putLong(BANK_TRANSACTION_EVENT_ID, bankTransactionEventId);
        fragment.setArguments(args);
        return fragment;
    }

    // region Di
    private PosCancelComponent component;
    // endregion
    //region Views
    private SimpleLseView simpleLseView;
    private TextView timerView;
    //endregion
    //region Other
    private PosCancelPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = ((PdRepealActivity) getActivity()).getComponent().posCancelComponentBuilder()
                .saleTransactionEventId(getArguments().getLong(BANK_TRANSACTION_EVENT_ID))
                .build();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::posCancelPresenter, PosCancelPresenter.class);
        presenter.initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_repeal_pos_cancel, container, false);

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        timerView = (TextView) view.findViewById(R.id.timerView);

        return view;
    }

    @Override
    public void showConnectingState(long timeout) {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_connecting);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        timerView.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeout)));
        timerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showConnectionTimeoutState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_no_connection);
        stateBuilder.setButton1(R.string.pd_repeal_pos_cancel_no_connection_cancel_btn, v -> presenter.onNotConnectedCancelBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        timerView.setVisibility(View.GONE);
    }

    @Override
    public void showConnectedState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.pd_repeal_pos_cancel_processing);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        timerView.setVisibility(View.GONE);
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

}
