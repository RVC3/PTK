package ru.ppr.chit.ui.activity.rootaccess;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class RootAccessViewState extends BaseMvpViewState<RootAccessView> implements RootAccessView {

    private boolean errorVisible;

    @Inject
    RootAccessViewState() {

    }

    @Override
    protected void onViewAttached(RootAccessView view) {
        view.setErrorVisible(errorVisible);
    }

    @Override
    protected void onViewDetached(RootAccessView view) {

    }

    @Override
    public void setErrorVisible(boolean visible) {
        this.errorVisible = visible;
        forEachView(view -> view.setErrorVisible(this.errorVisible));
    }

}
