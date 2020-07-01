package ru.ppr.nsi.model;

/**
 * @author Dmitry Nevolin
 */
public class TransferStationRoute {

    private Long depStationCode;
    private Long destStationCode;
    private String routeCode;

    public Long getDepStationCode() {
        return depStationCode;
    }

    public void setDepStationCode(Long depStationCode) {
        this.depStationCode = depStationCode;
    }

    public Long getDestStationCode() {
        return destStationCode;
    }

    public void setDestStationCode(Long destStationCode) {
        this.destStationCode = destStationCode;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

}
