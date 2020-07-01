package ru.ppr.chit.ui.widget.syncstatus;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class SyncStatusPresenter extends BaseMvpViewStatePresenter<SyncStatusView, SyncStatusViewState> {

    private boolean initialized;

    @Inject
    SyncStatusPresenter(SyncStatusViewState syncStatusViewState) {
        super(syncStatusViewState);
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
