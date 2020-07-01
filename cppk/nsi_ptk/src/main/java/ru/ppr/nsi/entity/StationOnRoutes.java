package ru.ppr.nsi.entity;

/**
 * Cущность для таблицы StationOnRoutes из НСИ
 *
 * @author A.Ushakov
 */
public class StationOnRoutes {

    private int routeCode = -1;
    private int stationNumber = -1;
    private int stationCode = -1;
    private int distanceFromBegining = -1;
    private int zoneNumber = -1;
    private boolean turnstileExist;
    private boolean railway;
    private boolean hasNext;
    private boolean noFracture;
    private boolean tariffBorder;
    private boolean departureStation;
    private boolean destinationStation;

    public int getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(int routeCode) {
        this.routeCode = routeCode;
    }

    public int getStationNumber() {
        return stationNumber;
    }

    public void setStationNumber(int stationNumber) {
        this.stationNumber = stationNumber;
    }

    public int getStationCode() {
        return stationCode;
    }

    public void setStationCode(int stationCode) {
        this.stationCode = stationCode;
    }

    public int getDistanceFromBegining() {
        return distanceFromBegining;
    }

    public void setDistanceFromBegining(int distanceFromBegining) {
        this.distanceFromBegining = distanceFromBegining;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    public boolean isTurnstileExist() {
        return turnstileExist;
    }

    public void setTurnstileExist(boolean turnstileExist) {
        this.turnstileExist = turnstileExist;
    }

    public boolean isRailway() {
        return railway;
    }

    public void setRailway(boolean railway) {
        this.railway = railway;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isNoFracture() {
        return noFracture;
    }

    public void setNoFracture(boolean noFracture) {
        this.noFracture = noFracture;
    }

    public boolean isTariffBorder() {
        return tariffBorder;
    }

    public void setTariffBorder(boolean tariffBorder) {
        this.tariffBorder = tariffBorder;
    }

    public boolean isDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(boolean departureStation) {
        this.departureStation = departureStation;
    }

    public boolean isDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(boolean destinationStation) {
        this.destinationStation = destinationStation;
    }

}
