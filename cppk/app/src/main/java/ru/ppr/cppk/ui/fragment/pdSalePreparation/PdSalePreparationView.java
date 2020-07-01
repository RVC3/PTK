package ru.ppr.cppk.ui.fragment.pdSalePreparation;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Aleksandr Brazhkin
 */
interface PdSalePreparationView extends MvpView {

    void setCostGroupVisible(boolean visible);

    void setPaymentTypeEnabled(boolean enabled);

    void setPaymentTypeVisible(boolean visible);

    void setSendETicketBtnVisible(boolean visible);

    void setFeeLabel(FeeLabel feeLabel);

    void setDirection(TicketWayType ticketWayType);

    void showProgress();

    void hideProgress();

    void setTariffPlans(List<TariffPlan> tariffPlans);

    void setTicketTypes(List<TicketType> ticketTypes);

    void setSelectedTariffPlanPosition(int position);

    void setSelectedTicketTypePosition(int position);

    void setPaymentType(PaymentType paymentType);

    void setOnePdCost(BigDecimal cost);

    void setTotalCost(BigDecimal cost);

    void setFeeChecked(boolean checked);

    void setFeeEnabled(boolean enabled);

    void setFeeValue(@Nullable BigDecimal feeValue);

    void setOnePdCostLabel(TicketCategoryLabel label);

    void setTitle(TicketCategoryLabel label);

    void setPdCount(int count);

    void setPdCountLayoutEnabled(boolean enabled);

    void setExemptionValue(int exemptionCode, int percentage);

    void showEdsFailedError();

    void setDepartureStationName(String departureStationName);

    void setDepartureStationEnabled(boolean enabled);

    void setDestinationStationName(String destinationStationName);

    void setDestinationStationEnabled(boolean enabled);

    void setDepartureStations(List<Station> stations);

    void setDestinationStations(List<Station> stations);

    void setSaleButtonsState(SaleButtonsState saleButtonsState);

    void setDirectionBtnEnabled(boolean enabled);

    void setDecrementPdCountBtnEnabled(boolean enabled);

    void setIncrementPdCountBtnEnabled(boolean enabled);

    void setTransitStationName(String stationName);

    void showExemptionInDifferentRegionsDeniedError();

    void showDeniedForSaleOnTicketStorageTypeError(String ticketStorageTypeName);

    void setUnhandledErrorOccurredDialogVisible(boolean visible);

    void setCriticalNsiBackDialogVisible(boolean visible);

    void setCriticalNsiCloseShiftDialogVisible(boolean visible);

    enum SaleButtonsState {
        /**
         * "Записать на БСК" и "Распечатать билет"
         */
        WRITE_AND_PRINT,
        /**
         * "Распечатать билет"
         */
        PRINT_ONLY,
        /**
         * "Записать на БСК"
         */
        WRITE_ONLY,
        /**
         * "Оформить"
         */
        PROCESS
    }

    enum FeeLabel {
        IN_TRAIN,
        AT_DESTINATION_STATION
    }

    enum TicketCategoryLabel {
        SINGLE,
        BAGGAGE
    }
}
