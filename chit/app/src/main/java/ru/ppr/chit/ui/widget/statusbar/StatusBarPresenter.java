package ru.ppr.chit.ui.widget.statusbar;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class StatusBarPresenter extends BaseMvpViewStatePresenter<StatusBarView, StatusBarViewState> {

    private boolean initialized;

    @Inject
    StatusBarPresenter(StatusBarViewState statusBarViewState) {
        super(statusBarViewState);
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {

    }

}
