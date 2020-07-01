package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;

/**
 * Комбинированный абонемент на количество поездок, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class CombinedCountTripsPdViewModel extends CountTripsPdViewModel {

    /**
     * Количество оставшихся поездок на поезд 6000
     */
    private int availableTripsCount6000;
    /**
     * Количество оставшихся поездок на поезд 7000
     */
    private int availableTripsCount7000;
    /**
     * Время последнего прохода на поезд 7000
     * {@code null} если прохода не было
     */
    private Date lastPassageTime7000;
    /**
     * Флаг наличия ошибки во времени последнего прохода на поезд 7000
     */
    private boolean lastPassage7000Error;
    /**
     * Флаг отсутствия поездок для списания на поезд 7000
     */
    private boolean noTrips7000Error;
    /**
     * Флаг  наличия ошибки "Проход на станции отправления НЕ соответствует категории поезда"
     */
    private boolean wrongTrainCategory;

    public int getAvailableTripsCount6000() {
        return availableTripsCount6000;
    }

    public void setAvailableTripsCount6000(int availableTripsCount6000) {
        this.availableTripsCount6000 = availableTripsCount6000;
    }

    public int getAvailableTripsCount7000() {
        return availableTripsCount7000;
    }

    public void setAvailableTripsCount7000(int availableTripsCount7000) {
        this.availableTripsCount7000 = availableTripsCount7000;
    }

    public Date getLastPassageTime7000() {
        return lastPassageTime7000;
    }

    public void setLastPassageTime7000(Date lastPassageTime7000) {
        this.lastPassageTime7000 = lastPassageTime7000;
    }

    public boolean isLastPassage7000Error() {
        return lastPassage7000Error;
    }

    public void setLastPassage7000Error(boolean lastPassage7000Error) {
        this.lastPassage7000Error = lastPassage7000Error;
    }

    public boolean isNoTrips7000Error() {
        return noTrips7000Error;
    }

    public void setNoTrips7000Error(boolean noTrips7000Error) {
        this.noTrips7000Error = noTrips7000Error;
    }

    public boolean isWrongTrainCategory() {
        return wrongTrainCategory;
    }

    public void setWrongTrainCategory(boolean wrongTrainCategory) {
        this.wrongTrainCategory = wrongTrainCategory;
    }
}
