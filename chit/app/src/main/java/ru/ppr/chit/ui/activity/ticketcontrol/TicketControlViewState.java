package ru.ppr.chit.ui.activity.ticketcontrol;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.chit.domain.ticketcontrol.DataCarrierType;
import ru.ppr.chit.ui.activity.ticketcontrol.model.TicketValidationResult;
import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketControlViewState extends BaseMvpViewState<TicketControlView> implements TicketControlView {

    private long ticketNumber;
    private String ticketType;
    private DataCarrierType dataCarrierType;
    private Date date;
    private String trainNumber;
    private String depStationName;
    private String destStationName;
    private Integer exemptionExpressCode;
    private String expectedCarNumber;
    private String actualCarNumber;
    private boolean newCarInfoVisible;
    private String expectedSeatNumber;
    private String actualSeatNumber;
    private boolean newSeatInfoVisible;
    private String passengerFio;
    private String documentType;
    private String documentNumber;
    private DateValidity dateValidity = DateValidity.NOT_VALID;
    private boolean trainValid;
    private boolean depStationValid;
    private boolean destinationStationValid;
    private TicketValidationResult validationResult;
    private boolean approveBtnVisible;
    private boolean denyBtnVisible;
    private State state = State.PREPARING;

    @Inject
    TicketControlViewState() {
    }

    @Override
    protected void onViewAttached(TicketControlView view) {
        view.setTicketNumber(this.ticketNumber);
        view.setTicketType(this.ticketType);
        view.setDataCarrierType(this.dataCarrierType);
        view.setDate(this.date);
        view.setTrainNumber(this.trainNumber);
        view.setDepStationName(this.depStationName);
        view.setDestStationName(this.destStationName);
        view.setExemptionExpressCode(this.exemptionExpressCode);
        view.setCarNumber(this.expectedCarNumber, this.actualCarNumber);
        view.setNewCarInfoVisible(this.newCarInfoVisible);
        view.setSeatNumber(this.expectedSeatNumber, this.actualSeatNumber);
        view.setNewSeatInfoVisible(this.newSeatInfoVisible);
        view.setPassengerFio(this.passengerFio);
        view.setDocumentType(this.documentType);
        view.setDocumentNumber(this.documentNumber);
        view.setDateValid(this.dateValidity);
        view.setTrainValid(this.trainValid);
        view.setDepStationValid(this.depStationValid);
        view.setValidationResult(this.validationResult);
        view.setApproveBtnVisible(this.approveBtnVisible);
        view.setDenyBtnVisible(this.denyBtnVisible);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(TicketControlView view) {

    }

    @Override
    public void setTicketNumber(long ticketNumber) {
        this.ticketNumber = ticketNumber;
        forEachView(view -> view.setTicketNumber(this.ticketNumber));
    }

    @Override
    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
        forEachView(view -> view.setTicketType(this.ticketType));
    }

    @Override
    public void setDataCarrierType(DataCarrierType dataCarrierType) {
        this.dataCarrierType = dataCarrierType;
        forEachView(view -> view.setDataCarrierType(this.dataCarrierType));
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
        forEachView(view -> view.setDate(this.date));
    }

    @Override
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
        forEachView(view -> view.setTrainNumber(this.trainNumber));
    }

    @Override
    public void setDepStationName(String depStationName) {
        this.depStationName = depStationName;
        forEachView(view -> view.setDepStationName(this.depStationName));
    }

    @Override
    public void setDestStationName(String destStationName) {
        this.destStationName = destStationName;
        forEachView(view -> view.setDestStationName(this.destStationName));
    }

    @Override
    public void setExemptionExpressCode(Integer expressCode) {
        this.exemptionExpressCode = expressCode;
        forEachView(view -> view.setExemptionExpressCode(this.exemptionExpressCode));
    }

    @Override
    public void setCarNumber(String expectedCarNumber, String actualCarNumber) {
        this.expectedCarNumber = expectedCarNumber;
        this.actualCarNumber = actualCarNumber;
        forEachView(view -> view.setCarNumber(this.expectedCarNumber, this.actualCarNumber));
    }

    @Override
    public void setNewCarInfoVisible(boolean visible) {
        this.newCarInfoVisible = visible;
        forEachView(view -> view.setNewCarInfoVisible(this.newCarInfoVisible));
    }

    @Override
    public void setSeatNumber(String expectedSeatNumber, String actualSeatNumber) {
        this.expectedSeatNumber = expectedSeatNumber;
        this.actualSeatNumber = actualSeatNumber;
        forEachView(view -> view.setSeatNumber(this.expectedSeatNumber, this.actualSeatNumber));
    }

    @Override
    public void setNewSeatInfoVisible(boolean visible) {
        this.newSeatInfoVisible = visible;
        forEachView(view -> view.setNewSeatInfoVisible(this.newSeatInfoVisible));
    }

    @Override
    public void setPassengerFio(String fio) {
        this.passengerFio = fio;
        forEachView(view -> view.setPassengerFio(this.passengerFio));
    }

    @Override
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
        forEachView(view -> view.setDocumentType(this.documentType));
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        forEachView(view -> view.setDocumentNumber(this.documentNumber));
    }

    @Override
    public void setDateValid(@NonNull DateValidity dateValidity) {
        this.dateValidity = dateValidity;
        forEachView(view -> view.setDateValid(this.dateValidity));
    }

    @Override
    public void setTrainValid(boolean valid) {
        this.trainValid = valid;
        forEachView(view -> view.setTrainValid(this.trainValid));
    }

    @Override
    public void setDepStationValid(boolean valid) {
        this.depStationValid = valid;
        forEachView(view -> view.setDepStationValid(this.depStationValid));
    }

    @Override
    public void setDestinationStationValid(boolean valid) {
        this.destinationStationValid = valid;
        forEachView(view -> view.setDestinationStationValid(this.destinationStationValid));
    }

    @Override
    public void setValidationResult(TicketValidationResult validationResult) {
        this.validationResult = validationResult;
        forEachView(view -> view.setValidationResult(this.validationResult));
    }

    @Override
    public void setApproveBtnVisible(boolean visible) {
        this.approveBtnVisible = visible;
        forEachView(view -> view.setApproveBtnVisible(this.approveBtnVisible));
    }

    @Override
    public void setDenyBtnVisible(boolean visible) {
        this.denyBtnVisible = visible;
        forEachView(view -> view.setDenyBtnVisible(this.denyBtnVisible));
    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(this.state));
    }
}