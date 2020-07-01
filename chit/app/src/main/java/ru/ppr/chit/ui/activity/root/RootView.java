package ru.ppr.chit.ui.activity.root;

import java.util.List;

import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface RootView extends MvpView {

    void setCreateFullBackupProgressVisible(boolean visible);

    void setCreateLogsBackupProgressVisible(boolean visible);

    void setCreateDbsBackupProgressVisible(boolean visible);

    void setRestoreFullBackupProgressVisible(boolean visible);

    void setRestoreDbsBackupProgressVisible(boolean visible);

    void setEdsTypeList(List<EdsType> edsTypeList);

    void setEdsTypeListSelection(int selection);

    void setBarcodeTypeList(List<BarcodeType> barcodeTypeList);

    void setBarcodeTypeListSelection(int selection);

    void setRfidTypeList(List<RfidType> rfidTypeList);

    void setRfidTypeListSelection(int selection);

}
