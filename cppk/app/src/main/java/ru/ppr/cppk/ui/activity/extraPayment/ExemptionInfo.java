package ru.ppr.cppk.ui.activity.extraPayment;

import android.os.Parcel;
import android.os.Parcelable;

import ru.ppr.cppk.entity.event.model.ExemptionForEvent;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionInfo implements Parcelable {

    public ExemptionInfo() {

    }

    protected ExemptionInfo(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExemptionInfo> CREATOR = new Creator<ExemptionInfo>() {
        @Override
        public ExemptionInfo createFromParcel(Parcel in) {
            return new ExemptionInfo(in);
        }

        @Override
        public ExemptionInfo[] newArray(int size) {
            return new ExemptionInfo[size];
        }
    };

    public static ExemptionInfo from(ExemptionForEvent exemptionForEvent) {
        ExemptionInfo exemptionInfo = new ExemptionInfo();
        return exemptionInfo;
    }
}
