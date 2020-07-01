package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Идентификационная информация смарт-карты.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardId implements Parcelable {
    /**
     * Код типа носителя ПД
     */
    private int ticketStorageTypeCode;
    /**
     * Номер кристалла
     */
    private String crystalSerialNumber;
    /**
     * Внешний номер карты без пробелов.
     * Для ЭТТ содержит 14 цифр/
     * Для ТРОЙКИ/СТРЕЛКИ 10 цифр
     * Для карт СКМ, СКМО, ИПК берется из эмисионных данных
     */
    private String outerNumber;

    public SmartCardId() {

    }

    public int getTicketStorageTypeCode() {
        return ticketStorageTypeCode;
    }

    public void setTicketStorageTypeCode(int ticketStorageTypeCode) {
        this.ticketStorageTypeCode = ticketStorageTypeCode;
    }

    public String getCrystalSerialNumber() {
        return crystalSerialNumber;
    }

    public void setCrystalSerialNumber(String crystalSerialNumber) {
        this.crystalSerialNumber = crystalSerialNumber;
    }

    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartCardId that = (SmartCardId) o;

        if (ticketStorageTypeCode != that.ticketStorageTypeCode) return false;
        if (crystalSerialNumber != null ? !crystalSerialNumber.equals(that.crystalSerialNumber) : that.crystalSerialNumber != null)
            return false;
        return outerNumber != null ? outerNumber.equals(that.outerNumber) : that.outerNumber == null;

    }

    @Override
    public int hashCode() {
        int result = ticketStorageTypeCode;
        result = 31 * result + (crystalSerialNumber != null ? crystalSerialNumber.hashCode() : 0);
        result = 31 * result + (outerNumber != null ? outerNumber.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ticketStorageTypeCode);
        dest.writeString(this.crystalSerialNumber);
        dest.writeString(this.outerNumber);
    }

    protected SmartCardId(Parcel in) {
        this.ticketStorageTypeCode = in.readInt();
        this.crystalSerialNumber = in.readString();
        this.outerNumber = in.readString();
    }

    public static final Parcelable.Creator<SmartCardId> CREATOR = new Parcelable.Creator<SmartCardId>() {
        @Override
        public SmartCardId createFromParcel(Parcel source) {
            return new SmartCardId(source);
        }

        @Override
        public SmartCardId[] newArray(int size) {
            return new SmartCardId[size];
        }
    };
}
