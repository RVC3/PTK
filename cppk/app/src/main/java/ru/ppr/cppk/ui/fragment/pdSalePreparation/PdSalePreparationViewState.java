package ru.ppr.cppk.ui.fragment.pdSalePreparation;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Aleksandr Brazhkin
 */
class PdSalePreparationViewState extends BaseMvpViewState<PdSalePreparationView> implements PdSalePreparationView {

    private boolean mETicketBtnVisible;
    private boolean mPaymentTypeVisible = false;
    private boolean mCostGroupVisible = false;
    private FeeLabel mFeeLabel = FeeLabel.IN_TRAIN;
    private TicketWayType mTicketWayType = TicketWayType.OneWay;
    private boolean progressShown = false;
    private List<TariffPlan> mTariffPlans = Collections.emptyList();
    private List<TicketType> mTicketTypes = Collections.emptyList();
    private int mSelectedTariffPlanPosition = -1;
    private int mSelectedTicketTypePosition = -1;
    private PaymentType mPaymentType;
    private BigDecimal mOnePdCost = BigDecimal.ZERO;
    private BigDecimal mTotalCost = BigDecimal.ZERO;
    private boolean mFeeChecked = true;
    private boolean mFeeEnabled = true;
    private BigDecimal mFeeValue = BigDecimal.ZERO;
    private TicketCategoryLabel mOnePdCostLabel = TicketCategoryLabel.SINGLE;
    private TicketCategoryLabel mTitle = TicketCategoryLabel.SINGLE;
    private int mPdCount = 0;
    private List<Station> mDepartureStations = Collections.emptyList();
    private List<Station> mDestinationStations = Collections.emptyList();
    private String mDepartureStationName = "";
    private String mDestinationStationName = "";
    private SaleButtonsState mSaleButtonsState = SaleButtonsState.WRITE_AND_PRINT;
    private boolean mDirectionBtnEnabled = false;
    private boolean mDecrementPdCountBtnEnabled = false;
    private boolean mIncrementPdCountBtnEnable = false;
    private Pair<Integer, Integer> mExemptionCode = new Pair<>(0, 0);
    private boolean mPaymentTypeEnabled = false;
    private boolean mPdCountLayoutEnabled = false;
    private String mTransitStationName = null;
    private boolean departureStationEnabled = true;
    private boolean destinationStationEnabled = true;
    private boolean unhandledErrorOccurredDialogVisible = false;
    private boolean criticalNsiBackDialogVisible;
    private boolean criticalNsiCloseShiftDialogVisible;

    @Override
    protected void onViewAttached(PdSalePreparationView view) {
        view.setSendETicketBtnVisible(mETicketBtnVisible);
        view.setFeeLabel(mFeeLabel);
        view.setDirection(mTicketWayType);
        view.setPaymentTypeVisible(mPaymentTypeVisible);
        view.setCostGroupVisible(mCostGroupVisible);
        if (progressShown) {
            view.showProgress();
        } else {
            view.hideProgress();
        }
        view.setTariffPlans(mTariffPlans);
        view.setTicketTypes(mTicketTypes);
        view.setSelectedTariffPlanPosition(mSelectedTariffPlanPosition);
        view.setSelectedTicketTypePosition(mSelectedTicketTypePosition);
        view.setPaymentType(mPaymentType);
        view.setOnePdCost(mOnePdCost);
        view.setTotalCost(mTotalCost);
        view.setFeeChecked(mFeeChecked);
        view.setFeeEnabled(mFeeEnabled);
        view.setFeeValue(mFeeValue);
        view.setOnePdCostLabel(mOnePdCostLabel);
        view.setTitle(mTitle);
        view.setPdCount(mPdCount);
        view.setDepartureStations(mDepartureStations);
        view.setDestinationStations(mDestinationStations);
        view.setDepartureStationName(mDepartureStationName);
        view.setDestinationStationName(mDestinationStationName);
        view.setSaleButtonsState(mSaleButtonsState);
        view.setDirectionBtnEnabled(mDirectionBtnEnabled);
        view.setIncrementPdCountBtnEnabled(mIncrementPdCountBtnEnable);
        view.setDecrementPdCountBtnEnabled(mDecrementPdCountBtnEnabled);
        view.setExemptionValue(mExemptionCode.first, mExemptionCode.second);
        view.setPaymentTypeEnabled(mPaymentTypeEnabled);
        view.setPdCountLayoutEnabled(mPdCountLayoutEnabled);
        view.setTransitStationName(mTransitStationName);
        view.setUnhandledErrorOccurredDialogVisible(unhandledErrorOccurredDialogVisible);
        view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible);
        view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible);
    }

    @Override
    protected void onViewDetached(PdSalePreparationView view) {

    }

    @Override
    public void setSendETicketBtnVisible(boolean visible) {
        mETicketBtnVisible = visible;
        forEachView(view -> view.setSendETicketBtnVisible(mETicketBtnVisible));
    }

    @Override
    public void setFeeLabel(FeeLabel feeLabel) {
        mFeeLabel = feeLabel;
        forEachView(view -> view.setFeeLabel(mFeeLabel));
    }

    @Override
    public void setDirection(TicketWayType ticketWayType) {
        mTicketWayType = ticketWayType;
        forEachView(view -> view.setDirection(mTicketWayType));
    }

    @Override
    public void showProgress() {
        progressShown = true;
        forEachView(PdSalePreparationView::showProgress);
    }

    @Override
    public void hideProgress() {
        progressShown = false;
        forEachView(PdSalePreparationView::hideProgress);
    }

    @Override
    public void setTariffPlans(List<TariffPlan> tariffPlans) {
        mTariffPlans = tariffPlans;
        forEachView(view -> view.setTariffPlans(mTariffPlans));
    }

    @Override
    public void setTicketTypes(List<TicketType> ticketTypes) {
        mTicketTypes = ticketTypes;
        forEachView(view -> view.setTicketTypes(mTicketTypes));
    }

    @Override
    public void setSelectedTariffPlanPosition(int position) {
        mSelectedTariffPlanPosition = position;
        forEachView(view -> view.setSelectedTariffPlanPosition(mSelectedTariffPlanPosition));
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        mSelectedTicketTypePosition = position;
        forEachView(view -> view.setSelectedTicketTypePosition(mSelectedTicketTypePosition));
    }

    @Override
    public void setPaymentType(PaymentType paymentType) {
        mPaymentType = paymentType;
        forEachView(view -> view.setPaymentType(mPaymentType));
    }

    @Override
    public void setOnePdCost(BigDecimal cost) {
        mOnePdCost = cost;
        forEachView(view -> view.setOnePdCost(mOnePdCost));
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        mTotalCost = cost;
        forEachView(view -> view.setTotalCost(mTotalCost));
    }

    @Override
    public void setFeeChecked(boolean checked) {
        mFeeChecked = checked;
        forEachView(view -> view.setFeeChecked(mFeeChecked));
    }

    @Override
    public void setFeeEnabled(boolean enabled) {
        mFeeEnabled = enabled;
        forEachView(view -> view.setFeeEnabled(mFeeEnabled));
    }

    @Override
    public void setFeeValue(@Nullable BigDecimal feeValue) {
        mFeeValue = feeValue;
        forEachView(view -> view.setFeeValue(mFeeValue));
    }

    @Override
    public void setOnePdCostLabel(TicketCategoryLabel label) {
        mOnePdCostLabel = label;
        forEachView(view -> view.setOnePdCostLabel(mOnePdCostLabel));
    }

    @Override
    public void setTitle(TicketCategoryLabel label) {
        mTitle = label;
        forEachView(view -> view.setTitle(mTitle));
    }

    @Override
    public void setPdCount(int count) {
        mPdCount = count;
        forEachView(view -> view.setPdCount(mPdCount));
    }

    @Override
    public void setPdCountLayoutEnabled(boolean enabled) {
        mPdCountLayoutEnabled = enabled;
        forEachView(view -> view.setPdCountLayoutEnabled(mPdCountLayoutEnabled));
    }

    @Override
    public void setExemptionValue(int exemptionCode, int percentage) {
        mExemptionCode = new Pair<>(exemptionCode, percentage);
        forEachView(view -> view.setExemptionValue(mExemptionCode.first, mExemptionCode.second));
    }

    @Override
    public void showEdsFailedError() {
        forEachView(PdSalePreparationView::showEdsFailedError);
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        mDepartureStationName = departureStationName;
        forEachView(view -> view.setDepartureStationName(mDepartureStationName));
    }

    @Override
    public void setDepartureStationEnabled(boolean enabled) {
        departureStationEnabled = enabled;
        forEachView(view -> view.setDepartureStationEnabled(departureStationEnabled));
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        mDestinationStationName = destinationStationName;
        forEachView(view -> view.setDestinationStationName(mDestinationStationName));
    }

    @Override
    public void setDestinationStationEnabled(boolean enabled) {
        destinationStationEnabled = enabled;
        forEachView(view -> view.setDestinationStationEnabled(destinationStationEnabled));
    }

    @Override
    public void setDepartureStations(List<Station> stations) {
        mDepartureStations = stations;
        forEachView(view -> view.setDepartureStations(mDepartureStations));
    }

    @Override
    public void setDestinationStations(List<Station> stations) {
        mDestinationStations = stations;
        forEachView(view -> view.setDestinationStations(mDestinationStations));
    }

    @Override
    public void setSaleButtonsState(SaleButtonsState saleButtonsState) {
        mSaleButtonsState = saleButtonsState;
        forEachView(view -> view.setSaleButtonsState(mSaleButtonsState));
    }

    @Override
    public void setDirectionBtnEnabled(boolean enabled) {
        mDirectionBtnEnabled = enabled;
        forEachView(view -> view.setDirectionBtnEnabled(mDirectionBtnEnabled));
    }

    @Override
    public void setDecrementPdCountBtnEnabled(boolean enabled) {
        mDecrementPdCountBtnEnabled = enabled;
        forEachView(view -> view.setDecrementPdCountBtnEnabled(mDecrementPdCountBtnEnabled));
    }

    @Override
    public void setIncrementPdCountBtnEnabled(boolean enabled) {
        mIncrementPdCountBtnEnable = enabled;
        forEachView(view -> view.setIncrementPdCountBtnEnabled(mIncrementPdCountBtnEnable));
    }

    @Override
    public void setTransitStationName(String stationName) {
        mTransitStationName = stationName;
        forEachView(view -> view.setTransitStationName(mTransitStationName));
    }

    @Override
    public void showExemptionInDifferentRegionsDeniedError() {
        forEachView(PdSalePreparationView::showExemptionInDifferentRegionsDeniedError);
    }

    @Override
    public void showDeniedForSaleOnTicketStorageTypeError(String ticketStorageTypeName){
        forEachView(view -> view.showDeniedForSaleOnTicketStorageTypeError(ticketStorageTypeName));
    }

    @Override
    public void setUnhandledErrorOccurredDialogVisible(boolean visible) {
        unhandledErrorOccurredDialogVisible = visible;

        forEachView(view -> view.setUnhandledErrorOccurredDialogVisible(unhandledErrorOccurredDialogVisible));
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
    public void setPaymentTypeEnabled(boolean enabled) {
        mPaymentTypeEnabled = enabled;
        forEachView(view -> view.setPaymentTypeEnabled(mPaymentTypeEnabled));
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

}
