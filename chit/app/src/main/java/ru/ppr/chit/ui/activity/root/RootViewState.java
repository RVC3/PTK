package ru.ppr.chit.ui.activity.root;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class RootViewState extends BaseMvpViewState<RootView> implements RootView {

    private List<EdsType> edsTypeList = Collections.emptyList();
    private int edsTypeListSelection = -1;
    private List<BarcodeType> barcodeTypeList = Collections.emptyList();
    private int barcodeTypeListSelection = -1;
    private List<RfidType> rfidTypeList = Collections.emptyList();
    private int rfidTypeListSelection = -1;
    private boolean createFullBackupProgressVisible;
    private boolean createLogsBackupProgressVisible;
    private boolean createDbsBackupProgressVisible;
    private boolean restoreFullBackupProgressVisible;
    private boolean restoreDbsBackupProgressVisible;

    @Inject
    RootViewState() {

    }

    @Override
    protected void onViewAttached(RootView view) {
        view.setEdsTypeList(edsTypeList);
        view.setEdsTypeListSelection(edsTypeListSelection);
        view.setBarcodeTypeList(barcodeTypeList);
        view.setBarcodeTypeListSelection(barcodeTypeListSelection);
        view.setRfidTypeList(rfidTypeList);
        view.setRfidTypeListSelection(rfidTypeListSelection);
        view.setCreateFullBackupProgressVisible(createFullBackupProgressVisible);
        view.setCreateLogsBackupProgressVisible(createLogsBackupProgressVisible);
        view.setCreateDbsBackupProgressVisible(createDbsBackupProgressVisible);
        view.setRestoreFullBackupProgressVisible(restoreFullBackupProgressVisible);
        view.setRestoreDbsBackupProgressVisible(restoreDbsBackupProgressVisible);
    }

    @Override
    protected void onViewDetached(RootView view) {

    }

    @Override
    public void setCreateFullBackupProgressVisible(boolean visible) {
        this.createFullBackupProgressVisible = visible;
        forEachView(view -> view.setCreateFullBackupProgressVisible(this.createFullBackupProgressVisible));
    }

    @Override
    public void setCreateLogsBackupProgressVisible(boolean visible) {
        this.createLogsBackupProgressVisible = visible;
        forEachView(view -> view.setCreateLogsBackupProgressVisible(this.createLogsBackupProgressVisible));
    }

    @Override
    public void setCreateDbsBackupProgressVisible(boolean visible) {
        this.createDbsBackupProgressVisible = visible;
        forEachView(view -> view.setCreateDbsBackupProgressVisible(this.createDbsBackupProgressVisible));
    }

    @Override
    public void setRestoreFullBackupProgressVisible(boolean visible) {
        this.restoreFullBackupProgressVisible = visible;
        forEachView(view -> view.setRestoreFullBackupProgressVisible(this.restoreFullBackupProgressVisible));
    }

    @Override
    public void setRestoreDbsBackupProgressVisible(boolean visible) {
        this.restoreDbsBackupProgressVisible = visible;
        forEachView(view -> view.setRestoreDbsBackupProgressVisible(this.restoreDbsBackupProgressVisible));
    }

    @Override
    public void setEdsTypeList(List<EdsType> edsTypeList) {
        this.edsTypeList = edsTypeList;
        forEachView(view -> view.setEdsTypeList(this.edsTypeList));
    }

    @Override
    public void setEdsTypeListSelection(int selection) {
        this.edsTypeListSelection = selection;
        forEachView(view -> view.setEdsTypeListSelection(this.edsTypeListSelection));
    }

    @Override
    public void setBarcodeTypeList(List<BarcodeType> barcodeTypeList) {
        this.barcodeTypeList = barcodeTypeList;
        forEachView(view -> view.setBarcodeTypeList(this.barcodeTypeList));
    }

    @Override
    public void setBarcodeTypeListSelection(int selection) {
        this.barcodeTypeListSelection = selection;
        forEachView(view -> view.setBarcodeTypeListSelection(this.barcodeTypeListSelection));
    }

    @Override
    public void setRfidTypeList(List<RfidType> rfidTypeList) {
        this.rfidTypeList = rfidTypeList;
        forEachView(view -> view.setRfidTypeList(this.rfidTypeList));
    }

    @Override
    public void setRfidTypeListSelection(int selection) {
        this.rfidTypeListSelection = selection;
        forEachView(view -> view.setRfidTypeListSelection(this.rfidTypeListSelection));
    }

}
