package ru.ppr.cppk.localdb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Информация о родительском ПД.
 * Заполняется при продаже разового ПД без места на 2-й сегмент поездки инфо о билете на первый сегмент.
 *
 * @author Ivan Bachtin
 */
public class ParentTicketInfo implements LocalModelWithId<Long>, Parcelable {
    /**
     * Id
     */
    private transient Long id;
    /**
     * Дата продажи
     */
    @SerializedName("SaleDateTime")
    private Date saleDateTime;
    /**
     * Номер билета
     */
    @SerializedName("TicketNumber")
    private int ticketNumber;
    /**
     * Номер кассы
     */
    @SerializedName("CashRegisterNumber")
    private long cashRegisterNumber;
    /**
     * Направление действия ПД
     */
    @SerializedName("WayType")
    private TicketWayType wayType;

    public ParentTicketInfo() {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public long getCashRegisterNumber() {
        return cashRegisterNumber;
    }

    public void setCashRegisterNumber(long cashRegisterNumber) {
        this.cashRegisterNumber = cashRegisterNumber;
    }

    public TicketWayType getWayType() {
        return wayType;
    }

    public void setWayType(TicketWayType wayType) {
        this.wayType = wayType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(saleDateTime.getTime());
        dest.writeInt(ticketNumber);
        dest.writeLong(cashRegisterNumber);
        dest.writeInt(wayType.getCode());
    }

    private ParentTicketInfo(Parcel source) {
        saleDateTime = new Date(source.readLong());
        ticketNumber = source.readInt();
        cashRegisterNumber = source.readLong();
        wayType = TicketWayType.valueOf(source.readInt());
    }

    public static final Parcelable.Creator<ParentTicketInfo> CREATOR = new Parcelable.Creator<ParentTicketInfo>() {

        @Override
        public ParentTicketInfo createFromParcel(Parcel source) {
            return new ParentTicketInfo(source);
        }

        @Override
        public ParentTicketInfo[] newArray(int size) {
            return new ParentTicketInfo[size];
        }
    };
}
