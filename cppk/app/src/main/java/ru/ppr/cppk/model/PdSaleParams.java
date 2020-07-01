package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSaleParams implements Parcelable {

    private int ticketCategoryCode;
    private int directionCode;
    private long departureStationCode;
    private long destinationStationCode;
    private long couponReadEventId = -1;

    public PdSaleParams() {

    }

    public int getTicketCategoryCode() {
        return ticketCategoryCode;
    }

    public void setTicketCategoryCode(int ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
    }

    public int getDirectionCode() {
        return directionCode;
    }

    public void setDirectionCode(int directionCode) {
        this.directionCode = directionCode;
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

    public long getCouponReadEventId() {
        return couponReadEventId;
    }

    public void setCouponReadEventId(long couponReadEventId) {
        this.couponReadEventId = couponReadEventId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ticketCategoryCode);
        dest.writeInt(this.directionCode);
        dest.writeLong(this.departureStationCode);
        dest.writeLong(this.destinationStationCode);
        dest.writeLong(this.couponReadEventId);
    }

    protected PdSaleParams(Parcel in) {
        this.ticketCategoryCode = in.readInt();
        this.directionCode = in.readInt();
        this.departureStationCode = in.readLong();
        this.destinationStationCode = in.readLong();
        this.couponReadEventId = in.readLong();
    }

    public static final Parcelable.Creator<PdSaleParams> CREATOR = new Parcelable.Creator<PdSaleParams>() {
        @Override
        public PdSaleParams createFromParcel(Parcel source) {
            return new PdSaleParams(source);
        }

        @Override
        public PdSaleParams[] newArray(int size) {
            return new PdSaleParams[size];
        }
    };
}
