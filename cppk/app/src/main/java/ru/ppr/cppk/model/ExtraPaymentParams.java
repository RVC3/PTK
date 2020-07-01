package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;

/**
 * Информация для оформления доплаты.
 *
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentParams implements Parcelable {

    private int parentPdNumber;
    private Date parentPdSaleDateTime;
    private long parentPdTariffCode;
    private int parentPdDirectionCode;
    private long parentPdDeviceId;
    private SmartCardId smartCardId;
    private int parentPdExemptionExpressCode;
    private AdditionalInfoForEtt additionalInfoForEtt;

    public ExtraPaymentParams() {

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

    public long getParentPdTariffCode() {
        return parentPdTariffCode;
    }

    public void setParentPdTariffCode(long parentPdTariffCode) {
        this.parentPdTariffCode = parentPdTariffCode;
    }

    public int getParentPdDirectionCode() {
        return parentPdDirectionCode;
    }

    public void setParentPdDirectionCode(int parentPdDirectionCode) {
        this.parentPdDirectionCode = parentPdDirectionCode;
    }

    public long getParentPdDeviceId() {
        return parentPdDeviceId;
    }

    public void setParentPdDeviceId(long parentPdDeviceId) {
        this.parentPdDeviceId = parentPdDeviceId;
    }

    public SmartCardId getSmartCardId() {
        return smartCardId;
    }

    public void setSmartCardId(SmartCardId smartCardId) {
        this.smartCardId = smartCardId;
    }

    public int getParentPdExemptionExpressCode() {
        return parentPdExemptionExpressCode;
    }

    public void setParentPdExemptionExpressCode(int parentPdExemptionExpressCode) {
        this.parentPdExemptionExpressCode = parentPdExemptionExpressCode;
    }

    public AdditionalInfoForEtt getAdditionalInfoForEtt() {
        return additionalInfoForEtt;
    }

    public void setAdditionalInfoForEtt(AdditionalInfoForEtt additionalInfoForEtt) {
        this.additionalInfoForEtt = additionalInfoForEtt;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.parentPdNumber);
        dest.writeLong(this.parentPdSaleDateTime != null ? this.parentPdSaleDateTime.getTime() : -1);
        dest.writeLong(this.parentPdTariffCode);
        dest.writeInt(this.parentPdDirectionCode);
        dest.writeLong(this.parentPdDeviceId);
        dest.writeParcelable(this.smartCardId, flags);
        dest.writeInt(this.parentPdExemptionExpressCode);
        dest.writeParcelable(this.additionalInfoForEtt, flags);
    }

    protected ExtraPaymentParams(Parcel in) {
        this.parentPdNumber = in.readInt();
        long tmpParentPdSaleDateTime = in.readLong();
        this.parentPdSaleDateTime = tmpParentPdSaleDateTime == -1 ? null : new Date(tmpParentPdSaleDateTime);
        this.parentPdTariffCode = in.readLong();
        this.parentPdDirectionCode = in.readInt();
        this.parentPdDeviceId = in.readLong();
        this.smartCardId = in.readParcelable(SmartCardId.class.getClassLoader());
        this.parentPdExemptionExpressCode = in.readInt();
        this.additionalInfoForEtt = in.readParcelable(AdditionalInfoForEtt.class.getClassLoader());
    }

    public static final Creator<ExtraPaymentParams> CREATOR = new Creator<ExtraPaymentParams>() {
        @Override
        public ExtraPaymentParams createFromParcel(Parcel source) {
            return new ExtraPaymentParams(source);
        }

        @Override
        public ExtraPaymentParams[] newArray(int size) {
            return new ExtraPaymentParams[size];
        }
    };
}
