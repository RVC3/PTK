package ru.ppr.cppk.ui.fragment.cashPaymentCancel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.logger.Logger;

/**
 * Экран возврата наличных.
 *
 * @author Aleksandr Brazhkin
 */
public class CashPaymentCancelFragment extends LegacyMvpFragment implements CashPaymentCancelView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(CashPaymentCancelFragment.class);
    public static final String FRAGMENT_TAG = CashPaymentCancelFragment.class.getSimpleName();

    public static CashPaymentCancelFragment newInstance() {
        return new CashPaymentCancelFragment();
    }

    /**
     * Di
     */
    private final CashPaymentCancelDi di = new CashPaymentCancelDi(di());
    private InteractionListener mInteractionListener;

    //Views
    TextView amountView;
    Button continueBtn;
    //region Other
    private CashPaymentCancelPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_payment_cancel, container, false);
        amountView = (TextView) view.findViewById(R.id.amount);
        continueBtn = (Button) view.findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> presenter.onContinueBtnClicked());
        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(CashPaymentCancelPresenter::new, CashPaymentCancelPresenter.class);
    }

    public void initialize(BigDecimal amount) {
        presenter.bindInteractionListener(cashPaymentCancelInteractionListener);
        presenter.initialize(amount);
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        amountView.setText(amount == null ? "" : getString(R.string.cash_payment_cancel_amount, amount));
    }

    private CashPaymentCancelPresenter.InteractionListener cashPaymentCancelInteractionListener = new CashPaymentCancelPresenter.InteractionListener() {

        @Override
        public void onOperationFinished() {
            mInteractionListener.onOperationFinished();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onOperationFinished();
    }
}
