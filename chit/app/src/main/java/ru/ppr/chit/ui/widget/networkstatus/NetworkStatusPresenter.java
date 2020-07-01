package ru.ppr.chit.ui.widget.networkstatus;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class NetworkStatusPresenter extends BaseMvpViewStatePresenter<NetworkStatusView, NetworkStatusViewState> {

    private boolean initialized;

    private final WiFiManager wifiManager;
    private Disposable initializeDisposable = Disposables.disposed();

    @Inject
    NetworkStatusPresenter(NetworkStatusViewState networkStatusViewState, WiFiManager wifiManager) {
        super(networkStatusViewState);
        this.wifiManager = wifiManager;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        initializeDisposable = wifiManager
                .getWifiAvailablePublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setNetworkAvailable);
    }

    @Override
    public void destroy() {
        initializeDisposable.dispose();
        super.destroy();
    }

}
