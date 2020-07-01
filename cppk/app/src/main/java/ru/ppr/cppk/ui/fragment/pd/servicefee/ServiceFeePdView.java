package ru.ppr.cppk.ui.fragment.pd.servicefee;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.ui.fragment.pd.simple.model.ServicePdViewModel;

/**
 * @author Dmitry Nevolin
 */
interface ServiceFeePdView extends MvpView {

    void updatePdTitle(@Nullable String pdTitle);

    void updatePdValid(boolean isPdValid);

    void updatePdNumber(@Nullable Integer pdNumber);

    void updatePdErrors(@NonNull List<PassageResult> pdErrors);

    void updateDateActionsFrom(@Nullable Date from);

    void updateDateActionsTo(@Nullable Date to);

    /**
     * Метод на случай если serviceFee == null (не найден)
     * @param visible флаг видимости
     */
    void updateServiceFeeNotFound(boolean visible);

    void showZoomPdDialog(ServicePdViewModel pdViewModel);
}
