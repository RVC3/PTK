package ru.ppr.chit.ui.activity.ticketcontrol;


import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.chit.domain.ticketcontrol.DataCarrierType;
import ru.ppr.chit.ui.activity.ticketcontrol.model.TicketValidationResult;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
public interface TicketControlView extends MvpView {

    void setTicketNumber(long ticketNumber);

    void setTicketType(String ticketType);

    void setDataCarrierType(DataCarrierType dataCarrierType);

    void setDate(Date date);

    void setTrainNumber(String trainNumber);

    void setDepStationName(String depStationName);

    void setDestStationName(String destStationName);

    void setExemptionExpressCode(Integer expressCode);

    void setCarNumber(String expectedCarNumber, String actualCarNumber);

    void setNewCarInfoVisible(boolean visible);

    void setSeatNumber(String expectedSeatNumber, String actualSeatNumber);

    void setNewSeatInfoVisible(boolean visible);

    void setPassengerFio(String fio);

    void setDocumentType(String documentType);

    void setDocumentNumber(String documentNumber);

    void setDateValid(@NonNull DateValidity dateValidity);

    void setTrainValid(boolean valid);

    void setDepStationValid(boolean valid);

    void setDestinationStationValid(boolean valid);

    void setValidationResult(TicketValidationResult validationResult);

    void setApproveBtnVisible(boolean visible);

    void setDenyBtnVisible(boolean visible);

    void setState(State state);

    enum State {
        PREPARING,
        DATA,
        NO_DATA
    }

    enum DateValidity {

        FULLY_VALID,
        PROBABLY_VALID,
        NOT_VALID

    }

}
