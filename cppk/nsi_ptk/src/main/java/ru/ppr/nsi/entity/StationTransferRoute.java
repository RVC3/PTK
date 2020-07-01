package ru.ppr.nsi.entity;

/**
 * Связующая таблица станций и автобусных маршрутов
 * @author Dmitry Nevolin
 */
public class StationTransferRoute {

    private Long stationCode = null;
    private Long routeCode = null;

    public Long getStationCode() {
        return stationCode;
    }

    public void setStationCode(Long stationCode) {
        this.stationCode = stationCode;
    }

    public Long getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(Long routeCode) {
        this.routeCode = routeCode;
    }

}
