package ru.ppr.chit.ui.widget.networkstatus;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class NetworkStatusViewState extends BaseMvpViewState<NetworkStatusView> implements NetworkStatusView {

    private boolean networkAvailable;

    @Inject
    NetworkStatusViewState() {

    }

    @Override
    protected void onViewAttached(NetworkStatusView view) {
        view.setNetworkAvailable(networkAvailable);
    }

    @Override
    protected void onViewDetached(NetworkStatusView view) {

    }

    @Override
    public void setNetworkAvailable(boolean networkAvailable){
        this.networkAvailable = networkAvailable;
        forEachView(view -> view.setNetworkAvailable(this.networkAvailable));
    }

}
