package ru.ppr.cppk.ui.activity.transfersalestart;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
class TransferSaleStartViewState extends BaseMvpViewState<TransferSaleStartView> implements TransferSaleStartView {

    private boolean loadingDialogShown = false;
    private String departureStationName = "";
    private String destinationStationName = "";
    private List<Station> departureStations = Collections.emptyList();
    private List<Station> destinationStations = Collections.emptyList();
    private List<TicketType> ticketTypes = Collections.emptyList();
    private boolean ticketTypesSelectVisible;
    private int selectedTicketTypePosition = -1;
    private boolean continueBtnVisible = false;
    private boolean criticalNsiBackDialogVisible = false;
    private boolean criticalNsiCloseShiftDialogVisible = false;
    private boolean noStationsForSaleError = false;

    @Inject
    TransferSaleStartViewState() {

    }

    @Override
    protected void onViewAttached(TransferSaleStartView view) {
        if (loadingDialogShown) {
            view.showLoadingDialog();
        } else {
            view.hideLoadingDialog();
        }
        if (noStationsForSaleError) {
            view.showNoStationsForSaleError();
            noStationsForSaleError = false;
        }
        view.setDepartureStationName(departureStationName);
        view.setDestinationStationName(destinationStationName);
        view.setDepartureStations(departureStations);
        view.setDestinationStations(destinationStations);
        view.setTicketTypes(ticketTypes);
        view.setTicketTypesSelectVisible(ticketTypesSelectVisible);
        view.setSelectedTicketTypePosition(selectedTicketTypePosition);
        view.setContinueBtnVisible(continueBtnVisible);
        view.setCriticalNsiBackDialogVisible(criticalNsiBackDialogVisible);
        view.setCriticalNsiCloseShiftDialogVisible(criticalNsiCloseShiftDialogVisible);
    }

    @Override
    protected void onViewDetached(TransferSaleStartView view) {

    }

    @Override
    public void showLoadingDialog() {
        loadingDialogShown = true;
        forEachView(TransferSaleStartView::showLoadingDialog);
    }

    @Override
    public void hideLoadingDialog() {
        loadingDialogShown = false;
        forEachView(TransferSaleStartView::hideLoadingDialog);
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
        forEachView(view -> view.setDepartureStationName(this.departureStationName));
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
        forEachView(view -> view.setDestinationStationName(this.destinationStationName));
    }

    @Override
    public void setDepartureStations(@NonNull List<Station> stations) {
        this.departureStations = stations;
        forEachView(view -> view.setDepartureStations(this.departureStations));
    }

    @Override
    public void setDestinationStations(@NonNull List<Station> stations) {
        this.destinationStations = stations;
        forEachView(view -> view.setDestinationStations(this.destinationStations));
    }

    @Override
    public void setTicketTypes(@NonNull List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        forEachView(view -> view.setTicketTypes(this.ticketTypes));
    }

    @Override
    public void setTicketTypesSelectVisible(boolean visible) {
        this.ticketTypesSelectVisible = visible;
        forEachView(view -> view.setTicketTypesSelectVisible(this.ticketTypesSelectVisible));
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        this.selectedTicketTypePosition = position;
        forEachView(view -> view.setSelectedTicketTypePosition(this.selectedTicketTypePosition));
    }

    @Override
    public void setContinueBtnVisible(boolean visible) {
        this.continueBtnVisible = visible;
        forEachView(view -> view.setContinueBtnVisible(this.continueBtnVisible));
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        this.criticalNsiBackDialogVisible = visible;
        forEachView(view -> view.setCriticalNsiBackDialogVisible(this.criticalNsiBackDialogVisible));
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        this.criticalNsiCloseShiftDialogVisible = visible;
        forEachView(view -> view.setCriticalNsiCloseShiftDialogVisible(this.criticalNsiCloseShiftDialogVisible));
    }

    @Override
    public void showNoStationsForSaleError() {
        this.noStationsForSaleError = true;
        forEachView(view -> {
            this.noStationsForSaleError = false;
            view.showNoStationsForSaleError();
        });
    }

}
