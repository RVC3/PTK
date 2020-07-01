package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Номер карты и эмиссионные данные
 *
 * @author Brazhkin A.V.
 */
public class EmissionData implements Parcelable {

    private Integer formatVersion;
    private String cardNumber;
    private String cardSeries;
    private Integer controlSum;
    private Date validityTime;
    private Integer regionCode;
    private Integer orderNumber;

    public EmissionData() {

    }

    // Т.к. данный клас необходимо сделать "иммутабельным", но для тестов нужно установить
    // значения в поля то сделаем сеттеры package private
    public void setFormatVersion(Integer formatVersion) {
        this.formatVersion = formatVersion;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardSeries(String cardSeries) {
        this.cardSeries = cardSeries;
    }

    public void setControlSum(Integer controlSum) {
        this.controlSum = controlSum;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setValidityTime(Date validityTime) {
        this.validityTime = validityTime;
    }

    public void setRegionCode(Integer regionCode) {
        this.regionCode = regionCode;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public Date getValidityTime() {
        return validityTime;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(formatVersion);
        dest.writeString(cardNumber);
        dest.writeString(cardSeries);
        dest.writeInt(controlSum);
        dest.writeLong(validityTime.getTime());
        dest.writeInt(regionCode);
        dest.writeInt(orderNumber);
    }

    private EmissionData(Parcel source) {
        formatVersion = source.readInt();
        cardNumber = source.readString();
        cardSeries = source.readString();
        controlSum = source.readInt();
        validityTime = new Date(source.readLong());
        regionCode = source.readInt();
        orderNumber = source.readInt();
    }

    public static final Parcelable.Creator<EmissionData> CREATOR = new Creator<EmissionData>() {

        @Override
        public EmissionData[] newArray(int size) {
            return new EmissionData[size];
        }

        @Override
        public EmissionData createFromParcel(Parcel source) {
            return new EmissionData(source);
        }
    };

    @Override
    public String toString() {
        return "EmissionData{" +
                "formatVersion=" + formatVersion +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardSeries='" + cardSeries + '\'' +
                ", controlSum=" + controlSum +
                ", validityTime=" + validityTime +
                ", regionCode=" + regionCode +
                ", orderNumber=" + orderNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmissionData that = (EmissionData) o;

        if (!formatVersion.equals(that.formatVersion)) return false;
        if (!cardNumber.equals(that.cardNumber)) return false;
        if (!cardSeries.equals(that.cardSeries)) return false;
        if (!controlSum.equals(that.controlSum)) return false;
        if (!validityTime.equals(that.validityTime)) return false;
        if (!regionCode.equals(that.regionCode)) return false;
        return orderNumber.equals(that.orderNumber);

    }

    @Override
    public int hashCode() {
        int result = formatVersion.hashCode();
        result = 31 * result + cardNumber.hashCode();
        result = 31 * result + cardSeries.hashCode();
        result = 31 * result + controlSum.hashCode();
        result = 31 * result + validityTime.hashCode();
        result = 31 * result + regionCode.hashCode();
        result = 31 * result + orderNumber.hashCode();
        return result;
    }
}
