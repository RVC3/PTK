package ru.ppr.core.dataCarrier.smartCard.cardinformation;


import java.util.Date;

/**
 * Данные карты о комплексных и единых билетах
 * @author Kolesnikov Sergey
 */
public class CardPdData {

    /**
     *   Дата и время окончания действия услуги
     */
    private Date validityDateTime;

    /**
     *   Число оставшихся / совершенных поездок TRIP_COUNT
     */
    private int countRematingPerformedTrips;

    /**
     *   Тип билета CRDCODE
     */
    private int typeTicket;

    /**
     *   Валидность формата данных
     */
    private boolean validFormatData;

    public CardPdData() {
    }

    public void setValidityDateTime(Date validityDateTime) {
        this.validityDateTime = validityDateTime;
    }

    public Date getValidityDateTime() {
        return validityDateTime;
    }

    public void setCountRematingPerformedTrips(int countRematingPerformedTrips) {
        this.countRematingPerformedTrips = countRematingPerformedTrips;
    }

    public int getCountRematingPerformedTrips() {
        return countRematingPerformedTrips;
    }

    public void setTypeTicket(int typeTicket) {
        this.typeTicket = typeTicket;
    }

    public int getTypeTicket() {
        return typeTicket;
    }

    public boolean isValidFormatData() {
        return validFormatData;
    }

    public void setValidFormatData(boolean validFormatData) {
        this.validFormatData = validFormatData;
    }

    @Override
    public String toString() {
        return "CardPdData{" +
                "validityDateTime=" + validityDateTime +
                ", countRematingPerformedTrips=" + countRematingPerformedTrips +
                ", typeTicket=" + typeTicket +
                ", validFormatData=" + validFormatData +
                '}';
    }
}



