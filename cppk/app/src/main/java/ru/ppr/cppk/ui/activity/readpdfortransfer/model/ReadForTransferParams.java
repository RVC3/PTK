package ru.ppr.cppk.ui.activity.readpdfortransfer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.ui.activity.ResultBarcodeActivity;
import ru.ppr.cppk.ui.activity.RfidResultActivity;
import ru.ppr.cppk.ui.activity.controlreadbarcode.ControlReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.controlreadbsc.ControlReadBscActivity;
import ru.ppr.cppk.ui.activity.readpdfortransfer.ReadPdForTransferActivity;

/**
 * Параметры для запуска {@link ReadPdForTransferActivity}.
 * По факту - пришлось переиспользовать и в других местах, например:
 * {@link ControlReadBarcodeActivity}
 * {@link ControlReadBscActivity}
 * {@link ResultBarcodeActivity}
 * {@link RfidResultActivity}
 * Причина: сложность навигации при считывании базового ПД для оформления трансфера.
 * Придумать что-нибудь получше в будущем.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadForTransferParams implements Parcelable {
    /**
     * Код станции отправления
     */
    private long departureStationCode;
    /**
     * Код станции назначения
     */
    private long destinationStationCode;
    /**
     * Коды тарифных планов
     */
    private List<Long> tariffPlanCodes;
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

    public List<Long> getTariffPlanCodes() {
        return tariffPlanCodes;
    }

    public void setTariffPlanCodes(List<Long> tariffPlanCodes) {
        this.tariffPlanCodes = tariffPlanCodes;
    }

    @Nullable
    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(@Nullable Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
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
        return "ReadForTransferParams{" +
                "departureStationCode=" + departureStationCode +
                ", destinationStationCode=" + destinationStationCode +
                ", tariffPlanCodes=" + tariffPlanCodes +
                ", ticketTypeCode=" + ticketTypeCode +
                ", nsiVersion=" + nsiVersion +
                ", timestamp=" + timestamp +
                ", describeContents=" + describeContents() +
                '}';
    }

    public ReadForTransferParams() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.departureStationCode);
        dest.writeLong(this.destinationStationCode);
        dest.writeList(this.tariffPlanCodes);
        dest.writeValue(this.ticketTypeCode);
        dest.writeInt(this.nsiVersion);
        dest.writeLong(this.timestamp != null ? this.timestamp.getTime() : -1);
    }

    protected ReadForTransferParams(Parcel in) {
        this.departureStationCode = in.readLong();
        this.destinationStationCode = in.readLong();
        this.tariffPlanCodes = new ArrayList<Long>();
        in.readList(this.tariffPlanCodes, Long.class.getClassLoader());
        this.ticketTypeCode = (Long) in.readValue(Long.class.getClassLoader());
        this.nsiVersion = in.readInt();
        long tmpTimestamp = in.readLong();
        this.timestamp = tmpTimestamp == -1 ? null : new Date(tmpTimestamp);
    }

    public static final Creator<ReadForTransferParams> CREATOR = new Creator<ReadForTransferParams>() {
        @Override
        public ReadForTransferParams createFromParcel(Parcel source) {
            return new ReadForTransferParams(source);
        }

        @Override
        public ReadForTransferParams[] newArray(int size) {
            return new ReadForTransferParams[size];
        }
    };
}
