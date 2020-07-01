package ru.ppr.cppk.ui.fragment.pd.pdwithplace.model;

import java.util.Date;

import ru.ppr.cppk.ui.fragment.pd.simple.model.TicketPdViewModel;

/**
 * @author Dmitry Vinogradov
 */
public class PdWithPlaceViewModel extends TicketPdViewModel {

    /**
     * Дата отправления
     */
    private Date departureDate;

    /**
     * Имя и инициалы пассажира
     */
    private String passengerName;

    /**
     * Последние 4 цифры/символа номера документа
     */
    private String documentNumber;

    /**
     * Номер поезда
     */
    private String trainNumber;

    /**
     * Номер вагона
     */
    private String wagonNumber;

    /**
     * Номер места в вагоне. В текущих вагонах до 64 мест.
     */
    private String placeNumber;

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getWagonNumber() {
        return wagonNumber;
    }

    public void setWagonNumber(String wagonNumber) {
        this.wagonNumber = wagonNumber;
    }

    public String getPlaceNumber() {
        return placeNumber;
    }

    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }

}
