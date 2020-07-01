package ru.ppr.cppk.ui.fragment.fineSalePreparation;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.Fine;

/**
 * @author Aleksandr Brazhkin
 */
interface FineSalePreparationView extends MvpView {

    void setFines(List<Fine> fines);

    void setRegion(String regionName);

    void setCost(BigDecimal cost);

    void setSendETicketBtnVisible(boolean visible);

    void setPaymentTypeVisible(boolean visible);

    void setCostGroupVisible(boolean visible);

    void setNoFinesAvailableDialogVisible(boolean visible);

    void setReallyWantFineDialogVisible(boolean visible, String fineName, BigDecimal fineCost);

    void setCriticalNsiBackDialogVisible(boolean visible);

    void setCriticalNsiCloseShiftDialogVisible(boolean visible);
}
