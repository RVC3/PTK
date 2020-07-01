package ru.ppr.cppk.ui.activity.transfersale.preparation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
interface PreparationView extends MvpView {

    void showLoadingDialog();

    void hideLoadingDialog();

    void setParentPdInfo(@Nullable ParentPdInfo parentPdInfo);

    void setTransferValidityDates(Date startDate, Date endDate);

    void setStations(String departureStation, String destinationStation);

    void setTicketTypes(@NonNull List<TicketType> ticketTypes);

    void setSelectedTicketTypePosition(int position);

    void setTicketTypeSelectionEnabled(boolean enabled);

    void setFeeChecked(boolean checked);

    void setFeeEnabled(boolean enabled);

    void setFeeValue(@Nullable BigDecimal feeValue);

    void setFeeType(FeeType feeType);

    void setTransferCost(BigDecimal cost);

    void setTotalCost(BigDecimal cost);

    void setPaymentType(PaymentType paymentType);

    void setPaymentTypeEnabled(boolean enabled);

    void setPaymentTypeVisible(boolean visible);

    void setEdsFailedErrorDialogVisible(boolean visible);

    void setSaleButtonsState(SaleButtonsState saleButtonsState);

    class ParentPdInfo {
        String ticketType = "";
        int pdNumber;
        String departureStation = "";
        String destinationStation = "";
        String trainCategory = "";
        int exemptionCode;
        TicketWayType direction;
        Date startDate;
        Date endDate;
    }

    enum SaleButtonsState {
        /**
         * "Записать на БСК" и "Распечатать билет"
         */
        WRITE_AND_PRINT,
        /**
         * "Записать на БСК"
         */
        WRITE_ONLY
    }

}
