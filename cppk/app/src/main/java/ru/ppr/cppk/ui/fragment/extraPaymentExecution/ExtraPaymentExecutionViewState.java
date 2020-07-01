package ru.ppr.cppk.ui.fragment.extraPaymentExecution;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * @author Aleksandr Brazhkin
 */
class ExtraPaymentExecutionViewState extends BaseMvpViewState<ExtraPaymentExecutionView> implements ExtraPaymentExecutionView {

    private ParentPdInfo mParentPdInfo = new ParentPdInfo();
    private List<TariffPlan> mTariffPlans = Collections.emptyList();
    private String mDepartureStationName = "";
    private String mDestinationStationName = "";
    private List<Station> mDepartureStations = Collections.emptyList();
    private List<Station> mDestinationStations = Collections.emptyList();
    private int mSelectedTariffPlanPosition = -1;
    private PaymentType mPaymentType;
    private BigDecimal mFeeValue = BigDecimal.ZERO;
    private boolean mFeeChecked = true;
    private boolean mFeeEnabled = true;
    private boolean mCostGroupVisible = false;
    private BigDecimal mOpePdCost = BigDecimal.ZERO;
    private BigDecimal mTotalCost = BigDecimal.ZERO;
    private Pair<Integer, Integer> mExemptionCode = new Pair<>(0, 0);
    private boolean mProgressDialogShown = false;
    private boolean mStationFieldsEnabled = false;
    private boolean mClearStationsBtnEnabled = false;
    private boolean mSwapStationsBtnEnabled = false;
    private boolean mExemptionEnabled = true;
    private boolean mPaymentTypeEnabled = false;
    private boolean mPaymentTypeVisible = false;
    private String mFeeLabel = "";
    private boolean nonExemptionPaymentForExemptionPdAttentionDialogVisible;
    private boolean criticalNsiBackDialogVisible;
    private boolean criticalNsiCloseShiftDialogVisible;
    private boolean unhandledErrorOccurredDialogVisible;

    @Override
    protected void onViewAttached(ExtraPaymentExecutionView view) {
        view.setParentPdInfo(mParentPdInfo);
        view.setTariffPlans(mTariffPlans);
        view.setDepartureStations(mDepartureStations);
        view.setDestinationStations(mDestinationStations);
        view.setSelectedTariffPlanPosition(mSelectedTariffPlanPosition);
        view.setPaymentType(mPaymentType);
        view.setFeeChecked(mFeeChecked);
        view.setFeeEnabled(mFeeEnabled);
        view.setFeeValue(mFeeValue);
        view.setCostGroupVisible(mCostGroupVisible);
        view.setOpePdCost(mOpePdCost);
        view.setTotalCost(mTotalCost);
        view.setExemptionValue(mExemptionCode.first, mExemptionCode.second);
        if (mProgressDialogShown) {
            view.showProgressDialog();
        } else {
            view.hideProgressDialog();
        }
        view.setStationFieldsEnabled(mStationFieldsEnabled);
        view.setClearStationsBtnEnabled(mClearStationsBtnEnabled);
        view.setSwapStationsBtnEnabled(mSwapStationsBtnEnabled);
        view.setExemptionEnabled(mExemptionEnabled);
        view.setPaymentTypeEnabled(mPaymentTypeEnabled);
        view.setPaymentTypeVisible(mPaymentTypeVisible);
        view.setFeeLabel(mFeeLabel);
        view.setNonExemptionPaymentForExemptionPdAttentionDialogVisible(nonExemptionPaymentForExemptionPdAttentionDialogVisible);
        view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible);
        view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible);
        view.setUnhandledErrorOccurredDialogVisible(unhandledErrorOccurredDialogVisible);
    }

    @Override
    protected void onViewDetached(ExtraPaymentExecutionView view) {

    }

    @Override
    public void showProgressDialog() {
        mProgressDialogShown = true;
        for (ExtraPaymentExecutionView view : views) {
            view.showProgressDialog();
        }
    }

    @Override
    public void hideProgressDialog() {
        mProgressDialogShown = false;
        for (ExtraPaymentExecutionView view : views) {
            view.hideProgressDialog();
        }
    }

    @Override
    public void setParentPdInfo(ParentPdInfo parentPdInfo) {
        mParentPdInfo = parentPdInfo;
        for (ExtraPaymentExecutionView view : views) {
            view.setParentPdInfo(mParentPdInfo);
        }
    }

    @Override
    public void setTariffPlans(List<TariffPlan> tariffPlans) {
        mTariffPlans = tariffPlans;
        for (ExtraPaymentExecutionView view : views) {
            view.setTariffPlans(mTariffPlans);
        }
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        mDepartureStationName = departureStationName;
        for (ExtraPaymentExecutionView view : views) {
            view.setDepartureStationName(mDepartureStationName);
        }
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        mDestinationStationName = destinationStationName;
        for (ExtraPaymentExecutionView view : views) {
            view.setDestinationStationName(mDestinationStationName);
        }
    }

    @Override
    public void setDepartureStations(List<Station> stations) {
        mDepartureStations = stations;
        for (ExtraPaymentExecutionView view : views) {
            view.setDepartureStations(mDepartureStations);
        }
    }

    @Override
    public void setDestinationStations(List<Station> stations) {
        mDestinationStations = stations;
        for (ExtraPaymentExecutionView view : views) {
            view.setDestinationStations(mDestinationStations);
        }
    }

    @Override
    public void setPaymentType(PaymentType paymentType) {
        mPaymentType = paymentType;
        for (ExtraPaymentExecutionView view : views) {
            view.setPaymentType(mPaymentType);
        }
    }

    @Override
    public void setPaymentTypeEnabled(boolean enabled) {
        mPaymentTypeEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setPaymentTypeEnabled(mPaymentTypeEnabled);
        }
    }

    @Override
    public void setPaymentTypeVisible(boolean visible) {
        mPaymentTypeVisible = visible;
        for (ExtraPaymentExecutionView view : views) {
            view.setPaymentTypeVisible(mPaymentTypeVisible);
        }
    }

    @Override
    public void setFeeChecked(boolean checked) {
        mFeeChecked = checked;
        for (ExtraPaymentExecutionView view : views) {
            view.setFeeChecked(mFeeChecked);
        }
    }

    @Override
    public void setFeeEnabled(boolean enabled) {
        mFeeEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setFeeEnabled(mFeeEnabled);
        }
    }

    @Override
    public void setFeeValue(@Nullable BigDecimal feeValue) {
        mFeeValue = feeValue;
        for (ExtraPaymentExecutionView view : views) {
            view.setFeeValue(mFeeValue);
        }
    }

    @Override
    public void setCostGroupVisible(boolean visible) {
        mCostGroupVisible = visible;
        for (ExtraPaymentExecutionView view : views) {
            view.setCostGroupVisible(mCostGroupVisible);
        }
    }

    @Override
    public void setOpePdCost(BigDecimal cost) {
        mOpePdCost = cost;
        for (ExtraPaymentExecutionView view : views) {
            view.setOpePdCost(mOpePdCost);
        }
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        mTotalCost = cost;
        for (ExtraPaymentExecutionView view : views) {
            view.setTotalCost(mTotalCost);
        }
    }

    @Override
    public void setExemptionEnabled(boolean enabled) {
        mExemptionEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setExemptionEnabled(mExemptionEnabled);
        }
    }

    @Override
    public void setExemptionValue(int exemptionCode, int percentage) {
        mExemptionCode = new Pair<>(exemptionCode, percentage);
        for (ExtraPaymentExecutionView view : views) {
            view.setExemptionValue(mExemptionCode.first, mExemptionCode.second);
        }
    }

    @Override
    public void setSelectedTariffPlanPosition(int position) {
        mSelectedTariffPlanPosition = position;
        for (ExtraPaymentExecutionView view : views) {
            view.setSelectedTariffPlanPosition(mSelectedTariffPlanPosition);
        }
    }

    @Override
    public void setFeeLabel(String feeLabel) {
        mFeeLabel = feeLabel;
        for (ExtraPaymentExecutionView view : views) {
            view.setFeeLabel(mFeeLabel);
        }
    }

    @Override
    public void setStationFieldsEnabled(boolean enabled) {
        mStationFieldsEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setStationFieldsEnabled(mStationFieldsEnabled);
        }
    }

    @Override
    public void setClearStationsBtnEnabled(boolean enabled) {
        mClearStationsBtnEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setClearStationsBtnEnabled(mClearStationsBtnEnabled);
        }
    }

    @Override
    public void setSwapStationsBtnEnabled(boolean enabled) {
        mSwapStationsBtnEnabled = enabled;
        for (ExtraPaymentExecutionView view : views) {
            view.setSwapStationsBtnEnabled(mSwapStationsBtnEnabled);
        }
    }

    @Override
    public void setNonExemptionPaymentForExemptionPdAttentionDialogVisible(boolean visible) {
        nonExemptionPaymentForExemptionPdAttentionDialogVisible = visible;
        for (ExtraPaymentExecutionView view : views) {
            view.setNonExemptionPaymentForExemptionPdAttentionDialogVisible(nonExemptionPaymentForExemptionPdAttentionDialogVisible);
        }
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        criticalNsiBackDialogVisible = visible;
        for (ExtraPaymentExecutionView view : views) {
            view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible);
        }
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        criticalNsiCloseShiftDialogVisible = visible;
        for (ExtraPaymentExecutionView view : views) {
            view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible);
        }
    }

    @Override
    public void setUnhandledErrorOccurredDialogVisible(boolean visible) {
        unhandledErrorOccurredDialogVisible = visible;
        forEachView(view -> view.setUnhandledErrorOccurredDialogVisible(this.unhandledErrorOccurredDialogVisible));
    }

}
