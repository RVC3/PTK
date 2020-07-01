package ru.ppr.cppk.ui.fragment.extraPaymentExecution;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * @author Aleksandr Brazhkin
 */
interface ExtraPaymentExecutionView extends MvpView {

    void showProgressDialog();

    void hideProgressDialog();

    void setParentPdInfo(ParentPdInfo parentPdInfo);

    void setTariffPlans(List<TariffPlan> tariffPlans);

    void setDepartureStationName(String departureStationName);

    void setDestinationStationName(String destinationStationName);

    void setDepartureStations(List<Station> stations);

    void setDestinationStations(List<Station> stations);

    void setPaymentType(PaymentType paymentType);

    void setPaymentTypeEnabled(boolean enabled);

    void setPaymentTypeVisible(boolean visible);

    void setFeeChecked(boolean checked);

    void setFeeEnabled(boolean enabled);

    void setFeeValue(@Nullable BigDecimal feeValue);

    void setCostGroupVisible(boolean visible);

    void setOpePdCost(BigDecimal cost);

    void setTotalCost(BigDecimal cost);

    void setExemptionEnabled(boolean enabled);

    void setExemptionValue(int exemptionCode, int percentage);

    void setSelectedTariffPlanPosition(int position);

    void setFeeLabel(String feeLabel);

    void setStationFieldsEnabled(boolean enabled);

    void setClearStationsBtnEnabled(boolean enabled);

    void setSwapStationsBtnEnabled(boolean enabled);

    void setNonExemptionPaymentForExemptionPdAttentionDialogVisible(boolean visible);

    void setCriticalNsiBackDialogVisible(boolean visible);

    void setCriticalNsiCloseShiftDialogVisible(boolean visible);

    void setUnhandledErrorOccurredDialogVisible(boolean visible);

    class ParentPdInfo {
        String ticketType = "";
        int pdNumber;
        String departureStation = "";
        String destinationStation = "";
        String trainCategory = "";
        int exemptionCode;
        TicketWayType direction;
        Date startDateTime;
        long terminalNumber;
    }

}
