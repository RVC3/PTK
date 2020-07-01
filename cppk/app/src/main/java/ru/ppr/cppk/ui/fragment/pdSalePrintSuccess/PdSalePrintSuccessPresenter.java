package ru.ppr.cppk.ui.fragment.pdSalePrintSuccess;

import android.support.annotation.NonNull;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSalePrintSuccessPresenter extends BaseMvpViewStatePresenter<PdSalePrintSuccessView, PdSalePrintSuccessViewState> {

    private static final String TAG = Logger.makeLogTag(PdSalePrintSuccessPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;

    public PdSalePrintSuccessPresenter() {

    }

    @Override
    protected PdSalePrintSuccessViewState provideViewState() {
        return new PdSalePrintSuccessViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize() {
        if (!mInitialized) {
            mInitialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
    }

    void onPrintNextPdBtnClicked() {
        interactionListener.onPrintNextPdBtnClicked();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onPrintNextPdBtnClicked();
    }

}
