package ru.ppr.cppk.ui.activity.root.ofdsettings;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class OfdSettingsViewState extends BaseMvpViewState<OfdSettingsView> implements OfdSettingsView {

    private String ip;
    private int port;
    private int timeout;
    private State state = State.DEFAULT;
    private boolean progressVisible;

    @Inject
    OfdSettingsViewState() {

    }

    @Override
    protected void onViewAttached(OfdSettingsView view) {
        view.setIp(this.ip);
        view.setPort(this.port);
        view.setTimeout(this.timeout);
        view.setState(this.state);
        if (progressVisible) {
            view.showProgress();
        } else {
            view.hideProgress();
        }
    }

    @Override
    protected void onViewDetached(OfdSettingsView view) {

    }

    @Override
    public void setIp(String ip) {
        this.ip = ip;
        forEachView(view -> view.setIp(this.ip));
    }

    @Override
    public void setPort(int port) {
        this.port = port;
        forEachView(view -> view.setPort(this.port));
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        forEachView(view -> view.setTimeout(this.timeout));
    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(this.state));
    }

    @Override
    public void showProgress() {
        this.progressVisible = true;
        forEachView(OfdSettingsView::showProgress);
    }

    @Override
    public void hideProgress() {
        this.progressVisible = false;
        forEachView(OfdSettingsView::hideProgress);
    }
}
