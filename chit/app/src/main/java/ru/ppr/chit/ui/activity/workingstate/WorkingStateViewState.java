package ru.ppr.chit.ui.activity.workingstate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class WorkingStateViewState extends BaseMvpViewState<WorkingStateView> implements WorkingStateView {

    private Long terminalId;
    private String wifiSsid;
    private String bsId;
    private String softwareVersion = "";
    private Version version;
    private boolean forceSyncVisible;
    private boolean bsConnectVisible;
    private boolean syncProgressVisible;
    private LastExchangeInfo lastExchangeInfo;
    private int notExported;
    private String errorMessage;

    @Inject
    WorkingStateViewState() {

    }

    @Override
    protected void onViewAttached(WorkingStateView view) {
        view.setTerminalId(terminalId);
        view.setWifiSsid(wifiSsid);
        view.setBsId(bsId);
        view.setSoftwareVersion(softwareVersion);
        view.setNsiVersion(version);
        view.setForceSyncVisible(forceSyncVisible);
        view.setBsConnectVisible(bsConnectVisible);
        view.setSyncProgressVisible(syncProgressVisible);
        view.setLastExchangeInfo(lastExchangeInfo);
        view.setNotExported(notExported);
        // Если было отложенное сообщение, то выводим его и обнуляем
        if (errorMessage != null){
            view.setSyncErrorMessage(errorMessage);
            errorMessage = null;
        }
    }

    @Override
    protected void onViewDetached(WorkingStateView view) {

    }

    @Override
    public void setTerminalId(@Nullable Long terminalId) {
        this.terminalId = terminalId;
        forEachView(view -> view.setTerminalId(this.terminalId));
    }

    @Override
    public void setWifiSsid(@Nullable String wifiSsid) {
        this.wifiSsid = wifiSsid;
        forEachView(view -> view.setWifiSsid(this.wifiSsid));
    }

    @Override
    public void setBsId(@Nullable String bsId) {
        this.bsId = bsId;
        forEachView(view -> view.setBsId(this.bsId));
    }

    @Override
    public void setSoftwareVersion(@NonNull String softwareVersion) {
        this.softwareVersion = softwareVersion;
        forEachView(view -> view.setSoftwareVersion(this.softwareVersion));
    }

    @Override
    public void setNsiVersion(@Nullable Version version) {
        this.version = version;
        forEachView(view -> view.setNsiVersion(this.version));
    }

    @Override
    public void setForceSyncVisible(boolean visible) {
        this.forceSyncVisible = visible;
        forEachView(view -> view.setForceSyncVisible(this.forceSyncVisible));
    }

    @Override
    public void setBsConnectVisible(boolean visible) {
        this.bsConnectVisible = visible;
        forEachView(view -> view.setBsConnectVisible(this.bsConnectVisible));
    }

    @Override
    public void setSyncProgressVisible(boolean visible) {
        this.syncProgressVisible = visible;
        forEachView(view -> view.setSyncProgressVisible(this.syncProgressVisible));
    }

    @Override
    public void setSyncProgressMessage(String message) {
        forEachView(view -> view.setSyncProgressMessage(message));
    }

    @Override
    public void setSyncErrorMessage(String message) {
        // Выводим сообщение в привязанные view
        forEachView(view -> view.setSyncErrorMessage(message));
        // Если нет активных view, то запоминаем ошибку
        if (!hasView()){
            errorMessage = message;
        }
    }

    @Override
    public void setBsConnectBrokenError(String message){
        forEachView(view -> view.setBsConnectBrokenError(message));
    }

    @Override
    public void setLastExchangeInfo(@Nullable LastExchangeInfo lastExchangeInfo) {
        this.lastExchangeInfo = lastExchangeInfo;
        forEachView(view -> view.setLastExchangeInfo(this.lastExchangeInfo));
    }

    @Override
    public void setNotExported(int notExported) {
        this.notExported = notExported;
        forEachView(view -> view.setNotExported(this.notExported));
    }

}
