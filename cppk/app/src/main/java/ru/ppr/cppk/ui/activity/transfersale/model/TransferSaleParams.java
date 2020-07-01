package ru.ppr.cppk.ui.activity.transfersale.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.transfersale.TransferSaleActivity;

/**
 * Параметры для запуска {@link TransferSaleActivity}.
 *
 * @author Dmitry Nevolin
 */
public class TransferSaleParams implements Parcelable {
    /**
     * Код станции отправления
     */
    private long departureStationCode;
    /**
     * Код станции назначения
     */
    private long destinationStationCode;
    /**
     * Код вида ПД
     */
    private Long ticketTypeCode;
    /**
     * Версия НСИ для оформления ПД
     */
    private int nsiVersion;
    /**
     * Дата начала процесса оформления ПД
     */
    private Date timestamp;
    /**
     * Флаг наличия родительского ПД
     */
    private boolean withParentPd;
    /**
     * Номер родительского ПД
     */
    private int parentPdNumber;
    /**
     * Дата продажи родительского ПД
     */
    private Date parentPdSaleDateTime;
    /**
     * Дата начала действия родительского ПД
     */
    private Date parentPdStartDateTime;
    /**
     * Код тарифа родительского ПД
     */
    private long parentPdTariffCode;
    /**
     * Направление родительского ПД
     */
    private TicketWayType parentPdDirection;
    /**
     * Экспресс-код льготы родительского ПД
     */
    private int parentPdExemptionExpressCode;
    /**
     * Id устройства, продавшего родительский ПД
     */
    private long parentPdDeviceId;

    public TransferSaleParams() {

    }

    public long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }

    public long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    @Nullable
    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(@Nullable Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public boolean isWithParentPd() {
        return withParentPd;
    }

    public void setWithParentPd(boolean withParentPd) {
        this.withParentPd = withParentPd;
    }

    public int getParentPdNumber() {
        return parentPdNumber;
    }

    public void setParentPdNumber(int parentPdNumber) {
        this.parentPdNumber = parentPdNumber;
    }

    public Date getParentPdSaleDateTime() {
        return parentPdSaleDateTime;
    }

    public void setParentPdSaleDateTime(Date parentPdSaleDateTime) {
        this.parentPdSaleDateTime = parentPdSaleDateTime;
    }

    public Date getParentPdStartDateTime() {
        return parentPdStartDateTime;
    }

    public void setParentPdStartDateTime(Date parentPdStartDateTime) {
        this.parentPdStartDateTime = parentPdStartDateTime;
    }

    public long getParentPdTariffCode() {
        return parentPdTariffCode;
    }

    public void setParentPdTariffCode(long parentPdTariffCode) {
        this.parentPdTariffCode = parentPdTariffCode;
    }

    public TicketWayType getParentPdDirection() {
        return parentPdDirection;
    }

    public void setParentPdDirection(TicketWayType parentPdDirection) {
        this.parentPdDirection = parentPdDirection;
    }

    public int getParentPdExemptionExpressCode() {
        return parentPdExemptionExpressCode;
    }

    public void setParentPdExemptionExpressCode(int parentPdExemptionExpressCode) {
        this.parentPdExemptionExpressCode = parentPdExemptionExpressCode;
    }

    public long getParentPdDeviceId() {
        return parentPdDeviceId;
    }

    public void setParentPdDeviceId(long parentPdDeviceId) {
        this.parentPdDeviceId = parentPdDeviceId;
    }

    public int getNsiVersion() {
        return nsiVersion;
    }

    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransferSaleParams{" +
                "departureStationCode=" + departureStationCode +
                ", destinationStationCode=" + destinationStationCode +
                ", ticketTypeCode=" + ticketTypeCode +
                ", nsiVersion=" + nsiVersion +
                ", timestamp=" + timestamp +
                ", withParentPd=" + withParentPd +
                ", parentPdNumber=" + parentPdNumber +
                ", parentPdSaleDateTime=" + parentPdSaleDateTime +
                ", parentPdStartDateTime=" + parentPdStartDateTime +
                ", parentPdTariffCode=" + parentPdTariffCode +
                ", parentPdDirection=" + parentPdDirection +
                ", parentPdExemptionExpressCode=" + parentPdExemptionExpressCode +
                ", parentPdDeviceId=" + parentPdDeviceId +
                ", describeContents=" + describeContents() +
                '}';
    }

    public static class Builder {
        public static TransferSaleParams fromPD(@NonNull PD pd, @NonNull ReadForTransferParams readForTransferParams) {
            TransferSaleParams transferSaleParams = new TransferSaleParams();
            transferSaleParams.setDepartureStationCode(readForTransferParams.getDepartureStationCode());
            transferSaleParams.setDestinationStationCode(readForTransferParams.getDestinationStationCode());
            transferSaleParams.setTicketTypeCode(readForTransferParams.getTicketTypeCode());
            transferSaleParams.setNsiVersion(readForTransferParams.getNsiVersion());
            transferSaleParams.setTimestamp(readForTransferParams.getTimestamp());
            transferSaleParams.setWithParentPd(true);
            transferSaleParams.setParentPdNumber(pd.numberPD);
            transferSaleParams.setParentPdSaleDateTime(pd.saleDatetimePD);
            transferSaleParams.setParentPdStartDateTime(pd.getStartPdDate());
            transferSaleParams.setParentPdTariffCode(pd.tariffCodePD);
            transferSaleParams.setParentPdDirection(pd.wayType);
            transferSaleParams.setParentPdExemptionExpressCode(pd.exemptionCode);
            transferSaleParams.setParentPdDeviceId(pd.deviceId);
            return transferSaleParams;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.departureStationCode);
        dest.writeLong(this.destinationStationCode);
        dest.writeValue(this.ticketTypeCode);
        dest.writeInt(this.nsiVersion);
        dest.writeLong(this.timestamp != null ? this.timestamp.getTime() : -1);
        dest.writeByte(this.withParentPd ? (byte) 1 : (byte) 0);
        dest.writeInt(this.parentPdNumber);
        dest.writeLong(this.parentPdSaleDateTime != null ? this.parentPdSaleDateTime.getTime() : -1);
        dest.writeLong(this.parentPdStartDateTime != null ? this.parentPdStartDateTime.getTime() : -1);
        dest.writeLong(this.parentPdTariffCode);
        dest.writeInt(this.parentPdDirection == null ? -1 : this.parentPdDirection.ordinal());
        dest.writeInt(this.parentPdExemptionExpressCode);
        dest.writeLong(this.parentPdDeviceId);
    }

    protected TransferSaleParams(Parcel in) {
        this.departureStationCode = in.readLong();
        this.destinationStationCode = in.readLong();
        this.ticketTypeCode = (Long) in.readValue(Long.class.getClassLoader());
        this.nsiVersion = in.readInt();
        long tmpTimestamp = in.readLong();
        this.timestamp = tmpTimestamp == -1 ? null : new Date(tmpTimestamp);
        this.withParentPd = in.readByte() != 0;
        this.parentPdNumber = in.readInt();
        long tmpParentPdSaleDateTime = in.readLong();
        this.parentPdSaleDateTime = tmpParentPdSaleDateTime == -1 ? null : new Date(tmpParentPdSaleDateTime);
        long tmpParentPdStartDateTime = in.readLong();
        this.parentPdStartDateTime = tmpParentPdStartDateTime == -1 ? null : new Date(tmpParentPdStartDateTime);
        this.parentPdTariffCode = in.readLong();
        int tmpParentPdDirection = in.readInt();
        this.parentPdDirection = tmpParentPdDirection == -1 ? null : TicketWayType.values()[tmpParentPdDirection];
        this.parentPdExemptionExpressCode = in.readInt();
        this.parentPdDeviceId = in.readLong();
    }

    public static final Creator<TransferSaleParams> CREATOR = new Creator<TransferSaleParams>() {
        @Override
        public TransferSaleParams createFromParcel(Parcel source) {
            return new TransferSaleParams(source);
        }

        @Override
        public TransferSaleParams[] newArray(int size) {
            return new TransferSaleParams[size];
        }
    };
}
