package ru.ppr.cppk.ui.activity.pdrepeal;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class PdRepealViewState extends BaseMvpViewState<PdRepealView> implements PdRepealView {

    private boolean abortedStateVisible = false;
    private String message = null;
    private boolean initializingStateVisible = false;

    @Inject
    PdRepealViewState() {

    }

    @Override
    protected void onViewAttached(PdRepealView view) {
        if (this.abortedStateVisible)
            view.showAbortedState(this.message);
        else
            hideAbortedState();
    }

    @Override
    protected void onViewDetached(PdRepealView view) {

    }

    @Override
    public void showAbortedState(String message) {
        this.abortedStateVisible = true;
        this.message = message;
        forEachView(view -> view.showAbortedState(message));
    }

    @Override
    public void hideAbortedState() {
        this.abortedStateVisible = false;
        forEachView(view -> view.hideAbortedState());
    }

    @Override
    public void setInitializingStateVisible(boolean visible) {
        this.initializingStateVisible = visible;
        forEachView(view -> view.setInitializingStateVisible(this.initializingStateVisible));
    }
}
