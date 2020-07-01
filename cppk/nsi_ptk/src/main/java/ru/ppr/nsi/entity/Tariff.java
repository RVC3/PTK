package ru.ppr.nsi.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.TariffPlanRepository;

/**
 * Тариф.
 */
public class Tariff implements Parcelable {

    private Integer tariffPlanCode = null;
    private Integer stationDestinationCode = null;
    private Integer stationDepartureCode = null;
    private Integer ticketTypeCode = null;
    private Integer code = null; // id
    private String routeCode = null;
    private BigDecimal pricePd = new BigDecimal("-1");
    private int versionId = 0;
    private Integer deleteInVersion = null;
    private TariffPlan tariffPlan = null;
    private Station stationDestination = null;
    private Station stationDeparture = null;
    private TicketType ticketType = null;
    /**
     * Тарифный пояс отправления (специальный)
     */
    private Long departureTariffZoneCode;
    /**
     * Тарифный пояс назначения (специальный)
     */
    private Long destinationTariffZoneCode;

    public Tariff() {
    }

    public Tariff(Parcel source) {
        try {
            tariffPlanCode = source.readInt();
            routeCode = source.readString();
            stationDepartureCode = source.readInt();
            stationDestinationCode = source.readInt();
            ticketTypeCode = source.readInt();
            code = source.readInt();
            versionId = source.readInt();
            int deleteVersion = source.readInt();
            deleteInVersion = deleteVersion == -1 ? null : deleteVersion;
            pricePd = new BigDecimal(source.readString());
            long depTariffZoneCode = source.readLong();
            departureTariffZoneCode = depTariffZoneCode == -1 ? null : depTariffZoneCode;
            long destTariffZoneCode = source.readLong();
            destinationTariffZoneCode = destTariffZoneCode == -1 ? null : destTariffZoneCode;

        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("Tariff", e.getMessage());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tariffPlanCode);
        dest.writeString(routeCode);
        dest.writeInt(stationDepartureCode);
        dest.writeInt(stationDestinationCode);
        dest.writeInt(ticketTypeCode);
        dest.writeInt(code);
        dest.writeInt(versionId);
        dest.writeInt(deleteInVersion == null ? -1 : deleteInVersion);
        dest.writeString(pricePd.toString());
        dest.writeLong(departureTariffZoneCode == null ? -1 : departureTariffZoneCode);
        dest.writeLong(destinationTariffZoneCode == null ? -1 : destinationTariffZoneCode);
    }

    public static final Parcelable.Creator<Tariff> CREATOR = new Parcelable.Creator<Tariff>() {

        @Override
        public Tariff createFromParcel(Parcel source) {

            return new Tariff(source);
        }

        @Override
        public Tariff[] newArray(int size) {
            return new Tariff[size];
        }
    };

    public TariffPlan getTariffPlan(@NonNull TariffPlanRepository tariffPlanRepository) {
        if (tariffPlan == null)
            tariffPlan = tariffPlanRepository.load(tariffPlanCode, versionId);
        return tariffPlan;
    }

    public Station getStationDestination(NsiDaoSession nsiDaoSession) {
        if (stationDestination == null)
            stationDestination = nsiDaoSession.getStationDao().load((long) stationDestinationCode, versionId);
        return stationDestination;
    }

    public Station getStationDeparture(NsiDaoSession nsiDaoSession) {
        if (stationDeparture == null)
            stationDeparture = nsiDaoSession.getStationDao().load((long) stationDepartureCode, versionId);
        return stationDeparture;
    }

    public TicketType getTicketType(NsiDaoSession nsiDaoSession) {
        if (ticketType == null)
            ticketType = nsiDaoSession.getTicketTypeDao().load(ticketTypeCode, versionId);
        return ticketType;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public String toString() {
        return "Tariff{" +
                "tariffPlanCode=" + tariffPlanCode +
                ", stationDestinationCode=" + stationDestinationCode +
                ", stationDepartureCode=" + stationDepartureCode +
                ", ticketTypeCode=" + ticketTypeCode +
                ", code=" + code +
                ", versionId=" + versionId +
                ", pricePd=" + pricePd +
                ", routeCode='" + routeCode + '\'' +
                ", departureTariffZoneCode='" + departureTariffZoneCode + '\'' +
                ", destinationTariffZoneCode='" + destinationTariffZoneCode + '\'' +
                '}';
    }


    public Integer getDeleteInVersion() {
        return deleteInVersion;
    }

    public void setDeleteInVersion(Integer deleteInVersion) {
        this.deleteInVersion = deleteInVersion;
    }

    public Long getDepartureTariffZoneCode() {
        return departureTariffZoneCode;
    }

    public void setDepartureTariffZoneCode(Long departureTariffZoneCode) {
        this.departureTariffZoneCode = departureTariffZoneCode;
    }

    public Long getDestinationTariffZoneCode() {
        return destinationTariffZoneCode;
    }

    public void setDestinationTariffZoneCode(Long destinationTariffZoneCode) {
        this.destinationTariffZoneCode = destinationTariffZoneCode;
    }

    public Integer getTariffPlanCode() {
        return tariffPlanCode;
    }

    public void setTariffPlanCode(Integer tariffPlanCode) {
        this.tariffPlanCode = tariffPlanCode;
    }

    public Integer getStationDestinationCode() {
        return stationDestinationCode;
    }

    public void setStationDestinationCode(Integer stationDestinationCode) {
        this.stationDestinationCode = stationDestinationCode;
    }

    public Integer getStationDepartureCode() {
        return stationDepartureCode;
    }

    public void setStationDepartureCode(Integer stationDepartureCode) {
        this.stationDepartureCode = stationDepartureCode;
    }

    public Integer getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Integer ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public BigDecimal getPricePd() {
        return pricePd;
    }

    public void setPricePd(BigDecimal pricePd) {
        this.pricePd = pricePd;
    }

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }
}
