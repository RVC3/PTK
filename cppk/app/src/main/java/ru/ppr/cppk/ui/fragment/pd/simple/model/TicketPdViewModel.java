package ru.ppr.cppk.ui.fragment.pd.simple.model;

/**
 * Базовый класс для модели ПД, являющегося билетом, отображаемой во View.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketPdViewModel extends BasePdViewModel {
    /**
     * Наименование станции отправления
     */
    private String depStationName;
    /**
     * Наименование станции назначения
     */
    private String destStationName;
    /**
     * Наименование категории поезда
     */
    private String trainCategoryName;
    /**
     * 4-х значный код льготы
     */
    private int exemptionExpressCode;
    /**
     * Флаг, что это трансфер
     * {@code true} = трансфер
     * {@code false} = пд на поезд
     */
    private boolean transfer;
    /**
     * Флаг наличия ошибки
     * "Билет не действует на данном маршруте (Несоответствие станции)"
     */
    private boolean routeError;
    /**
     * Флаг наличия ошибки
     * "Некорректная категория поезда"
     */
    private boolean trainCategoryError;
    /**
     * Флаг наличия ошибки
     * "Некорректная категория поезда"
     */
    private boolean ticketAnnulledError;

    public String getDepStationName() {
        return depStationName;
    }

    public void setDepStationName(String depStationName) {
        this.depStationName = depStationName;
    }

    public String getDestStationName() {
        return destStationName;
    }

    public void setDestStationName(String destStationName) {
        this.destStationName = destStationName;
    }

    public String getTrainCategoryName() {
        return trainCategoryName;
    }

    public void setTrainCategoryName(String trainCategoryName) {
        this.trainCategoryName = trainCategoryName;
    }

    public int getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(int exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }

    public boolean isRouteError() {
        return routeError;
    }

    public void setRouteError(boolean routeError) {
        this.routeError = routeError;
    }

    public boolean isTrainCategoryError() {
        return trainCategoryError;
    }

    public void setTrainCategoryError(boolean trainCategoryError) {
        this.trainCategoryError = trainCategoryError;
    }

    public boolean isTicketAnnulledError() {
        return ticketAnnulledError;
    }

    public void setTicketAnnulledError(boolean ticketAnnulledError) {
        this.ticketAnnulledError = ticketAnnulledError;
    }
}
