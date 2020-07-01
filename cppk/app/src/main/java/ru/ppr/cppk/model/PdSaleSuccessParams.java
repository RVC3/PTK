package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSaleSuccessParams implements Parcelable {

    private BigDecimal pdCost;
    private SaleType saleType;
    private long newPDId;
    private boolean hideDeliveryButton;
    private long departureStationCode;
    private long destinationStationCode;

    public BigDecimal getPdCost() {
        return pdCost;
    }

    public void setPdCost(BigDecimal pdCost) {
        this.pdCost = pdCost;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

    public long getNewPDId() {
        return newPDId;
    }

    public void setNewPDId(long newPDId) {
        this.newPDId = newPDId;
    }

    public boolean isHideDeliveryButton() {
        return hideDeliveryButton;
    }

    public void setHideDeliveryButton(boolean hideDeliveryButton) {
        this.hideDeliveryButton = hideDeliveryButton;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pdCost.toPlainString());
        dest.writeInt(this.saleType == null ? -1 : this.saleType.ordinal());
        dest.writeLong(this.newPDId);
        dest.writeByte(this.hideDeliveryButton ? (byte) 1 : (byte) 0);
        dest.writeLong(this.departureStationCode);
        dest.writeLong(this.destinationStationCode);
    }

    public PdSaleSuccessParams() {
    }

    protected PdSaleSuccessParams(Parcel in) {
        this.pdCost = new BigDecimal(in.readString());
        int tmpSaleType = in.readInt();
        this.saleType = tmpSaleType == -1 ? null : SaleType.values()[tmpSaleType];
        this.newPDId = in.readLong();
        this.hideDeliveryButton = in.readByte() != 0;
        this.departureStationCode = in.readLong();
        this.destinationStationCode = in.readLong();
    }

    public static final Parcelable.Creator<PdSaleSuccessParams> CREATOR = new Parcelable.Creator<PdSaleSuccessParams>() {
        @Override
        public PdSaleSuccessParams createFromParcel(Parcel source) {
            return new PdSaleSuccessParams(source);
        }

        @Override
        public PdSaleSuccessParams[] newArray(int size) {
            return new PdSaleSuccessParams[size];
        }
    };
}
