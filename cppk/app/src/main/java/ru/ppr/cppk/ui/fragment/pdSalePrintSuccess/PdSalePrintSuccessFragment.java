package ru.ppr.cppk.ui.fragment.pdSalePrintSuccess;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.logger.Logger;

/**
 * Экран успешной печати одного из серии ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSalePrintSuccessFragment extends LegacyMvpFragment implements PdSalePrintSuccessView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PdSalePrintSuccessFragment.class);
    public static final String FRAGMENT_TAG = PdSalePrintSuccessFragment.class.getSimpleName();

    public static PdSalePrintSuccessFragment newInstance() {
        return new PdSalePrintSuccessFragment();
    }

    /**
     * Di
     */
    private final PdSalePrintSuccessDi di = new PdSalePrintSuccessDi(di());
    private InteractionListener mInteractionListener;

    //Views
    SimpleLseView simpleLseView;
    //region Other
    private PdSalePrintSuccessPresenter presenter;
    //endregion

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_sale_print_success, container, false);
        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_SUCCESS);
        stateBuilder.setTextMessage(R.string.pd_sale_print_success_msg);
        stateBuilder.setButton1(R.string.pd_sale_print_success_print_next_btn, v -> presenter.onPrintNextPdBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();

        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(PdSalePrintSuccessPresenter::new, PdSalePrintSuccessPresenter.class);
    }

    public void initialize() {
        presenter.bindInteractionListener(pdSalePrintSuccessInteractionListener);
        presenter.initialize();
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    private PdSalePrintSuccessPresenter.InteractionListener pdSalePrintSuccessInteractionListener = new PdSalePrintSuccessPresenter.InteractionListener() {

        @Override
        public void onPrintNextPdBtnClicked() {
            mInteractionListener.onPrintNextPdBtnClicked();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onPrintNextPdBtnClicked();
    }
}
