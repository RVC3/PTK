package ru.ppr.cppk.ui.fragment.fineSalePreparation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.nsi.entity.Fine;

/**
 * @author Aleksandr Brazhkin
 */
class FineSalePreparationViewState extends BaseMvpViewState<FineSalePreparationView> implements FineSalePreparationView {

    private List<Fine> mFines = Collections.emptyList();
    private String mRegionName;
    private BigDecimal mCost;
    private boolean mETicketBtnVisible;
    private boolean mPaymentTypeVisible = false;
    private boolean mCostGroupVisible = false;
    private boolean noFinesAvailableDialogVisible = false;
    private boolean reallyWantFineDialogVisible = false;
    private String reallyWantFineDialogFineName;
    private BigDecimal reallyWantFineDialogFineCost;
    private boolean criticalNsiBackDialogVisible;
    private boolean criticalNsiCloseShiftDialogVisible;

    @Override
    protected void onViewAttached(FineSalePreparationView view) {
        view.setFines(mFines);
        view.setRegion(mRegionName);
        view.setCost(mCost);
        view.setSendETicketBtnVisible(mETicketBtnVisible);
        view.setPaymentTypeVisible(mPaymentTypeVisible);
        view.setCostGroupVisible(mCostGroupVisible);
        view.setNoFinesAvailableDialogVisible(noFinesAvailableDialogVisible);
        view.setReallyWantFineDialogVisible(reallyWantFineDialogVisible, reallyWantFineDialogFineName, reallyWantFineDialogFineCost);
        view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible);
        view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible);
    }

    @Override
    protected void onViewDetached(FineSalePreparationView view) {

    }

    @Override
    public void setFines(List<Fine> fines) {
        mFines = fines;
        forEachView(view -> view.setFines(mFines));
    }

    @Override
    public void setRegion(String regionName) {
        mRegionName = regionName;
        forEachView(view -> view.setRegion(mRegionName));
    }

    @Override
    public void setCost(BigDecimal cost) {
        mCost = cost;
        forEachView(view -> view.setCost(mCost));
    }

    @Override
    public void setSendETicketBtnVisible(boolean visible) {
        mETicketBtnVisible = visible;
        forEachView(view -> view.setSendETicketBtnVisible(mETicketBtnVisible));
    }

    @Override
    public void setPaymentTypeVisible(boolean visible) {
        mPaymentTypeVisible = visible;
        forEachView(view -> view.setPaymentTypeVisible(mPaymentTypeVisible));
    }

    @Override
    public void setCostGroupVisible(boolean visible) {
        mCostGroupVisible = visible;
        forEachView(view -> view.setCostGroupVisible(mCostGroupVisible));
    }

    @Override
    public void setReallyWantFineDialogVisible(boolean visible, String fineName, BigDecimal fineCost) {
        reallyWantFineDialogVisible = visible;
        reallyWantFineDialogFineName = fineName;
        reallyWantFineDialogFineCost = fineCost;
        forEachView(view -> view.setReallyWantFineDialogVisible(reallyWantFineDialogVisible, reallyWantFineDialogFineName, reallyWantFineDialogFineCost));
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        criticalNsiBackDialogVisible = visible;
        forEachView(view -> view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible));
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        criticalNsiCloseShiftDialogVisible = visible;
        forEachView(view -> view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible));
    }

    @Override
    public void setNoFinesAvailableDialogVisible(boolean visible) {
        noFinesAvailableDialogVisible = visible;
        forEachView(view -> view.setNoFinesAvailableDialogVisible(noFinesAvailableDialogVisible));
    }

}
