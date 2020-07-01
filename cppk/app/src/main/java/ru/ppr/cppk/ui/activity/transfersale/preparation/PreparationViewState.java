package ru.ppr.cppk.ui.activity.transfersale.preparation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
class PreparationViewState extends BaseMvpViewState<PreparationView> implements PreparationView {

    private boolean loadingDialogShown = false;
    private ParentPdInfo parentPdInfo = null;
    private Date startDate;
    private Date endDate;
    private String departureStation = "";
    private String destinationStation = "";
    private List<TicketType> ticketTypes = Collections.emptyList();
    private int selectedTicketTypePosition = -1;
    private boolean ticketTypeSelectionEnabled;
    private boolean feeChecked = true;
    private boolean feeEnabled = true;
    private BigDecimal feeValue = BigDecimal.ZERO;
    private FeeType feeType = FeeType.TRANSFER_IN_TRAIN;
    private BigDecimal transferCost = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private PaymentType paymentType = null;
    private boolean paymentTypeEnabled = false;
    private boolean paymentTypeVisible = false;
    private boolean edsFailedErrorDialogVisible = false;
    private SaleButtonsState saleButtonsState = SaleButtonsState.WRITE_AND_PRINT;

    @Inject
    PreparationViewState() {

    }

    @Override
    protected void onViewAttached(PreparationView view) {
        if (loadingDialogShown) {
            view.showLoadingDialog();
        } else {
            view.hideLoadingDialog();
        }
        view.setParentPdInfo(parentPdInfo);
        view.setTransferValidityDates(startDate, endDate);
        view.setStations(departureStation, destinationStation);
        view.setTicketTypes(ticketTypes);
        view.setSelectedTicketTypePosition(selectedTicketTypePosition);
        view.setTicketTypeSelectionEnabled(ticketTypeSelectionEnabled);
        view.setFeeChecked(feeChecked);
        view.setFeeEnabled(feeEnabled);
        view.setFeeValue(feeValue);
        view.setFeeType(feeType);
        view.setTransferCost(transferCost);
        view.setTotalCost(totalCost);
        view.setPaymentType(paymentType);
        view.setPaymentTypeEnabled(paymentTypeEnabled);
        view.setPaymentTypeVisible(paymentTypeVisible);
        view.setEdsFailedErrorDialogVisible(edsFailedErrorDialogVisible);
        view.setSaleButtonsState(saleButtonsState);
    }

    @Override
    protected void onViewDetached(PreparationView view) {

    }

    @Override
    public void showLoadingDialog() {
        loadingDialogShown = true;
        forEachView(PreparationView::showLoadingDialog);
    }

    @Override
    public void hideLoadingDialog() {
        loadingDialogShown = false;
        forEachView(PreparationView::hideLoadingDialog);
    }

    @Override
    public void setParentPdInfo(@Nullable ParentPdInfo parentPdInfo) {
        this.parentPdInfo = parentPdInfo;
        forEachView(view -> view.setParentPdInfo(this.parentPdInfo));
    }

    @Override
    public void setTransferValidityDates(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        forEachView(view -> view.setTransferValidityDates(this.startDate, this.endDate));
    }

    @Override
    public void setStations(String departureStation, String destinationStation) {
        this.departureStation = departureStation;
        this.destinationStation = destinationStation;
        forEachView(view -> view.setStations(this.departureStation, this.destinationStation));
    }

    @Override
    public void setTicketTypes(@NonNull List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        forEachView(view -> view.setTicketTypes(this.ticketTypes));
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        this.selectedTicketTypePosition = position;
        forEachView(view -> view.setSelectedTicketTypePosition(this.selectedTicketTypePosition));
    }

    @Override
    public void setTicketTypeSelectionEnabled(boolean enabled) {
        this.ticketTypeSelectionEnabled = enabled;
        forEachView(view -> view.setTicketTypeSelectionEnabled(this.ticketTypeSelectionEnabled));
    }

    @Override
    public void setFeeChecked(boolean checked) {
        this.feeChecked = checked;
        forEachView(view -> view.setFeeChecked(this.feeChecked));
    }

    @Override
    public void setFeeEnabled(boolean enabled) {
        this.feeEnabled = enabled;
        forEachView(view -> view.setFeeEnabled(this.feeEnabled));
    }

    @Override
    public void setFeeValue(@Nullable BigDecimal feeValue) {
        this.feeValue = feeValue;
        forEachView(view -> view.setFeeValue(this.feeValue));
    }

    @Override
    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
        forEachView(view -> view.setFeeType(this.feeType));
    }

    @Override
    public void setTransferCost(BigDecimal cost) {
        this.transferCost = cost;
        forEachView(view -> view.setTransferCost(this.transferCost));
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        this.totalCost = cost;
        forEachView(view -> view.setTotalCost(this.totalCost));
    }

    @Override
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
        forEachView(view -> view.setPaymentType(this.paymentType));
    }

    @Override
    public void setPaymentTypeEnabled(boolean enabled) {
        this.paymentTypeEnabled = enabled;
        forEachView(view -> view.setPaymentTypeEnabled(this.paymentTypeEnabled));
    }

    @Override
    public void setPaymentTypeVisible(boolean visible) {
        this.paymentTypeVisible = visible;
        forEachView(view -> view.setPaymentTypeVisible(this.paymentTypeVisible));
    }

    @Override
    public void setEdsFailedErrorDialogVisible(boolean visible) {
        this.edsFailedErrorDialogVisible = visible;
        forEachView(view -> view.setEdsFailedErrorDialogVisible(this.edsFailedErrorDialogVisible));
    }

    @Override
    public void setSaleButtonsState(SaleButtonsState saleButtonsState) {
        this.saleButtonsState = saleButtonsState;
        forEachView(view -> view.setSaleButtonsState(this.saleButtonsState));
    }
}
