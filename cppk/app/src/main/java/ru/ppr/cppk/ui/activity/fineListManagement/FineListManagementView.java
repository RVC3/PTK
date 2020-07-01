package ru.ppr.cppk.ui.activity.fineListManagement;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.Fine;

/**
 * @author Dmitry Nevolin
 */
interface FineListManagementView extends MvpView {

    void setAllowedFineCodeList(@NonNull List<Long> allowedFineCodeList);

    void setFineList(@NonNull List<Fine> fineList);

    void setFineListIsEmptyDialogVisible(boolean visible);

}
