package ru.ppr.cppk.ui.activity.pdrepeal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Параметры для экрана {@link PdRepealActivity}.
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealParams implements Parcelable {
    /**
     * Id события продажи
     */
    private long pdSaleEventId;
    /**
     * Причина аннулирования
     */
    private String repealReason;

    public long getPdSaleEventId() {
        return pdSaleEventId;
    }

    public void setPdSaleEventId(long pdSaleEventId) {
        this.pdSaleEventId = pdSaleEventId;
    }

    public String getRepealReason() {
        return repealReason;
    }

    public void setRepealReason(String repealReason) {
        this.repealReason = repealReason;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.pdSaleEventId);
        dest.writeString(this.repealReason);
    }

    public PdRepealParams() {
    }

    protected PdRepealParams(Parcel in) {
        this.pdSaleEventId = in.readLong();
        this.repealReason = in.readString();
    }

    public static final Parcelable.Creator<PdRepealParams> CREATOR = new Parcelable.Creator<PdRepealParams>() {
        @Override
        public PdRepealParams createFromParcel(Parcel source) {
            return new PdRepealParams(source);
        }

        @Override
        public PdRepealParams[] newArray(int size) {
            return new PdRepealParams[size];
        }
    };
}
