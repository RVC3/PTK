package ru.ppr.nsi.entity;

/**
 * Связь станций с тарифными поясами(зонами)
 */
public class StationToTariffZone extends BaseNSIObject<Long> {
    /**
     * Код станции
     */
    private long stationCode;
    /**
     * Код тарифного пояса (зоны)
     */
    private long tariffZoneCode;

    private boolean primaryStation;

    public long getStationCode() {
        return stationCode;
    }

    public void setStationCode(long stationCode) {
        this.stationCode = stationCode;
    }

    public long getTariffZoneCode() {
        return tariffZoneCode;
    }

    public void setTariffZoneCode(long tariffZoneCode) {
        this.tariffZoneCode = tariffZoneCode;
    }

    public boolean isPrimaryStation() {
        return primaryStation;
    }

    public void setPrimaryStation(boolean primaryStation) {
        this.primaryStation = primaryStation;
    }
}
